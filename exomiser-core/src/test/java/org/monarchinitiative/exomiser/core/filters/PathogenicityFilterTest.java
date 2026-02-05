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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class PathogenicityFilterTest {

    private PathogenicityFilter instance;

    private static final boolean PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS = false;
    private static final boolean PASS_ALL_VARIANTS = true;

    private VariantEvaluation missensePassesFilter;
    private VariantEvaluation downstreamFailsFilter;
    private VariantEvaluation stopGainPassesFilter;
    private VariantEvaluation predictedNonPathogenicMissense;

    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = SiftScore.of(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = SiftScore.of(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = PolyPhenScore.of(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = PolyPhenScore.of(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = MutationTasterScore.of(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = MutationTasterScore.of(MTASTER_FAIL_SCORE);

    @BeforeEach
    void setUp() {

        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        // make the variant evaluations
        missensePassesFilter = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .pathogenicityData(PathogenicityData.of(SIFT_PASS))
                .build();

        predictedNonPathogenicMissense = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .pathogenicityData(PathogenicityData.of(POLYPHEN_FAIL))
                .build();

        downstreamFailsFilter = testVariantBuilder()
                .variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT)
                .pathogenicityData(PathogenicityData.empty())
                .build();

        stopGainPassesFilter = testVariantBuilder()
                .variantEffect(VariantEffect.STOP_GAINED)
                .pathogenicityData(PathogenicityData.empty())
                .build();
    }

    private VariantEvaluation.Builder testVariantBuilder() {
        return TestFactory.variantBuilder(1, 1, "A", "T");
    }

    @Test
    void test() {
        assertThat(instance.keepNonPathogenic(), equalTo(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS));
    }

    @Test
    void testThatOffTargetNonPathogenicVariantsAreStillScoredAndFailFilterWhenPassAllVariantsSetFalse() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    void testThatOffTargetNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetTrue() {
        instance = new PathogenicityFilter(PASS_ALL_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    void testThatMissenseNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetTrue() {
        instance = new PathogenicityFilter(PASS_ALL_VARIANTS);

        FilterResult filterResult = instance.runFilter(predictedNonPathogenicMissense);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    void testThatMissenseNonPathogenicVariantsAreStillScoredAndFailFilterWhenPassAllVariantsSetFalse() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        FilterResult filterResult = instance.runFilter(predictedNonPathogenicMissense);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    void alwaysPassesWhiteListedVariant() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);
        // under normal circumstances, this would fail
        FilterTestHelper.assertFailed(instance.runFilter(downstreamFailsFilter));

        // however the user has set this variant as whitelisted so it should pass regardless
        downstreamFailsFilter.setWhiteListed(true);
        FilterTestHelper.assertPassed(instance.runFilter(downstreamFailsFilter));
    }

    @ParameterizedTest
    @CsvSource({
            "DOWNSTREAM_GENE_VARIANT, false, '', FAIL",
            "DOWNSTREAM_GENE_VARIANT, true, '', PASS",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, '', FAIL",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, SPLICE_AI=0.1, FAIL",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, SPLICE_AI=0.11, PASS",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, CADD=12;SPLICE_AI=0.11, PASS",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, CADD=12, FAIL",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, CADD=15, PASS",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, REMM=0.9;SPLICE_AI=0.11, PASS",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, REMM=0.9, FAIL",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, REMM=0.901, PASS",
            "THREE_PRIME_UTR_EXON_VARIANT, false, CADD=12;REMM=0.901, PASS",
            "THREE_PRIME_UTR_INTRON_VARIANT, false, CADD=12;REMM=0.901, PASS",
            "NON_CODING_TRANSCRIPT_INTRON_VARIANT, false, CADD=12;REMM=0.901, PASS",
            "MISSENSE_VARIANT, false, CADD=12;REVEL=0.8, PASS",
            "MISSENSE_VARIANT, false, REVEL=0.1, FAIL",
            "MISSENSE_VARIANT, false, '', PASS",
            "SYNONYMOUS_VARIANT, false, '', FAIL",
            "SYNONYMOUS_VARIANT, false, SPLICE_AI=0.2, FAIL", // spliceAI permissive
            "SYNONYMOUS_VARIANT, false, SPLICE_AI=0.5, PASS", // spliceAI default
            "SPLICE_REGION_VARIANT, false, SPLICE_AI=0.2, PASS", // splice region variants have a default path score of 0.8
            "SPLICE_REGION_VARIANT, false, '', PASS", // splice region variants have a default path score of 0.8
            "FRAMESHIFT_TRUNCATION, false, '', PASS",
    })
    void whenFilterSetToRemoveNonPathogenic(VariantEffect variantEffect, boolean isWhitelisted, String pathScoreStrings, FilterResult.Status expected) {
        PathogenicityFilter filter = new PathogenicityFilter(false);

        var variant = testVariantBuilder()
                .variantEffect(variantEffect)
                .whiteListed(isWhitelisted)
                .pathogenicityData(PathogenicityData.of(parsePathScores(pathScoreStrings)))
                .build();
        assertThat(filter.runFilter(variant).status(), equalTo(expected));
    }

    private List<PathogenicityScore> parsePathScores(String pathScoreStrings) {
        if (pathScoreStrings.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(pathScoreStrings.split(";"))
                .map(scoreValue -> {
                    String[] tokens = scoreValue.split("=");
                    return PathogenicityScore.of(PathogenicitySource.valueOf(tokens[0]), Float.parseFloat(tokens[1]));
                })
                .toList();
    }

    @Test
    void testFilterType() {
        assertThat(instance.filterType(), equalTo(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    void testToString() {
        String expResult = "PathogenicityFilter{keepNonPathogenic=false}";
        String result = instance.toString();
        assertThat(result, equalTo(expResult));
    }

    @Test
    void testEqualToOtherPathogenicityFilter() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(false);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    void testNotEqualToOtherPathogenicityFilter() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(true);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    void testNotEqualToOtherFilterType() {
        instance = new PathogenicityFilter(false);
        Filter other = new FrequencyFilter(0.1f);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    void testNotEqualToObjectOfDifferentType() {
        Object other = "a string";
        assertThat(instance.equals(other), is(false));
    }

    @Test
    void testNotEqualToNullObject() {
        Object other = null;
        assertThat(instance.equals(other), is(false));
    }

    @Test
    void testHashCode() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(false);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

}
