package de.charite.compbio.exomiser.priority;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class GenewandererPriorityScore implements PriorityScore {
    /**
     * The Random walk similarity score.
     */
    private double genewandererScore;
    private double rawScore;
    private double scaledByMaxScore = -10;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public GenewandererPriorityScore(double score) {
	this.genewandererScore = score;
        this.rawScore = score;
    }



    /** 
     * @see exomizer.priority.IRelevanceScore#getRelevanceScore
     */
    @Override public float getScore() {
	return (float)genewandererScore;
    }

  
    /**
     * This is call for genes with no PPI data; they are assigned a score of zero.
     * They will be assigned a score equivalent to the median of all genes by
     * the function {@code prioritize_listofgenes} in 
     * {@link exomizer.priority.IPriority IPriority}.
     * basically as a kind of uniform prior.
     */
    public static GenewandererPriorityScore noPPIDataScore() {
	float nodatascore = 0f;
	GenewandererPriorityScore grs = new GenewandererPriorityScore(nodatascore);
	return grs;
    }


    /** 
     * @return An HTML list with an entry representing the GeneWanderer (Random walk) similarity score.
     * @see exomizer.filter.Triage#getHTMLCode()
     */
    @Override public String getHTMLCode() {
	return String.format("<ul><li>Random walk similarity score: %.3f</li></ul>",this.genewandererScore);
    }

    public double getRawScore() {
        return this.rawScore;
    }
    
    public double getScaledScore() {
        return this.scaledByMaxScore;
    }
    
    /**
     * Resets the value of the relevance score to a number between 0 and 1
     * The scores resulting from random walk analysis are renormalized such that the
     * highest score is equal to 1 and the lowest score to 0. The renormalization is
     * performed by {@link exomizer.priority.GenewandererPriority GenewandererPriority}
     * @param newscore new value for relevance score
     */
    public void setScore(float newscore){
	this.genewandererScore = newscore;
        if (this.scaledByMaxScore == -10){
            this.scaledByMaxScore = newscore;
        }
    }


    /** @return A list with detailed results of filtering. Not yet implemented for gene wanderer. */
    @Override 
    public List<String> getFilterResultList(){
	return new ArrayList<>();
    }


}