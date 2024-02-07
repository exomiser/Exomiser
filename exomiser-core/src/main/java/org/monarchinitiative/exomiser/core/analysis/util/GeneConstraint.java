/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import java.util.Objects;

// pLI (probability of being loss-of-function intolerant)
// pLI scores closer to one indicate more intolerance to protein-truncating variation. For a set of transcripts
// intolerant of protein-truncating variation, we suggest pLI ≥ 0.9.

// Observed / expected (oe)
// The constraint score shown in gnomAD is the ratio of the observed / expected (oe) number of loss-of-function variants in that gene.
// The expected counts are based on a mutational model that takes sequence context, coverage and methylation into account.
// Interpretation
// Observed/expected (oe) is a continuous measure of how tolerant a gene is to a certain class of variation (e.g. loss-of-function).
// When a gene has a low oe value, it is under stronger selection for that class of variation than a gene with a higher value.
// Because counts depend on gene size and sample size, the precision of the oe values varies a lot from one gene to the next.
// Therefore in addition to the oe value, we also display the 90% confidence interval (CI) for each of the oe values.
// When evaluating how constrained a gene is, it is essential to take the 90% CI into consideration.
// Although oe is a continuous value, we understand that it can be useful to use a threshold for certain applications.
// In particular, for the interpretation of Mendelian diseases cases, we suggest using the upper bound of the oe CI < 0.35 as a threshold if needed.
// Again, ideally oe should be used as a continuous value rather than a cutoff and evaluating the oe 90% CI is a must.
/**
 * @since 13.1.0
 */
public record GeneConstraint(String geneSymbol, String transcriptId, double pLI, double loeuf, double loeufLower, double loeufUpper, double missenseZ, double synonymousZ) {

    /**
     * For the interpretation of Mendelian diseases cases, we suggest using the upper bound of the oe CI < 0.35 as a threshold if needed.
     *
     * @return true if the loeufUpper is less than 0.35
     */
    public boolean isLossOfFunctionIntolerant() {
        //gnomAD suggest using a loeufUpper < 0.35;
        // However varsome suggests a relaxed metric has better recall due to lower FP.
        // https://varsome.com/about/resources/acmg-implementation/#pvs1
        return loeuf < 0.7555;
    }

}
