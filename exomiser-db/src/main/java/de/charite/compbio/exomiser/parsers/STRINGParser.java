package de.charite.compbio.exomiser.parsers;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



/**
 * A class designed to parse in protein - protein interaction data from STRING.
 * This is the data used to perform Random Walk analysis in ExomeWalker. However, we
 * also want to display all protein interactions of distance one and two in the HTML 
 * results page, and we will store PPI data in the SQL table string for this purpose.
 * <P>
 * Before this class is called by {@link exomizer.PopulateExomiserDatabase},
 * you need to download the file <b>protein.links.detailed.v9.1.txt.gz</b> from the STRING
 * database und uncompress it. Then, go to the <b>scripts</b> directory in the Exomiser
 * project, and run the R script <b>downloadEns2Entrez.R</b>. This script will download a 
 * mapping between Ensembl ids (which are used by STRING), and Entrez Gene ids (which are
 * used by the Exomiser application). Note that we will directly import the entrezGene to
 * gene symbol data into the database, as a table called entrez2sym.
 * @see <a href="http://string-db.org/">STRING database</a>
 * @author Peter Robinson
 * @version 0.05 (15 Feb, 2014).
 */
public class STRINGParser {

     /** Handle to the SQL database (postgreSQL) */
    private Connection connection = null;
     /**
     * Path to the STRING (http://string-db.org/) file protein.links.v9.1.txt.
     */
     private String stringPath = null;
    /**
     * In order to parse the STRING data (see {@link #stringPath}, we need to map between the
     * Ensembl peptide ids (used by STRING) and EntrezGene ids. There is an R biomaRt script
     * in the scripts directory of the Exomiser SVN repository that downloads all such mappings
     * for homo sapiens. This creates a file with the columns ensembl_peptide_id, hgnc_gene_symbol,
     * and entrezgene.*/
    private String ensemblMapPath = null;

    private HashMap<Integer,String> entrez2sym=null;


    private HashMap<String,ArrayList<Integer>> ensembl2EntrezGene=null;

    
    private HashSet<Interaction> interactionSet = null;

    public STRINGParser(String STRING, String biomaRt, Connection conn) {
	this.stringPath = STRING;
	this.ensemblMapPath=biomaRt;
	this.ensembl2EntrezGene = new HashMap<String,ArrayList<Integer>>();
	this.interactionSet = new HashSet<Interaction>();
	this.connection=conn;
   }

