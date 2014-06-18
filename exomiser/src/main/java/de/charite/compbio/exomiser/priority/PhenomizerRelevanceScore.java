package de.charite.compbio.exomiser.priority;



import java.util.List;

/**
 * Semantic Similarity in HPO
 * @author Sebastian Koehler
 * @version 0.05 (6 January, 2014).
 */
public class PhenomizerRelevanceScore implements GeneScore {
    /** The semantic similarity score as implemented in Phenomizer. Note that this is not the 
     * p-value methodology in that paper, but merely the simple semantic similarity score.
     */
    private double hpoSemSimScore;
    /**
     * The negative logarithm of the p-value. e.g., 10 means p=10^{-10}
     */
    private double negativeLogPval;


    static double NORMALIZATION_FACTOR = 1f;


    public static void setNormalizationFactor(double factor) {
	NORMALIZATION_FACTOR = factor;
    }

    
    /**
     * @param negLogPVal The negative logarithm of the p-val
     */
    public PhenomizerRelevanceScore(double negLogPVal) {
	this.negativeLogPval = negLogPVal;
    }



    public  PhenomizerRelevanceScore(double negLogPVal, double semScore) {
	this.negativeLogPval = negLogPVal;
	this.hpoSemSimScore = semScore;
	 String s = String.format("Semantic similarity score: %.2f (neg. log of p-value: %.2f)",
			     this.hpoSemSimScore,this.negativeLogPval);
	 //System.out.println(s);
    }
    
    
    /** 
     * @see exomizer.priority.IRelevanceScore#getRelevanceScore
     * @return the HPO semantic similarity score calculated via Phenomizer.
     */
    @Override public float getScore() {
	return (float)(hpoSemSimScore*NORMALIZATION_FACTOR);
    }
    
    /** 
     * @see exomizer.filter.Triage#getHTMLCode()
     */
    @Override 
    public String getHTMLCode() {
	return String.format("<ul><li>Phenomizer: Semantic similarity score: %.2f (p-value: %f)</li></ul>",
			     this.hpoSemSimScore,Math.exp(-1*this.negativeLogPval));
    }
    
    /** This method not needed for this derived class. */
    @Override public void resetScore(float newscore){ /* not implemented */ }


    @Override public String getFilterResultSummary() {
	return String.format("Phenomizer semantic similarity score: %.2f [neg. log. p value: %.2f]", 
			     this.hpoSemSimScore,this.negativeLogPval);
    }
    
    /** @return A list with detailed results of filtering. Not yet implemented for gene wanderer. */
    @Override public List<String> getFilterResultList(){
	return null;
    }

}