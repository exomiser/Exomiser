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
     * This is call for genes with no PPI data; they are assigned a score of
     * zero. They will be assigned a score equivalent to the median of all genes
     * by the function {@code prioritize_listofgenes} in
     * {@link exomizer.priority.IPriority IPriority}. basically as a kind of
     * uniform prior.
     */
    public static HiPhivePriorityResult noPPIDataScore() {
        float nodatascore = 0f;
        HiPhivePriorityResult grs = new HiPhivePriorityResult(nodatascore, "", "", "", "", 0, 0, 0, 0);
        return grs;
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

    /**
     * @return HTML describing the phenotype evidence for the match
     */
    public String getHumanPhenotypeEvidence() {
        return this.humanPhenotypeEvidence;
    }

    /**
     * @return HTML describing the phenotype evidence for the match
     */
    public String getMousePhenotypeEvidence() {
        return this.mousePhenotypeEvidence;
    }

    /**
     * @return HTML describing the phenotype evidence for the match
     */
    public String getFishPhenotypeEvidence() {
        return this.fishPhenotypeEvidence;
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

    /**
     * @return A list with detailed results of filtering. Not yet implemented
     * for gene wanderer.
     */
    @Override
    public List<String> getFilterResultList() {
        return new ArrayList<>();
    }
}
