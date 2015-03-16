package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class HiPhivePriorityResult implements PriorityResult {
    /**
     * The Random walk similarity score.
     */
    private double score;
    private final double humanScore;
    private final double fishScore;
    private final double mouseScore;
    private final double walkerScore;
    private final String evidence;
    private final String humanPhenotypeEvidence;
    private final String mousePhenotypeEvidence;
    private final String fishPhenotypeEvidence;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public HiPhivePriorityResult(double score, String evidence, String humanPhenotypeEvidence,
            String mousePhenotypeEvidence, String fishPhenotypeEvidence, double humanScore, double mouseScore, double fishScore, double walkerScore) {
        this.score = score;
        this.evidence = evidence;
        this.humanPhenotypeEvidence = humanPhenotypeEvidence;
        this.mousePhenotypeEvidence = mousePhenotypeEvidence;
        this.fishPhenotypeEvidence = fishPhenotypeEvidence;
        this.humanScore = humanScore;
        this.mouseScore = mouseScore;
        this.fishScore = fishScore;
        this.walkerScore = walkerScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.HI_PHIVE_PRIORITY;
    }

    /**
     * @see exomizer.priority.IRelevanceScore#getRelevanceScore
     */
    @Override
    public float getScore() {
        return (float) score;
    }

    /**
     * @return An HTML list with an entry representing the GeneWanderer (Random
     * walk) similarity score.
     * @see exomizer.filter.ITriage#getHTMLCode()
     */
    @Override
    public String getHTMLCode() {
        //return String.format("<ul><li>Similarity score: %.3f %s</li></ul>",this.genewandererScore,this.evidence);
        return this.evidence;
    }

    public float getHumanScore() {
        return (float) this.humanScore;
    }

    public float getMouseScore() {
        return (float) this.mouseScore;
    }

    public float getFishScore() {
        return (float) this.fishScore;
    }

    public float getWalkerScore() {
        return (float) this.walkerScore;
    }

    /**
     * Resets the value of the relevance score to a number between 0 and 1 The
     * scores resulting from random walk analysis are renormalized such that the
     * highest score is equal to 1 and the lowest score to 0. The
     * renormalization is performed by {@link exomizer.priority.GenewandererPriority GenewandererPriority}
     *
     * @param newscore new value for relevance score
     */
    public void setScore(float newscore) {
        this.score = newscore;
    }

}
