package de.charite.compbio.exomiser.filter;

import java.util.List;
import java.util.ArrayList;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;


/**
 * Filter Variants on the basis of the PHRED quality score for the variant that
 * was derived from the VCF file (QUAL field).
 * @author Peter N Robinson
 * @version 0.09 (18 December, 2013).
 */
public class QualityTriage implements ITriage {
    /** The PHRED quality value for the current variant call. */
    private float quality;
    /** A threshold for the quality filter. It can be set by the QualityFilter class before
	filtering individual variants.*/
    private static float threshold = 1f;

    /**
     * Minimum number of reads supporting the ALT call. There must be at least this number of
     * reads in each direction. 
     */
    private static int minReadThreshold = 0;

    public static void set_frequency_threshold(float t) throws ExomizerInitializationException { 
	if (t<0f ) {
	    throw new ExomizerInitializationException("Illegal value for quality threshold:" + t);
	}
	QualityTriage.threshold = t; 
    }

    /**
     * @param qual a PHRED variant-call quality score.
     */
    public QualityTriage(float qual) {
	this.quality = qual;
    }

    /** 
     * See the documentation for the entire class. We are no longer filtering by requiring
     * a minimum niumber of reads for each DP4 field (alt/ref in both directions). Instead,
     * we are just filtering on the overall PHRED variant call quality.
     * @return true if the variant being analyzed passes the filter (e.g., has high quality ). 
     */
    public boolean passesFilter() {
	return this.quality >= QualityTriage.threshold; 
    }

    /** 
     * We have implemented this in a boolean fashion. If a variant is above the
     * the threshold for PHRED variant score, return 1, otherwise return 0.
     * @return return a float representation of the filter result [0..1]. 
     */
    @Override public float filterResult() { if (passesFilter()) { return 1.0f; } else { return 0f; } }

    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() {
	//return String.format("Quality: %.1f",quality);
	StringBuilder sb = new StringBuilder();
	sb.append(String.format("Quality: %.1f",quality));
	
	return sb.toString();
    }


    /**
     * @return HTML code to display the PHRED score for the variant call as a bullet point.
     */
     @Override  public String getHTMLCode() {
	 return String.format("<UL><LI>PHRED: %d</LI></UL>\n",(int)quality);
     }

    /**
     * This was removed because the DP4 field is too rarely used in VCF files and many
     * simply do not have thhis data.
     ield is too rarely used in VCF files and many
     * simply do not have thhis data.
    private String getDP4TableAsHTML() {
	StringBuilder sb = new StringBuilder();
	sb.append("<table id=\"qy\"><tr><th>read</th><th>&rarr;</th><th>&larr;</th></tr>\n");
	sb.append(String.format("<tr><td>ref</td><td>%d</td><td>%d</td></tr>\n",
				DP4[N_REF_FORWARD_BASES],DP4[N_REF_REVERSE_BASES]));
	sb.append(String.format("<tr><td>alt</td><td>%d</td><td>%d</td></tr>\n",
				DP4[N_ALT_FORWARD_BASES],DP4[N_ALT_REVERSE_BASES]));
	sb.append("</table>\n");
	return sb.toString();
    }
    */


    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired.
     null should be interpreted as "detailed list not available or sensible".*/
    public List<String> getFilterResultList()
    {
	List<String> L = new ArrayList<String>();
	L.add( String.format("%d",(int)this.quality) );
	
	return L;
    }



}