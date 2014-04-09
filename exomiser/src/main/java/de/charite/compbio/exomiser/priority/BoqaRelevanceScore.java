package de.charite.compbio.exomiser.priority;



/**
 * This class is used to classify genes based on BOQA analysis.
 * @author Peter N Robinson
 * @version 0.03 (9 September,2013).
 */
public class BoqaRelevanceScore  implements IRelevanceScore {

    
    /** Name of the disease that was associated with the best score for this gene. */
    private String disease=null;
    /** Posterior probability (according to BOQA) for the disease to explain the symptoms. */
    private float posteriorProbability;
    
    
    /** @return true (Always, this Triage is not intended to filter out variants, only to annotate them) */
    public boolean passesFilter() { return true; }
    /** @return return  1.0 for true */
    public float filterResult() {return this.posteriorProbability;}
    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() { return null; }
    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public java.util.ArrayList<String> getFilterResultList()
	{ return null; }

    /**
     * Return the relevance score.
     *
     * @return the relevance score.
     */
    @Override public float getRelevanceScore(){
	return getPosteriorProbability();
    }

    /**
     * Return the posterior probability.
     *
     * @return The posterior probability of the disease given the phenotype according to BOQA
     */
    public float getPosteriorProbability() {
        return posteriorProbability;
    }

    /**
     * Set the posterior probability.
     */
    public void setPosteriorProbability(float prob) {
        posteriorProbability = prob;
    }

    /**
     * @param prob The posterior probability according to BOQA
     * @param disease Name of the disease (six-digit OMIM id)
     */
    public BoqaRelevanceScore(double prob, String disease) {
	this.posteriorProbability = (float) prob;
	this.disease = disease;
    }

    /**
     * @param id a six-digit number representing an OMIM entry
     * @return An HTML anchor with the corresponding OMIM URL.
     */
    private String getOMIMurl(String id) {
	if (id==null) 
	    return ".";
	else 
	    return String.format("<a href=\"http://omim.org/entry/%s\">MIM:%s</a>",id,id);
    }

      /**
     * @return A string with HTML code producing a bullet list of OMIM entries/links.
     */
    @Override public String getHTMLCode() {
	String url = getOMIMurl(this.disease);
	return String.format("<ul><li>BOQA posterior probability: %.3f (%s)</li></ul>",
			     this.posteriorProbability, url);
    }

    @Override public void resetRelevanceScore(float newscore){ /* not implemented */ }





}