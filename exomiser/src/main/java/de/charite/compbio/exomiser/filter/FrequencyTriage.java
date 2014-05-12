package de.charite.compbio.exomiser.filter;


import java.util.List;
import java.util.ArrayList;

import jannovar.common.Constants;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;

/**
 * Filter Variants on the basis of Thousand Genomes and 5000 Exome project data.
 * The FrequencyTriage is created by the FrequencyFilter, one for each tested
 * variant. The FrequencyTriage object can be used to ask whether the variant
 * passes the filter, in this case whether it is rarer than then threshold in 
 * both the Thousand Genomes data and the Exome Server Project data. If no information
 * is available for either of these, the filter is not applied (ergo the Variant does not
 * fail the filter).
 * <P>
 * Note that the frequency data for Variants is expressed in percentage (not proportion).
 * @author Peter N Robinson
 * @version 0.05 (9 January,2013).
 */
public class FrequencyTriage implements Triage {
    /** Thousand Genomes allele count (all samples).*/
    private int dbSNPid;
    /** dbSNP GMAF (often from thousand genomes project).*/
    private float dbSNPmaf;
    /** Exome Server Project (ESP) European American MAF. */
    private float espEAmaf;
    /** Exome Server Project (ESP) African American MAF. */
    private float espAAmaf;
    /** Exome Server Project (ESP) all comers MAF. */
    private float espAllmaf;
   
    /** A threshold for the frequency filter. It needs to be set once by the FrequencyFilter class before
	filtering individual variants. It is initialized to 200% to let all variants pass if for some
    reason the client class does not set a value for it.*/
    private static float threshold = 200f;

    /**
     * A threshold for frequency of a variant is used to filter out variants with a higher 
     * population frequency.
     * @param t Threshold, in percent
     */
    public static void set_frequency_threshold(float t) throws ExomizerInitializationException { 
	if (t<0f || t>100f) {
	    throw new ExomizerInitializationException("Illegal value for frequency threshold:" + t);
	}
	FrequencyTriage.threshold = t; 
    }

    /**
     * @return true if this variant is at all represented in dbSNP or ESP data, regardless of
     * frequency. That is, if the variant has an RS id in dbSNP or any frequency data at all, 
     * return true, otherwise false.
     */
    public boolean representedInDatabase() {
	if (this.dbSNPid != Constants.UNINITIALIZED_INT) return true;
	if (this.dbSNPmaf > Constants.UNINITIALIZED_FLOAT) return true;
	else if (this.espAllmaf  > Constants.UNINITIALIZED_FLOAT) return true;
	else return false;
    }

    /**
     * @return true if this variant has frequency data from dbSNP
     */
    public boolean hasFrequencyDataFrom_dbSNP() { return this.dbSNPmaf > Constants.UNINITIALIZED_FLOAT; }
    
    /**
     * @return true if this variant has frequency data from the exome variant server (ESP)
     */
    public boolean hasFrequencyDataFromESP() { return this.espAllmaf  > Constants.UNINITIALIZED_FLOAT; }
    
    /**
     * @return true if this variant has a dbSNP "rs" id.
     */
    public boolean has_dbSNPrsID() { return this.dbSNPid > Constants.UNINITIALIZED_INT; }

    /**
     * @param dbsnp an "rs" id from dbSNP, in integer form
     * @param dbsnpmaf dbSNP GMAF (often from thousand genomes project)
     * @param EAmaf ESP European American MAF (as percent)
     * @param AAmaf ESP African  American MAF (as percent)
     * @param Allmaf ESP all comers MAF (as percent)
     */
    public FrequencyTriage(int dbsnp, float dbsnpmaf, float EAmaf, float AAmaf,float Allmaf) {
	this.dbSNPid =  dbsnp;
	this.dbSNPmaf =  dbsnpmaf;
	this.espEAmaf = EAmaf;
	this.espAAmaf = AAmaf;
	this.espAllmaf =  Allmaf;
    }

    /** If we have not data for the object, then we simply create a noData object
	that is initialized with flags such that we "know" that this object was not
	initialized. The purpose of this is so that we do not throuw away Variants if 
	there is no data about them in our database -- presumably, these are really rare.
    */
   public static FrequencyTriage  createNoDataTriageObject()
    {
	FrequencyTriage ft = new FrequencyTriage(Constants.UNINITIALIZED_INT,Constants.UNINITIALIZED_FLOAT,Constants.UNINITIALIZED_FLOAT,
						 Constants.UNINITIALIZED_FLOAT,Constants.UNINITIALIZED_FLOAT);
	return ft;
    }
    



    /** 
     * This method returns false if the variant is more common than the threshold
     * in any one of the dbSNP data, or the ESP data for European Americans,
     * African Americans, or All comers.
     * @return true if the variant being analyzed is rarer than the threshold
     */
    public boolean passesFilter() {
    if (dbSNPmaf > FrequencyTriage.threshold) return false;
	else if (espAllmaf > FrequencyTriage.threshold) return false;
	else if (espEAmaf > FrequencyTriage.threshold) return false;
	else if (espAAmaf > FrequencyTriage.threshold) return false;
	else return true;
    }

