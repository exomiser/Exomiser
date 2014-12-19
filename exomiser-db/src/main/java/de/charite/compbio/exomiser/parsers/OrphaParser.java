package de.charite.compbio.exomiser.parsers;


import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException; 
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;



/**
 * Class to parse two files that are needed to gather information about Orphanet disease entries.
 * The first file is downloaded from the Orphadata website, en_product1.xml, an XML file
 * that has a list of Orphanet disease entries with the OrphaNumbers and names. The second comes
 * from our website and has the link between Orphanumbers and EntrezGene ids,
 * ORPHANET_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt.
 * For now, this class is still untested and preliminary TODO
 * @author Peter Robinson
 * @version 0.02 (8 February, 2014)
 */
@Deprecated
public class OrphaParser { //extends PhenotypeAnnotationParser {
     /** Handle to the SQL database (postgreSQL) */
    private Connection connection = null;
    /** An SQL statement we will use to create the database tables. */
    private Statement statement = null;
    /** Key: The Orphanumber. Valuee: Diseas name. */
    private HashMap<Integer,String> orphadata=null;

      /** Key: MIM id for a disease, Value: inheritance mode (see variable inheritanceMode in the
	inner class MIM). */
    private HashMap<Integer,ArrayList<String>> orpha2inherit=null;
    /**

    /*
     * @param c Connection to the Exomiser database.
     */
    public OrphaParser(Connection c,String hpoPath) {
//	super(hpoPath);
	this.connection = c;
	this.orphadata = new HashMap<Integer,String>();
//	parseHPOAnnotationFile("ORPHANET");
    }


    /**
     * Input the file
     * from the HPO Hudson server website
     * Typical line looks like:
     * <pre>
     * ORPHANET:1328	TGFB1	7040	HP:0002240	Hepatomegaly
     * </pre>
     * Note some lines are non-Orphanet, we skip them.
     * We are basically just interested in the Orphanet ID to Entrez Gene link, this is
     * easier to get from Sebastian Koehler's file than parsing the original Orphadata
     * and is also guaranteed to match the contents of the Phenomizer db in Exomizer.
     */
    public void inputALL_FREQUENCIES(String path) {
	/* Note that the combination of orphanumber and entrez gene number may be used multiple
	   times is there are multiple phenotypes for some disease. Just store one copy each using a set. */
	HashSet<String> orphaEntrezComb = new HashSet<String>();
	try {
	    FileInputStream fstream = new FileInputStream(path);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line;
	    
	  
	     while ((line = br.readLine()) != null)   {
		 Integer orphaNum = null;
		 Integer entrezID = null;
		 if (! line.startsWith("ORPHANET"))
		     continue;
		 //System.out.println(line);
		 String split[] = line.split("\t");
		 if (split.length<4) {
		     System.err.println("Error: malformed line in ALL_FREQUENCIES: " + line);
		     continue;
		 }
		 /* Note that we parse to Integers to ensure valid format, even though we convert back to
		    string to save only unique combinations of OrphaNumbers/Entrez Gene IDs. */
		 try {
		     String num = split[0].substring(9); // skip the word ORPHANET and the semicolon
		     orphaNum = Integer.parseInt(num);
		 } catch (NumberFormatException e) {
		     System.err.println("Could not parse Orphanumber from line " 
					+ line + "; " + e.getMessage());
		     continue;
		 }
		 try {
		     entrezID =  Integer.parseInt(split[2]);
		 }  catch (NumberFormatException e) {
		     System.err.println("Could not parse EntrezGene from line " 
					+ line + "; " + e.getMessage());
		     continue;
		 }
		
		 String comb = String.format("%d:%d",orphaNum,entrezID);
		 orphaEntrezComb.add(comb);
	     }
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	if (orphaEntrezComb.size()==0) {
	    System.err.println("Error, no Orphadata was accumulated");
	    System.exit(1);
	}
	enterToDatabase(orphaEntrezComb);
    }



    private void enterToDatabase(HashSet<String> orphaEntrezComb) {
	try {
	    String insert = "INSERT INTO orphanet(orphanumber, entrezGeneID, diseasename) "+
		"VALUES(?,?,?);";
	    PreparedStatement ins = this.connection.prepareStatement(insert);
	    for (String s : orphaEntrezComb) {
		String split[] = s.split(":");
		if (split.length != 2) {
		    System.err.println("Bad format for orphaEntrezComb: " + s);
		    System.exit(1); /* should never happen, but if so, we need to check code. */
		}
		Integer orphaNumber = Integer.parseInt(split[0]);
		Integer entrezGeneID = Integer.parseInt(split[1]);
		String name = this.orphadata.get(orphaNumber);
		if (name == null)
		    name = "could not retrieve Orphanet disease name";
		ins.setInt(1,orphaNumber);
		ins.setInt(2,entrezGeneID);
		ins.setString(3,name);
//		String inh =  getInheritanceCode(orphaNumber);
//		System.out.println(ins + " got: " + inh);
		ins.executeUpdate();
		
	    }
	} catch (SQLException e) {
	    System.err.println("Error inserting orphanet data into Exomiser database");
	    System.err.println(e.getMessage());
	    System.exit(1);

	}
    }





    /**
     * We only want to extract a small part of the data from this XML file, and 
     * we do not use a true XML parser. We need simple the OrphaNumber and the disease name.
     * The Disorder id is irrelevant, but we can use this a a flag to know we are starting a new entry.
     * <pre>
     * <Disorder id="840">
     * <OrphaNumber>2372</OrphaNumber>
     * <ExpertLink lang="en">http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&amp;Expert=2372</ExpertLink>
     * <Name lang="en">Laryngocele</Name>
     * @param path Path to en_product1.xml
     */
    public void inputOrphadataXML(String path) {
	try {
	    FileInputStream fstream = new FileInputStream(path);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line;
	    Integer orphaNum = null;
	    String name = null;
	    while ((line = br.readLine()) != null)   {
		//System.out.println(line);
		line = line.trim();
		if (line.startsWith("<Disorder")) {
		    if (orphaNum != null && name != null) {
			this.orphadata.put(orphaNum,name);
			System.out.println("Putting " + orphaNum + ", " + name);
		    }
		    orphaNum = null;
		    name = null; // reset
		} else if (line.startsWith("<OrphaNumber>")) {
		    int i = line.indexOf("<",13);// skip to after start tag
		    if (i<0) continue;
		    String num = line.substring(13,i);
		    try {
			orphaNum = Integer.parseInt(num);
		    } catch (NumberFormatException e) {
			System.err.println("Could not parse Orphanumber from line " 
					   + line + "; " + e.getMessage());
			continue;
		    }
		} else if (line.startsWith("<Name")) {
		    int i = line.indexOf(">");
		    if (i<0) continue;
		    i++; // skip the >
		    int j = line.indexOf("<",i);
		    if (j<i) continue;
		    name = line.substring(i,j);
		    if (orphaNum.equals(821))
			System.out.println("GGGG Got 821 for name=" + name);
		}
	    }
	    // get very last one!
	    if (orphaNum != null && name != null)
		this.orphadata.put(orphaNum,name);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }


}