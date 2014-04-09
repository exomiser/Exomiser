package de.charite.compbio.exomiser.priority;



/**
 * 
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class GenewandererRelevanceScore implements IRelevanceScore {
    /**
     * The Random walk similarity score.
     */
    private double genewandererScore;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public GenewandererRelevanceScore(double score) {
	this.genewandererScore = score;
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
    public static GenewandererRelevanceScore noPPIDataScore() {
	float nodatascore = 0f;
	GenewandererRelevanceScore grs = new GenewandererRelevanceScore(nodatascore);
	return grs;
    }


    /** 
     * @return An HTML list with an entry representing the GeneWanderer (Random walk) similarity score.
     * @see exomizer.filter.ITriage#getHTMLCode()
     */
    @Override public String getHTMLCode() {
	return String.format("<ul><li>Random walk similarity score: %.3f</li></ul>",this.genewandererScore);
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