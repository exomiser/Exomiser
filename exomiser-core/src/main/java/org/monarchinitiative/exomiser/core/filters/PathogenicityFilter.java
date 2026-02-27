/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.svart.VariantType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Filters variants according to their predicted pathogenicity.
 * <p>
 * The keepNonPathogenic parameter will apply the pathogenicity
 * scoring, but no further filtering will be applied so all variants will
 * pass irrespective of their score.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (29 December, 2012).
 */
public record PathogenicityFilter(@JsonProperty boolean keepNonPathogenic) implements VariantFilter {

    private static final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    @Override
    public FilterType filterType() {
        return filterType;
    }

    private static final Set<VariantEffect> INTRONIC_EFFECTS = EnumSet.of(VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.FIVE_PRIME_UTR_INTRON_VARIANT, VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT);
    /**
     * VariantFilter variants based on their calculated pathogenicity. Those
     * that pass have a pathogenicity score assigned to them. The failed ones
     * are deemed to be non-pathogenic and marked as such.
     */
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (keepNonPathogenic || variantEvaluation.isWhiteListed()) {
            return PASS;
        }
        if (variantEvaluation.isNonCodingVariant()) {
            PathogenicityData pathogenicityData = variantEvaluation.pathogenicityData();
            // CADD, REMM and SPLICE_AI are all optional. However, CADD 1.6+ is a general non-coding model which
            //  includes splice predictors, so check this first
            PathogenicityScore caddScore = pathogenicityData.pathogenicityScore(PathogenicitySource.CADD);
            if (caddScore != null && caddScore.rawScore() >= 15.0) {
                return PASS;
            }
            PathogenicityScore spliceAiScore = pathogenicityData.pathogenicityScore(PathogenicitySource.SPLICE_AI);
            if (INTRONIC_EFFECTS.contains(variantEvaluation.variantEffect()) && (spliceAiScore != null && spliceAiScore.score() > SpliceAiScore.NON_SPLICEOGENIC_SCORE)) {
                return PASS;
            }
            PathogenicityScore remmScore = pathogenicityData.pathogenicityScore(PathogenicitySource.REMM);
            if (remmScore != null && remmScore.score() > RemmScore.LIKELY_PATHOGENIC_THRESHOLD) {
                return PASS;
            }
            return FAIL;
        }
        // this should run after the non-coding check as otherwise lower quality REMM scores will be returned and
        // potential SpliceAI scores removed.
        if (variantEvaluation.isPredictedPathogenic()) {
            return PASS;
        }
        return FAIL;
    }

    @Override
    public String toString() {
        return "PathogenicityFilter{" + "keepNonPathogenic=" + keepNonPathogenic + '}';
    }
}