    /**
     * Parses the two input files (a biomaRt map and the main STRING datafile
     * for protein-protein interactions). Outputs a "dump" file that is used to
     * import the data into the Exomiser database.
     */
    public void parseFiles() {
	try {
	    parseBiomartMap();
	    addBiomartToDatabase();
	    parseSTRING();
	    outputDumpFile();
	} catch (IOException e) {
	    System.err.println("Problems parsing STRING/Biomart file");
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }


    /**
     * A simple struct-like class representing an interaction.
     */
    class Interaction {
	int entrezGeneA;
	int entrezGeneB;
	int score;

	public Interaction(int A, int B, int sc) {
	    this.entrezGeneA=A;
	    this.entrezGeneB=B;
	    this.score=sc;
	}
	public String getDumpLine() {
	    return String.format("%d|%d|%d",entrezGeneA,entrezGeneB,score);
	}

	/**
	 * We regard two interaction objects as being equal if both of
	 * the interactants are the same. Note we are not interested in the
	 * score and will take one or other of the scores arbitrarily if
	 * we find objects that are equal like this while constructing the
	 * hashmap of interactions.
	 */
	@Override public boolean equals(Object obj) {
	    
	    Interaction other = (Interaction) obj;
	    if (this == other)
		return true;
	    if (other == null)
		return false;
	    if (other.entrezGeneA == this.entrezGeneA && other.entrezGeneB == this.entrezGeneB)
		return true;
	    if (other.entrezGeneB == this.entrezGeneA && other.entrezGeneA == this.entrezGeneB)
		return true;
	    return false;
	}

	@Override public int hashCode(){
	    int x=37;
	    x += 17*entrezGeneA;
	    x += 17*entrezGeneB;
	    return x+13;
	}

    }

    /**
     * Output a dump file we will use to add the informationto the postgreSQL database.
     */
    private void outputDumpFile() throws IOException
    {
	String fname = "string.pg";
	FileWriter fwriter = new FileWriter(fname);
	BufferedWriter out  = new BufferedWriter(fwriter);
	for (Interaction f: interactionSet) {
	    out.write(f.getDumpLine()+"\n");
	}
	out.close();

	String stringPth ="/path/string.pg";
	try {
	    File f = new File("string.pg");
	    stringPth = f.getAbsolutePath();
	} catch (Exception e) {
	    e.printStackTrace();
	    // skip
	}
	System.out.println("[INFO] I have just created the file \"string.pg\"");
	System.out.println("[INFO] You will need to import this file into the Exomiser database (hgmddisease first)");
	System.out.println("\t$ psql -h localhost exomizer -U postgres -W");
	System.out.println("\t(enter password for user \"postgres\")");
	System.out.println(String.format("\texomizer=# COPY string FROM \'%s' WITH DELIMITER \'|\';",stringPth));
    }



    /**
     * Main method for parsing the STRING protein interaction file.
     */
    private void parseSTRING() throws IOException
    {
	FileInputStream fstr = new FileInputStream(this.stringPath);
	BufferedReader br = new BufferedReader(new InputStreamReader(fstr));
	String header = br.readLine();
	
	String line=null;
	boolean in_human_block=false;
	while ((line = br.readLine()) != null)   {
	    if (! line.startsWith("9606")) {
		if (in_human_block) {
		    /* This means we are now finished with the human data */
		    break;
		} else {
		    /* This means we are still in front of the human data, skip and go on. */
		    continue;
		}
	    } else {
		in_human_block=true;
	    }
	    
	    String split[] = line.split("\\s+");
	    String p1=null,p2=null;
	    if (split[0].substring(0,5).equals("9606.")) {
		p1= split[0].substring(5);
	    } else {
		System.err.println("Malformed protein (p1): " + line);
		System.exit(1);
	    }
	    if (split[1].substring(0,5).equals("9606.")) {
		p2= split[1].substring(5);
	    } else {
		System.err.println("Malformed protein (p2): " + line);
		System.exit(1);
	    }
	    Integer score = null;
	    try {
		score = Integer.parseInt(split[2]);
	    } catch (NumberFormatException e) {
		System.err.println("Malformed score: " + line + "\n\t(could not parse field: \"" + split[2] + "\"");
		System.exit(1);
	    }
	    ArrayList<Integer> e1 = this.ensembl2EntrezGene.get(p1);
	    ArrayList<Integer> e2 = this.ensembl2EntrezGene.get(p2);
	    if (e1==null || e2==null) {
		/* cannot find entrezgene id, just skip */
		continue;
	    }
	    if (score <700)  {
		/* Note that STRING high-confidence scores have a score
		   of at least 0.700 (which is stored as 700 in this file). */
		continue;
	    }
	    for (Integer a : e1) {
		for (Integer b : e2) {
		    Interaction ita = new Interaction(a,b,score);
		    //System.out.println(a + " / " + b + "(" + score + ")");
		    if (! this.interactionSet.contains(ita))
			this.interactionSet.add(ita);
		}
	    }
	}
	System.out.println("[INFO] Parsed a total of " + this.interactionSet.size() + " high-confidence interactions");
    }


    /**
     * Add the contents of {@link #entrez2sym} to the
     * Exomiser database. Note that we use batches of
     * 500 prepared statements each, this improves
     * performance substantially over single inserts.
     */
    private void addBiomartToDatabase() {
	System.out.println("Will populate entrez2sym table...");
	try {
	    this.connection.setAutoCommit(false);
	    String insert = "INSERT INTO entrez2sym(entrezID, symbol) VALUES(?,?);";
	    PreparedStatement ins = this.connection.prepareStatement(insert);
	    Iterator<Integer> it = this.entrez2sym.keySet().iterator();
	    int i=0;
	    while (it.hasNext()) {
		i++;
		Integer id = it.next();
		if (id == null)
		    continue;
		String sym = this.entrez2sym.get(id);
		if (sym == null)
		    continue;
		ins.setInt(1,id);
		ins.setString(2,sym);	
		ins.addBatch();
		if (i%500==0) {
		    int[] y = ins.executeBatch();
		    System.out.println(i + ") Added " + y.length + " lines to entrez2sym table");
		}
	    }
	     int[] y = ins.executeBatch();
	     System.out.println(i + ") Added " + y.length + " lines to entrez2sym table");
	     System.out.println("Finished. Added " + i + " entries to entrez2sym table.");
	     ins.close();
	     this.connection.setAutoCommit(true);
	} catch (SQLException e) {
	    System.err.println("Error inserting entrez2sym data into Exomiser database");
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }



    /**
     * Parses the contents of the file at {@link #ensemblMapPath} and places them
     * into the map {@link #ensembl2EntrezGene}. This file is constructed to have
     * the structure:
     * <pre>
     * ensembl_peptide_id	hgnc_symbol	entrezgene
     * ENSP00000456546	SLC25A26	115286
     * ...
     * </pre>
     * We create a hashMap with key: entrezgene, and value, hgncsymbol. THis will
     * be the basis of the SQL table entrez2sym.
     */
    private void parseBiomartMap() throws IOException
    {
	this.entrez2sym = new HashMap<Integer,String>();
	HashSet<Integer> noSym = new HashSet<Integer>();
	FileInputStream fstr = new FileInputStream(this.ensemblMapPath);
	BufferedReader br = new BufferedReader(new InputStreamReader(fstr));
	String header = br.readLine();
	if (! header.startsWith("ensembl_peptide_id")) {
	    System.err.println("[ERROR] Ensembl mapping file malformed, first line should start with \"ensembl_peptide_id\" but it was:" +
			       header);
	    //System.exit(1);
	}
	String line=null;
	int n_noEntrez=0;
	int doubleEnt=0;
	while ((line = br.readLine()) != null)   {
	    //System.out.println(line);
	    if (line.startsWith("#"))
		continue; /* skip the header */
	    String split[] = line.split("\t");
	    if (split.length != 3) {
		/* should never happen, sanity check */
		System.err.println("Error: malformed line: " + line + " (line does not have three fields but " + split.length + ")");
		System.exit(1);
	    }
	    String ens = split[0];
	    Integer entrez = null;
	    if (split[2].equals("NA")) {
		n_noEntrez++; /* This line has no EntrezGene entry, so just skip it. */
		continue;
	    }
	    try {
		entrez = Integer.parseInt(split[2]);
	    } catch (NumberFormatException e) {
		System.err.println("Error: malformed line: " + line + " (could not parse entrez gene field: " + split[2]);
		System.exit(1);
	    }
	    String symbol = split[1];
	    if (symbol == null || symbol.length()==0) {
		noSym.add(entrez);
		System.err.println("[WARN- could not extract symbol, skipping line: " + line);
	    }
	    this.entrez2sym.put(entrez,symbol);
	    if (this.ensembl2EntrezGene.containsKey(ens)) {
		ArrayList<Integer> test = this.ensembl2EntrezGene.get(ens);
       		if (! test.contains(entrez)) {
		    //System.err.println("Error: ensembl peptide id: " + ens + " is associated with two different Entrez Gene IDs: " 
		    //+ entrez + " and " + test.get(0));
		    doubleEnt++;
		    test.add(entrez);
		}
	    } else {
		ArrayList<Integer> lst = new ArrayList<Integer>();
		lst.add(entrez);
		this.ensembl2EntrezGene.put(ens,lst);
	    }
	}
	System.out.println("[INFO] Total of " + noSym.size() + "entrezgene entries with no available gene symbol");
	System.out.println("[INFO] input a total of " + this.ensembl2EntrezGene.size() + " ensembl/entrez gene mappings");
	System.out.println("[INFO] No entrez gene id was available for " + n_noEntrez + " ensembl peptide ids");
	System.out.println("[INFO] There were " + doubleEnt + " cases of double entrez gene ids for some ensembl peptide id");
	
    }

}
/* eof */