/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

/**
 * Semantic Similarity in HPO
 *
 * @author Sebastian Koehler
 * @version 0.05 (6 January, 2014).
 */
public class PhenixPriorityResult extends AbstractPriorityResult {

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
    public PhenixPriorityResult(int geneId, String geneSymbol, double negLogPVal) {
        super(PriorityType.PHENIX_PRIORITY, geneId, geneSymbol, negLogPVal);
        this.negativeLogPval = negLogPVal;
    }

    //TODO: negLogPVal - this isn't actually used - apart from in getHTMLCode where it is simply reversed.
    //TODO: NORMALIZATION_FACTOR is STATIC and will likely clash with other runs when used in a webserver.
    //TODO: calculate (hpoSemSimScore * NORMALIZATION_FACTOR) and use this directly.
    public PhenixPriorityResult(int geneId, String geneSymbol, double negLogPVal, double semScore) {
        super(PriorityType.PHENIX_PRIORITY, geneId, geneSymbol, semScore);
        this.negativeLogPval = negLogPVal;
        this.hpoSemSimScore = semScore;
    }

    /**
     * @return the HPO semantic similarity score calculated via Phenomizer.
     */
    @Override
    public double getScore() {
        return (hpoSemSimScore * NORMALIZATION_FACTOR);
    }

    /**
     */
    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>PhenIX semantic similarity score: %.2f (p-value: %f)</dt></dl>",
                this.hpoSemSimScore, Math.exp(-1 * this.negativeLogPval));
    }

}
