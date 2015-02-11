package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

/**
 * Semantic Similarity in HPO
 *
 * @author Sebastian Koehler
 * @version 0.05 (6 January, 2014).
 */
public class PhenixPriorityResult implements PriorityResult {

    /**
     * The semantic similarity score as implemented in PhenIX (also know as
     * Phenomizer). Note that this is not the p-value methodology in that paper,
     * but merely the simple semantic similarity score.
     */
    private double hpoSemSimScore;
    /**
     * The negative logarithm of the p-value. e.g., 10 means p=10^{-10}
     */
    private final double negativeLogPval;

    private static double NORMALIZATION_FACTOR = 1f;

    public static void setNormalizationFactor(double factor) {
        NORMALIZATION_FACTOR = factor;
    }

    /**
     * @param negLogPVal The negative logarithm of the p-val
     */
    public PhenixPriorityResult(double negLogPVal) {
        this.negativeLogPval = negLogPVal;
    }

    public PhenixPriorityResult(double negLogPVal, double semScore) {
        this.negativeLogPval = negLogPVal;
        this.hpoSemSimScore = semScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.PHENIX_PRIORITY;
    }

    /**
     * @see exomizer.priority.IRelevanceScore#getRelevanceScore
     * @return the HPO semantic similarity score calculated via Phenomizer.
     */
    @Override
    public float getScore() {
        return (float) (hpoSemSimScore * NORMALIZATION_FACTOR);
    }

    /**
     * @see exomizer.filter.Triage#getHTMLCode()
     */
    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>PhenIX semantic similarity score: %.2f (p-value: %f)</dt></dl>",
                this.hpoSemSimScore, Math.exp(-1 * this.negativeLogPval));
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
