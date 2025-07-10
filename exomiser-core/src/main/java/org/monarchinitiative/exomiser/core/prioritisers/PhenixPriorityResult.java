/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

import java.util.Objects;

/**
 * Semantic Similarity in HPO
 *
 * @param hpoSemSimScore The semantic similarity score as implemented in PhenIX (also known as Phenomizer). Note
 *                       that this is not the p-value methodology in that paper, but merely the simple
 *                       semantic similarity score.
 * @param negativeLogP   The negative logarithm of the p-value. e.g., 10 means p=10^{-10}
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Jules Jacobsen
 * @version 0.05 (6 January, 2014).
 */
public record PhenixPriorityResult(int geneId, String geneSymbol, double score, double hpoSemSimScore,
                                   double negativeLogP) implements PriorityResult {

    public PhenixPriorityResult {
        Objects.requireNonNull(geneSymbol);
    }

    @Override
    public PriorityType priorityType() {
        return PriorityType.PHENIX_PRIORITY;
    }

    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>PhenIX semantic similarity score: %.2f (p-value: %f)</dt></dl>",
                this.hpoSemSimScore, Math.exp(-1 * this.negativeLogP));
    }

    @Override
    public String toString() {
        return "PhenixPriorityResult{" +
               "geneId=" + geneId +
               ", geneSymbol='" + geneSymbol + '\'' +
               ", score=" + score +
               ", hpoSemSimScore=" + hpoSemSimScore +
               ", negativeLogP=" + negativeLogP +
               '}';
    }
}
