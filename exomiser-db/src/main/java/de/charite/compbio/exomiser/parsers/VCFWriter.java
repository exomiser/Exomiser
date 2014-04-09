package de.charite.compbio.exomiser.parsers;


import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to write out a filter VCF file.
 * It writes out a list of variants by taking the original VCF lines
 * and it add onto the end an item representing the SIFT score.
 * This is very bad code and needs to be refactored by improving the VCF
 * reader and by exporting in a more flexibel way.
 */

public class VCFWriter {

    private static final Logger logger = LoggerFactory.getLogger(VCFWriter.class);

    private String outfile=null;
    private ArrayList<String> header=null;

    public VCFWriter(String outfilename) {
	this.outfile = outfilename;
    }

    public void set_header(ArrayList<String> h) { this.header = h; }



    /*   
    public void writefile(ArrayList<NSFP> hits) {
			    

	System.out.println("Output results to file \"" + outfile + "\"");
	try{
 
	    FileWriter fstream = new FileWriter(this.outfile);
	    BufferedWriter out = new BufferedWriter(fstream);
	    if (hits == null) {
		System.err.println("Error: got empty list of NSFP Hits!");
		return;
	    }
	    if (header != null) {
		Iterator<String> header_iter =  header.iterator();
		while (header_iter.hasNext()) {
		    String L = header_iter.next();
		    if (L.startsWith("##CHROM")) {
	 		StringBuilder sb = new StringBuilder();
			sb.append(L);
			sb.append("\tSIFT");
			out.write(sb + "\n");
		    } else {
			out.write(L + "\n");
		    }
		}
	    }

	    Iterator<NSFP> it = hits.iterator();
	    while (it.hasNext()) {
		NSFP n = it.next();
		String sift = n.get_SIFT_score_as_String();
		String vcfline = n.get_SNV_annotation();
		out.write(vcfline +  "\t" + sift + "\n");
	    }
	    out.close();
	}catch (IOException e){
	    System.err.println("Error: " + e.getMessage());
	}
    }

    */





}