    /** 
     * This method returns a numerical value that is closer to one, the
     * rarer the variant is. If a variant is not entered in any of the
     * four data sources, it returns one (highest score).
     * Otherwise, it identifies the maximum MAF in any of the databases,
     * and returns a score that depends on the MAF.
     * Note that the frequency is expressed as a percentage.
     @return return a float representation of the filter result [0..1]. 
     If the result is boolean, return 0.0 for false and 1.0 for true 
    */
    @Override public float filterResult() {
	/*
	float max = Math.max(Math.max(this.dbSNPmaf,this.espAllmaf),Math.max(this.espEAmaf,this.espAAmaf));
	if (max <= 0) return 1f;
	else if (max > FrequencyTriage.threshold) return 0f;
	else if (max < 0.01) return 0.95f;
	else if (max < 0.1) return 0.9f;
	else if (max < 1f) return 0.8f;
	else if (max < 5f) return 0.7f;
	else return 0.1f;
	*/
	float max = Math.max(Math.max(this.dbSNPmaf,this.espAllmaf),Math.max(this.espEAmaf,this.espAAmaf));
	double maf = max;
        if (max <= 0) {
	    return 1f;
	}
	else if (max > 2) {
	    return 0f;
	}
	else{
	    return 1f - (0.13533f * (float) Math.exp(maf));
	}
    }
    
    /** @return A string with a summary of the filtering results (intended for HTML).*/
    public String getFilterResultSummary() {
	String rsID = ".";
	String dbSNPstring = null;
	StringBuilder sb = new StringBuilder();
	if (dbSNPid > Constants.UNINITIALIZED_INT) {
	    String url = String.format("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=%d",dbSNPid);
	    if (dbSNPmaf > Constants.UNINITIALIZED_FLOAT) 
		dbSNPstring = String.format("<a href=\"%s\" target=\"_new%s\">rs%s</a> MAF: %.4f",url,dbSNPid,dbSNPid,dbSNPmaf);
	    else
		dbSNPstring = String.format("<a href=\"%s\" target=\"_new%s\">rs%s</a>",url,dbSNPid,dbSNPid);
	    sb.append(dbSNPstring + "<br/>\n");
	} 
	if (this.espAllmaf > Constants.UNINITIALIZED_FLOAT) {
	    /* We will assume that there is data for all three ESP fields */
	    String espString = String.format("ESP: all %.3f%%, EA %.3f%%, AA %.3f%%<br/>\n",espAllmaf,espEAmaf,espAAmaf);
	    sb.append(espString);
	}
	if (sb.length() ==0) 
	    sb.append("No frequency data found<br/>\n");
	return sb.toString();
    }




    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public List<String> getFilterResultList() {
	List<String> L = new ArrayList<String>();
	String rsID = ".";
	String dbSNPstring = null;
	if (dbSNPid > Constants.UNINITIALIZED_INT) {
	    if (dbSNPmaf > Constants.UNINITIALIZED_FLOAT) 
		dbSNPstring = String.format("rs%s: %.4f",dbSNPid,dbSNPmaf);
	    else
		dbSNPstring = String.format("rs%s",dbSNPid);
	    L.add(dbSNPstring);
	} else if (dbSNPmaf > Constants.UNINITIALIZED_FLOAT) {
	    dbSNPstring = String.format("dbSNP: %.4f",dbSNPid,dbSNPmaf);
	    L.add(dbSNPstring);
	}
	if (this.espAllmaf > Constants.UNINITIALIZED_FLOAT) {
	    /* We will assume that there is data for all three ESP fields */
	    String espString = String.format("ESP: all %.4f, EA %.4f, AA %.4f",espAllmaf,espEAmaf,espAAmaf);
	    L.add(espString);
	}
	if (L.isEmpty()) 
	    L.add(".");
	return L;
    }
    
    /**
     * @return HTML code for the contents of the cell representing the frequency evaluation
     */
    @Override public String getHTMLCode() {
	StringBuilder sb = new StringBuilder();
	sb.append("<ul>\n");
	if (dbSNPid > 0) { // NOTE SOMEWHERE CODE IS ENTERING -1 instead of UNINITIALIZED_INT) {
	    String url=String.format("<a href=\"http://www.ncbi.nlm.nih.gov/snp/?term=rs%d\">rs%d</a>",
				     dbSNPid,dbSNPid);
	    if (dbSNPmaf > Constants.UNINITIALIZED_FLOAT) {
		String s = String.format("<li>%s: %.2f%%</li>",url,dbSNPmaf);
		sb.append(s);
	    } else {
		sb.append(String.format("<li>%s (no frequency data)</li>\n",url));
	    }
	} else {
	    sb.append("<li>No dbSNP entry found</li>\n");
	}
	if ( this.espAllmaf > Constants.UNINITIALIZED_FLOAT) {
	    /* We will assume that there is data for all three ESP fields */
	    String espString = String.format("ESP: <ul><li>all %.4f%%</li><li>EA %.4f%%</li><li>AA %.4f%%</li></ul>",espAllmaf,espEAmaf,espAAmaf);
	    sb.append(String.format("<li>%s</li>\n",espString));
	}
	sb.append("</ul>\n");
	return sb.toString();
    }
    
}