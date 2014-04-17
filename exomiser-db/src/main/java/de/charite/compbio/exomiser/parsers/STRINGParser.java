package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.*; 
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
public class STRINGParser implements Parser {


    private static final Logger logger = LoggerFactory.getLogger(EntrezParser.class);
    private HashMap<String,ArrayList<Integer>> ensembl2EntrezGene=null;

    
    private HashSet<Interaction> interactionSet = null;

    public STRINGParser(HashMap<String, ArrayList<Integer>> ensembl2EntrezGene) {
	this.ensembl2EntrezGene = ensembl2EntrezGene;
	this.interactionSet = new HashSet<Interaction>();
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
     * This function does the actual work of parsing the STRING file.
     *
     * @param inPath Complete path to string file.
     * @param outPath PAth where output file is to be written
     */
    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {
        try (FileReader fileReader = new FileReader(inPath);
                BufferedReader br = new BufferedReader(fileReader);
                FileWriter fileWriter = new FileWriter(new File(outPath));
                BufferedWriter writer = new BufferedWriter(fileWriter)) {   
	String header = br.readLine();	
	String line=null;
	while ((line = br.readLine()) != null)   {
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
		    if (! this.interactionSet.contains(ita)){
                        writer.write(String.format("%s|%s|%s", a, b, score));
                        writer.newLine();
			this.interactionSet.add(ita);
                    }
		}
	    }
	}
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FAILURE;
        }
        return ResourceOperationStatus.SUCCESS;
    }


    


}
/* eof */