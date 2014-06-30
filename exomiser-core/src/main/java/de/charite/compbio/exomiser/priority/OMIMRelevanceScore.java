package de.charite.compbio.exomiser.priority;



import java.util.List;
import java.util.ArrayList;

/**
 * This class is used to annotate with OMIM data based on the link
 * between the entrez Gene and the OMIM data in the exomizer database table called
 * omim. There is no actual filtering out of variants.
 * @author Peter N Robinson
 * @version 0.08 (9 February, 2014).
 */
public class OMIMRelevanceScore implements GeneScore {
    /** A list of all diseases in OMIM that are associated with the gene affected by the variant. */
    private final List<String> mimEntryList;
    /** This score will be set to 1 if the variant distribution in the gene matches the
     * mode of inheritance of the disease in the HPO annotation data. For instance, if
     * the gene has a homozygous variant, than a disease with autosomal recessive inheritance
     * would get a score of 1.0. If the gene only has one het variant, the disease would get
     * a score of 0.5. 
     */
    private static final float DEFAULT_SCORE=-0.1f;

    private float score=DEFAULT_SCORE;

    /** @return true (Always, this Triage is not intended to filter out variants, only to annotate them) */
    public boolean passesFilter() { return true; }
    /** @return return  1.0 for true */
    public float filterResult() {return 1f;}
    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() { return null; }
    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public List<String> getFilterResultList()
	{ return this.mimEntryList; }

    /**
     * @return 1 if the inheritance pattern of the diseases associated with the gene match the variants, otherwise 0.5
     */
    @Override public float getScore(){
	if (this.score==DEFAULT_SCORE) 
	    return 1f;
	else
	    return this.score;
    }
    

    public boolean is_empty() { return this.mimEntryList.size()==0; }

    /**
     * This method adds one row to the list of OMIM diseases that will be shown in the 
     * HTML output. It also updates the score of this relevance object, which is set to
     * one if at least one of the diseases associated with the gene has a mode of inheritance
     * that is compatible with the distribution of variants observed in the gene in the VCF file.
     * @param phenmim The MIM number for the phenotype entry associated with the gene
     * @param genemim The MIM number for the gene entry in OMIM for this gene
     * @param disease Name of the disease in English
     * @param typ The type of the diseases (Mendelian, susceptibility, non-disease)
     * @param  inheritance One of 'D', 'R', 'B', 'X'. 'Y', 'M'
     * @param factor Factor for whether the inheritance pattern matches (see above).
     */
    public void addRow(String phenmim, String genemim, String disease,char typ,char inheritance, float factor){
	String mimGeneUrl = String.format("http://www.omim.org/entry/%s",genemim);
        String[] phenParts = phenmim.split(":");
	String mimPhenUrl = String.format("http://www.omim.org/entry/%s",phenParts[1]);
	String display = null;
	if (inheritance=='D') {
	    display = String.format("%s [%s; gene: %s], autosomal dominant",disease,phenmim,genemim);
	} else if (inheritance=='R') {
	    display = String.format("%s [%s; gene: %s], autosomal recessive",disease,phenmim,genemim);
	} else if (inheritance=='B') {
	    display = String.format("%s [%s; gene: %s], autosomal dominant/recessive",disease,phenmim,genemim);
	} else if (inheritance=='X') {
	    display = String.format("%s [%s; gene: %s], X chromosomal",disease,phenmim,genemim);
	} else
	    display = String.format("%s [%s; gene: %s]",disease,phenmim,genemim);
	//String href = String.format("<a href=\"%s\">%s</a>",mimPhenUrl,display);
	String href = String.format("<a href=\"%s\">%s</a>", mimPhenUrl,disease);
	if (factor > this.score) this.score=factor;

	if (typ=='D') {
	    mimEntryList.add(href);
	} else if (typ=='N') {
	    String row = String.format("%s (non-disease)",href);
	    mimEntryList.add(row);
	} else if (typ=='S') {
	    String row = String.format("%s (susceptibility)",href);
	    mimEntryList.add(row);
	} else if (typ=='?') {
	    String row = String.format("%s (unconfirmed)",href);
	    mimEntryList.add(row);
	} else if (typ=='C') {
	    String row = String.format("%s (CNV)",href);
	    mimEntryList.add(row);
	}
    }



    /**
     * @param orphanum The Number of the disease in Orphanet.
     * @param disease The name of the disease in English
     */
    public void addOrphanetRow(String orphanum, String disease) {
        String[] orphaParts = orphanum.split(":");
	String url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s",orphaParts[1]);
	String href = String.format("<a href=\"%s\">%s</a>",
				    url,disease);
	mimEntryList.add(href);
    }



    public OMIMRelevanceScore() {
	this.mimEntryList = new ArrayList<String>();
    }


    /**
     * @return A string with HTML code producing a bullet list of OMIM entries/links.
     */
    @Override public String getHTMLCode() {
	 if (is_empty()) return "<ul><li>No known disease</ul></li>";
	 StringBuilder sb = new StringBuilder();
         sb.append("<ul><li>Known diseases</li></ul>");
	 if (this.score<1f && this.score != DEFAULT_SCORE) {
	     sb.append("Observed variants not compatible with mode of inheritance:");
	 }
	 sb.append("<ul>\n");
	 for (String s: this.mimEntryList) {
	     sb.append("<li>" + s + "</li>\n");
	 }
	 sb.append("</ul>\n");
	 return sb.toString();
    }

     @Override public void resetScore(float newscore){ /* not implemented */ }

}