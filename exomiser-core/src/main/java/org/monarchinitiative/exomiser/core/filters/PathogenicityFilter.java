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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.Objects;

/**
 * Filters variants according to their predicted pathogenicity.
 * <p>
 * When the keepNonPathogenic parameter is true, the class will apply the pathogenicity scoring, but no further
 * filtering will be applied, so all variants will pass irrespective of their score.
 * <p>
 * When keepNonPathogenic is false, the target parameter is used to determine which variant types will be filtered out.
 * If the target is set to ALL, the filter will consider both coding and non-coding variants. If set to NON_CODING,
 * only non-coding variants will be considered. The intention is that this filter is used to filter non-coding variants
 * in WGS samples (keepNonPathogenic=false, target=NON_CODING) whilst keeping all coding variants. For WES samples, it
 * should be safe to run (keepNonPathogenic=false, target=NON_CODING), but seeing as the non-coding variants are not
 * present, this will be the equivalent of (keepNonPathogenic=true). When used in pipelines which want to score and show
 * all variants, the keepNonPathogenic parameter should be set to true.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (29 December, 2012).
 */
public record PathogenicityFilter(@JsonProperty boolean keepNonPathogenic, Target target) implements VariantFilter {

    private static final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    public enum Target {
        ALL,
        NON_CODING
    }

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    public PathogenicityFilter {
        target = Objects.requireNonNullElse(target, Target.ALL);
    }

    public PathogenicityFilter(@JsonProperty boolean keepNonPathogenic) {
        this(keepNonPathogenic, Target.ALL);
    }

    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    @Override
    public FilterType filterType() {
        return filterType;
    }

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
            return switch (target) {
                case ALL, NON_CODING -> filterNonCoding(variantEvaluation.pathogenicityData());
            };
        }
        // coding variant
        return switch (target) {
            case NON_CODING -> PASS;
            case ALL -> variantEvaluation.isPredictedPathogenic() ? PASS : FAIL;
        };
    }

    private @NonNull FilterResult filterNonCoding(PathogenicityData pathogenicityData) {
        // CADD, REMM and SPLICE_AI are all optional. However, CADD 1.6+ is a general non-coding model which
        //  includes splice predictors, so check this first
        PathogenicityScore caddScore = pathogenicityData.pathogenicityScore(PathogenicitySource.CADD);
        if (caddScore != null && caddScore.rawScore() >= 15.0) {
            return PASS;
        }
        PathogenicityScore spliceAiScore = pathogenicityData.pathogenicityScore(PathogenicitySource.SPLICE_AI);
        if (spliceAiScore != null && spliceAiScore.score() > SpliceAiScore.NON_SPLICEOGENIC_SCORE) {
            return PASS;
        }
        PathogenicityScore remmScore = pathogenicityData.pathogenicityScore(PathogenicitySource.REMM);
        if (remmScore != null && remmScore.score() > RemmScore.LIKELY_PATHOGENIC_THRESHOLD) {
            return PASS;
        }
        return FAIL;
    }

    @Override
    public String toString() {
        return "PathogenicityFilter{" + "keepNonPathogenic=" + keepNonPathogenic + ", target=" + target + '}';
    }
}
