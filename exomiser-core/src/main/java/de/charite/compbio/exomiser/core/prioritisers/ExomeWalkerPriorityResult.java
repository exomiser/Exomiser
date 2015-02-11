package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class ExomeWalkerPriorityResult implements PriorityResult {

    /**
     * The Random walk similarity score.
     */
    private double score;
    private double rawScore;
    private double scaledByMaxScore = -10;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public ExomeWalkerPriorityResult(double score) {
        this.score = score;
        this.rawScore = score;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.EXOMEWALKER_PRIORITY;
    }

    /**
     * 
     */
    @Override
    public float getScore() {
        return (float) score;
    }

    /**
     * This is call for genes with no PPI data; they are assigned a score of
     * zero. They will be assigned a score equivalent to the median of all genes
     * by the function {@code prioritize_listofgenes} in
     * {@link exomizer.priority.Priority Priority}. basically as a kind of
     * uniform prior.
     */
    public static ExomeWalkerPriorityResult noPPIDataScore() {
        float nodatascore = 0f;
        ExomeWalkerPriorityResult grs = new ExomeWalkerPriorityResult(nodatascore);
        return grs;
    }

    /**
     * @return An HTML list with an entry representing the GeneWanderer (Random
     * walk) similarity score.
     * 
     */
    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>Random walk similarity score: %.3f</dt></dl>", this.score);
    }

    public double getRawScore() {
        return this.rawScore;
    }

    public double getScaledScore() {
        return this.scaledByMaxScore;
    }

    /**
     * Resets the value of the relevance score to a number between 0 and 1 The
     * scores resulting from random walk analysis are renormalized such that the
     * highest score is equal to 1 and the lowest score to 0. The
     * renormalization is performed by
     * {@link exomizer.priority.GenewandererPriority GenewandererPriority}
     *
     * @param newscore new value for relevance score
     */
    public void setScore(float newscore) {
        this.score = newscore;
        if (this.scaledByMaxScore == -10) {
            this.scaledByMaxScore = newscore;
        }
    }

    /**
     * @return A list with detailed results of filtering. Not yet implemented
     * for gene wanderer.
     */
    @Override
    public List<String> getFilterResultList() {
        return new ArrayList<>();
    }

}
