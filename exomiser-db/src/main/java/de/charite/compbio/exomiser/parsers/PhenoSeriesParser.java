package de.charite.compbio.exomiser.parsers;

/** Command line functions from apache */
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException; 
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This class is intended to parse the information about the OMIM phenotypic
 * series and to enter it into the SQL database exomizer that will be used for the
 * larger exomizer project. Note that this program parses the output of the perl
 * scripts that are located in the "extraStuff" folder of the ExomeWalker project; see
 * the README file there for information on how to get all of that parsed. These scripts
 * are currently not very pretty, since the phenoseries information is delivered as an HTML
 * page with subtly inconsistent formatting, it requires some "hacks" to parse with a perl script.
 * <P>
 * This program is thus part of the ExomeWalker subproject of the Exomizer.
 * <P>
 * The format of the pheno2gene.txt file is as follows:
 * <PRE>
 * Warburg micro syndrome|600118|10p12.1|Warburg micro syndrome 3|3|614222|RAB18, WARBM3|602207|22931|RAB18
 * </PRE>
 * We are interested in the following fields.
 * <ol>
 * <li> Warburg micro syndrome: name of the phenoseries
 * <li> 600118: MIM Id of the main entry of the phenoseries, we will use it as a PRIMARY KEY
 * <li> 10p12.1:  Cytoband of the specific disease entry
 * <li> Warburg micro syndrome 3: Name of the specific disease entry
 * <li> 3: OMIM class of the specific disease entry (3 means gene has been mapped)
 * <li> 614222: MIM Id (phenotype) of the  specific disease entry
 * <li> RAB18, WARBM3: gene symbol and synonyms of the   specific disease entry
 * <li> 602207: MIM Id (gene) of the specific disease entry
 * <li> 22931: Entrez Gene Id of the specific disease entry's gene
 * <li> Gene symbol of the Entrez Gene entry (should match one of the items in field 7).
 * </ol>
 * @author Peter Robinson
 * @version 0.05 (17 November, 2013)
 *
 */
public class PhenoSeriesParser {

    private static final Logger logger = LoggerFactory.getLogger(PhenoSeriesParser.class);


    /** Handle to the SQL database (postgreSQL) */
    private Connection connect = null;
    /** An SQL statement we will use to create the database tables. */
    private Statement statement = null;
    /**
     * If true, output a file with all of the phenotypic series. This file can be used to validate
     * the performance of the methods.
     */
    private boolean outputPhenoseriesFile = false;
    /**
     * Name of the output file with all of the phenotypic series. 
     */
    private String outfilename = "phenoseries.tab";


    /**
     * Path to file created by Perl scripts in ExomeWalker/extraStuff directory.
     * The file is called pheno2gene.txt, see the README in that directory for information.
     */
    private String phenoseriesPath = null;

    
    /** The constructor parses the command-line arguments to 
	 get the path to the phenoseries file. */
    public PhenoSeriesParser(Connection conn, String path){
	this.connect = conn;
	this.phenoseriesPath = path;
    }
    



    public boolean outputPhenoFile() { return this.outputPhenoseriesFile; }

    private void insertIntoPhenoseries(PreparedStatement pst, String seriesID, String name)
	throws SQLException
    {
	Integer i = Integer.parseInt(seriesID);
	pst.setInt(1,i);
	pst.setString(2,name);
	pst.executeUpdate();
    }


    /**
     * This is the main function of this program, and it adds diseases to the database.
     * Note that OMIM has some entries such as 61206
     * <ul>
     * <li>Frontotemporal lobar degeneration, TARDBP-related 	
     * <li>Amyotrophic lateral sclerosis 10, with or without FTD 
     * </ul>
     * These entries have the same OMIM phenoID and the same OMIM gene, and are in a sense
     * duplicates, more or less two subversions of the same disease. To keep things
     * simple, we take only one of these entries, and we discard the rest. We do this using
     * the HashSet series2diseaseSeen.
     */
    public void parsePhenoSeries() {
	HashSet<String> seen = new HashSet<String>();
	HashSet<String> series2diseaseSeen = new HashSet<String>();
	try{     
	    String insert = "INSERT INTO phenoseries (seriesID,name) VALUES(?,?);";
	    PreparedStatement instPS = this.connect.prepareStatement(insert);
	    String insert2 = "INSERT INTO omim2gene(mimDiseaseID, mimDiseaseName,cytoBand,mimGeneID,entrezGeneID,geneSymbol,seriesID) "+
		"VALUES(?,?,?,?,?,?,?);";
	    PreparedStatement insO2G = this.connect.prepareStatement(insert2);
	    
	    FileInputStream fstream = new FileInputStream(this.phenoseriesPath);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line;
	  
	    
	    while ((line = br.readLine()) != null)   {
		System.out.println(line);
		String A[] = line.split("\\|");
		if (A.length != 10) {
		    debugPrint(line,A);
		}
		String seriesName= A[0];
		String seriesID = A[1];
		String cytoBand = A[2];
		String mimDiseaseName = A[3];
		String mimDiseaseID = A[5];
		String mimGeneID = A[7];
		String entrezGeneID = A[8];
		String geneSymbol = A[9];

		if (entrezGeneID.startsWith("?"))
		    continue; // No gene for this entry

		String combi = String.format("%s-%s",seriesID,mimDiseaseID);
		if (series2diseaseSeen.contains(combi)) {
		    continue;
		} else {
		    series2diseaseSeen.add(combi);
		}

		Integer entrID = Integer.parseInt(entrezGeneID);
		Integer mGeneID = Integer.parseInt(mimGeneID);
		Integer mDiseaseID = Integer.parseInt(mimDiseaseID);
		Integer sID = Integer.parseInt(seriesID);

		insO2G.setInt(1,mDiseaseID);
		insO2G.setString(2,mimDiseaseName);
		insO2G.setString(3,cytoBand);
		insO2G.setInt(4,mGeneID);
		insO2G.setInt(5,entrID);
		insO2G.setString(6,geneSymbol);
		insO2G.setInt(7,sID);

		System.out.println("XX:" + insO2G);

		insO2G.executeUpdate();
	
		String ky = String.format("%s-%s",seriesID,seriesName);
		if (! seen.contains(ky)) {
		    seen.add(ky);
		    insertIntoPhenoseries(instPS, seriesID, seriesName);
		}
	    }
	    br.close();
	    instPS.close();
	    insO2G.close();

	} catch (IOException e){
	    System.err.println("[PhenoSeriesImporter] I/O error encountered while trying to parse the pheno2gene.txt file at:");
	    System.err.println("[PhenoSeriesImporter]\t\"" + this.phenoseriesPath + "\"");
	    System.err.println("[PhenoSeriesImporter]\t" + e.getMessage());
	    System.exit(1);
	} catch (SQLException e) {
            System.err.println("SQL Exception");
	    System.err.println(e);
	    System.exit(1);
	} 
    }


