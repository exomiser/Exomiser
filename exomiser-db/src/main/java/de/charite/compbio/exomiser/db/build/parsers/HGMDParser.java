package de.charite.compbio.exomiser.db.build.parsers;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException; 
import java.util.HashMap;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parse files from the Human Gene Mutation Database. The profile has the following structure.
 * 
 * <ol>
 * <li>Disease Name (e.g., Hyperinsulinism )
 * <li>Gene symbol (e.g., ABCC8)
 * <li>Chromosome band (e.g., 11p15.1)
 * <li>Gene name (e.g., ATP-binding cassette, sub-family C (CFTR/MRP), member 8 (sulphonylurea receptor, SUR))
 * <li>591370 (seems to be a Human Genome Database ID; the HGD no longer exists)
 * <li>600509 (OMIM Gene id)
 * <li>Ala-Val (amino acid change)
 * <li>\N
 * <li>\N
 * <li>163 (amino acid position)
 * <li>163 (amino acid position)
 * <li>Ala163Val (protein mutation, 3 letter code)
 * <li>488C&gt;T (c.DNA mutation)
 * <li>488CtoT  (cDNA mutation in English words, 1 letter code)
 * <li>A163V (protein mutation)
 * <li>rs72466448 (dbSNP id)
 * <li>11 (chromosomal location of gene)
* <li>17415243 (start position of mutation)
* <li>17415243 (end position of mutation)
* <li>DM? (DM: disease-causing mutations; DP:disease-associated polymorphisms; DFP: disease-associated polymorphisms 
* , and FP: in vitro/laboratory or in vivo functional polymorphisms). 
* <li>Yorifuji (Name of first author)
* <li>J Clin Endocrinol Metab (journal abbreviation)
* <li>The Journal of clinical endocrinology and metabolism (journal full name)
* <li>96 (volume)
* <li>E141 (page)
* <li>2011 (year)
* <li>20943781 (pmid)
* <li>PRI (?)
* <li>\N
* <li>CS110251
* <li>2011-01-13
* <li>S
* </ol>
 * @version 0.04 (28 November, 2013)
 * @author Peter Robinson
 *
 */
public class HGMDParser {
    
    private static final Logger logger = LoggerFactory.getLogger(HGMDParser.class);
        
    /* Path to HGMD's allmut.txt pro file.
    private String hgmdProPath=null;
    /** FileWriter for the  hgmd-pro.pg "dump" file */
    private FileWriter fstream =null;
    /** BufferedWriter for the hgmd-pro.pg "dump" file */
    private BufferedWriter out =null;

    private int currentNumber;

    private HashMap<String,Integer> id2DiseaseMap=null;
    
    public HGMDParser() {
	this.id2DiseaseMap = new HashMap<String,Integer>();
	this.currentNumber = 1;
    }


    private void initializeOutFileHandle(String outname) throws IOException {
	this.fstream = new FileWriter(outname);
	this.out = new BufferedWriter(this.fstream);
    }


    private Integer getDiseaseGeneID(String diseasename, String genesym)   {
	String s = String.format("%s|%s",diseasename, genesym);
	Integer id = this.id2DiseaseMap.get(s);
	if (id == null) { /* We have not seen this string before */
	    id = new Integer(this.currentNumber);
	    this.currentNumber++;
	    this.id2DiseaseMap.put(s,id);
	}
	return id;
    }

    private void outputDiseaseGeneDumpFile() throws IOException {
	 String outname="hgmddisease.pg";
	 initializeOutFileHandle(outname);
	 Iterator<String> it = this.id2DiseaseMap.keySet().iterator();
	 while (it.hasNext()) {
	     String disease = it.next();
	     Integer id = this.id2DiseaseMap.get(disease);
	     int i = disease.indexOf("|");
	     if (i<0) {
		 logger.error("Could not parse HGMD disease/gene string: {}", disease);
		 System.exit(1);
	     }
	     String dis = disease.substring(0,i);
	     String gs = disease.substring(i+1);
	     String s = String.format("%d|%s|%s",id,dis,gs);
	     //System.out.println(s);
	     out.write(s + "\n");
	 }
	 this.out.close();

    }


    
    /** This function parses the HGMD Pro file and creates a "dump" file that can be used to
     * populate the postgreSQL database.
     * See the setup documentation for instructions on how to import the outputfile, which 
     * is called <b>hgmdpro.pg</b> to the database.
     * @param path Path to the allmut.txt file of HGMD.
     */
    public void parseHGMDProFile(String path) {
	logger.info("Parsing HGMD Pro File: {}", path);
	try{     
	    /* The infile */
	    FileInputStream fstr = new FileInputStream(path);
	    //DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstr));
	    String outname="hgmdpro.pg";
	    initializeOutFileHandle(outname);
	    String line = br.readLine();
	   
	    /* Skip up to the first Term stanza. */
	    String diseasename=null;
	    String gensym=null; /* Gene Symbol */
	    String cDNAmut=null; /* e.g., 461A>G */
	    String aaMut=null; /* e.g., Q154R */
	    String chr=null;
	    String start=null;
	    String end=null;
	    String pmid=null;
	    int c = 0;
	    int bad=0;
	    int no_pmid=0;
	    logger.info("Parsing HGMD Pro File ready {}", br.ready() );
	    while ((line = br.readLine()) != null)   {
		//System.out.println(line);
		String F[] = line.split("!");
		if (F.length<27) {
		    //System.out.println("Error parsing line with less than 30 fields:\n" + line + "\n\t=>Had only " + F.length + " fields");
		    bad++;
		    continue;
		}
		diseasename=F[0];
		gensym=F[1];
		cDNAmut=F[12];
		aaMut=F[11];
		chr=F[15];
		start=F[16];
		end=F[17];
		pmid=F[25];

                switch (chr) {
                    case "X":
                        chr = "23";
                        break;
                    case "Y":
                        chr = "24";
                        break;
                    case "M":
                        chr="25";
                        break;
                }
	
		Integer pubmedInt = null;
		try {
		    pubmedInt = Integer.parseInt(pmid);
		} catch (NumberFormatException e) {
		    //System.out.println("Difficulties parsing line:\n"+line);
		    //e.printStackTrace();
		    no_pmid++;
		    pmid="-1"; /* flag for no pmid given */
		}
		    
		if (diseasename==null || gensym == null) {
		    logger.error("Disease or gene sym null on line\n{}\n", line);
		    continue;
		}
		Integer id = getDiseaseGeneID(diseasename,gensym);
		String s = String.format("%d|%s|%s|%s|%s|%s",id,cDNAmut,aaMut,chr,start,pmid);
		//System.out.println(s);
		out.write(s + "\n");
		c++;
		if (c % 5000 == 0)
		    logger.info("Parsed {} HGMD mutations", c);
	    }
	    br.close();
	    this.out.close();
	    logger.info("Parsed {} mutations and skipped {} malformed lines, and did not find pmid for {} mutations", c, bad, no_pmid);
	    outputDiseaseGeneDumpFile();
	} catch (IOException e) {
	    logger.error("{}", e);
	    System.exit(1);
	}

    }



}