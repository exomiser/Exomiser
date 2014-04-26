package de.charite.compbio.exomiser.priority;



import jannovar.common.Constants;
/**
 * 
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class DynamicPhenoWandererRelevanceScore implements IRelevanceScore,Constants  {
    /**
     * The Random walk similarity score.
     */
    private double genewandererScore;
    private String evidence;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public DynamicPhenoWandererRelevanceScore(double score, String evidence) {
	this.genewandererScore = score;
        this.evidence = evidence;
    }



    /** 
     * @see exomizer.priority.IRelevanceScore#getRelevanceScore
     */
    @Override public float getRelevanceScore() {
	return (float)genewandererScore;
    }

  
    /**
     * This is call for genes with no PPI data; they are assigned a score of zero.
     * They will be assigned a score equivalent to the median of all genes by
     * the function {@code prioritize_listofgenes} in 
     * {@link exomizer.priority.IPriority IPriority}.
     * basically as a kind of uniform prior.
     */
    public static DynamicPhenoWandererRelevanceScore noPPIDataScore() {
	float nodatascore = 0f;
	DynamicPhenoWandererRelevanceScore grs = new DynamicPhenoWandererRelevanceScore(nodatascore,"");
	return grs;
    }


    /** 
     * @return An HTML list with an entry representing the GeneWanderer (Random walk) similarity score.
     * @see exomizer.filter.ITriage#getHTMLCode()
     */
    @Override public String getHTMLCode() {
        //return String.format("<ul><li>Similarity score: %.3f %s</li></ul>",this.genewandererScore,this.evidence);
        return this.evidence;
    }

    
    
    /**
     * Resets the value of the relevance score to a number between 0 and 1
     * The scores resulting from random walk analysis are renormalized such that the
     * highest score is equal to 1 and the lowest score to 0. The renormalization is
     * performed by {@link exomizer.priority.GenewandererPriority GenewandererPriority}
     * @param newscore new value for relevance score
     */
    public void resetRelevanceScore(float newscore){
	this.genewandererScore = newscore;
    }


    @Override public String getFilterResultSummary() {
	return String.format("Random walk score: %.2f", this.genewandererScore);
    }
    /** @return A list with detailed results of filtering. Not yet implemented for gene wanderer. */
    @Override public java.util.ArrayList<String> getFilterResultList(){
	return null;
    }


}