    /**
     * This method prints out the arguments for debugging purposes.
     */
    private void debugPrint(String L, String A[]) {
	System.err.println("Malformed line");
	System.err.println(L);
	for (int i=0;i<A.length;++i) {
	    System.out.println(String.format("%d) %s",i+1,A[i]));
	}
	System.exit(1);
    }


    /**
     * This method can be used to write a file with all phenotypic
     * series. This file can be used for the validation of the 
     * Exome Walker method. The file has the following structure:
     *<pre>
     * bla
     *</pre>
     * This method should be used after the data has been entered into the database.
     * It should be called as
     * <pre>
     * $java -jar PhenoSeriesImporter -Z [-f filename]
     * </pre>
     * Note that the default file name is phenoseries.tab
     */
    public void printTableOfPhenotypicSeries() {
	ArrayList<Integer> idList = new ArrayList<Integer>();

	String select = "SELECT seriesID FROM phenoseries;";
	System.out.println(select);
	try {
	    Statement st  = connect.createStatement();
	    ResultSet rs = st.executeQuery(select);
	    while( rs.next() ) {
		int id = rs.getInt(1);
		idList.add(id);
		System.out.println(id);
	    }
	    st.close();
	} catch (SQLException e) {
            System.err.println("problems selecting from phenoseries");
	    System.err.println(e);
	    System.exit(1);
	}


	select = "SELECT entrezGeneID FROM phenoseries P,omim2gene O "+
	    "WHERE P.seriesID = O.seriesID and P.seriesID = ?";
	String selectName = "select name FROM phenoseries WHERE seriesID = ?";

	try {
	    FileWriter fstream = new FileWriter(this.outfilename);
    	    BufferedWriter out = new BufferedWriter(fstream);
	    PreparedStatement pst = connect.prepareStatement(select);
	    PreparedStatement namePst = connect.prepareStatement(selectName);
	    for (Integer pid : idList) {
		namePst.setInt(1,pid);
		ResultSet rsName = namePst.executeQuery();
		String phenoseriesname=null;
		while (rsName.next()) {
		    phenoseriesname = rsName.getString(1);
		}
	

		pst.setInt(1, pid);
		ResultSet rs = pst.executeQuery();
		HashSet<Integer> entrezSet = new HashSet<Integer>(); 
		/* Note there are some duplicate genes because of 
		   the fact that there are multiple phenotype entries
		   for some genes. We only want a unique list of gene ids,
		   which is why we use a HashSet here. */
	
		while( rs.next() ) {
		    int id = rs.getInt(1);
		    entrezSet.add(id);
		}
		
		String entrezStr = myjoin(entrezSet);
		out.write(pid + ":" + phenoseriesname + ":" + entrezStr + "\n");
	    } /* end for loop */
	    pst.close();
	    namePst.close();
	    out.close();
	} catch (SQLException e) {
            System.err.println("problems selecting from omim2gene");
	    System.err.println(e);
	    System.exit(1);
	} catch (IOException ioe) {
	    System.err.println("problems writing to phenoseries file");
	    System.err.println(ioe);
	    System.exit(1);
	}
    }

    /**
     * @param st Set of integers
     * @return joined version of set joined by comma similar to perl function "join"
     */
    private String myjoin(HashSet<Integer> st) {
	StringBuffer sb = new StringBuffer();
	int i = 0;
	Iterator<Integer> it = st.iterator();
	while (it.hasNext()) {
	    Integer x = it.next();
	    if (i>0)
		sb.append(",");
	    sb.append(x);
	    i++;
	}
	return sb.toString();

    }





}