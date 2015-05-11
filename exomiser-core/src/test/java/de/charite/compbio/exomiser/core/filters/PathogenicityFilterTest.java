/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.exomiser.core.model.VariantEvaluation.VariantBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityFilterTest {

    private PathogenicityFilter instance;

    private static final boolean PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS = false;
    private static final boolean PASS_ALL_VARIANTS = true;

    VariantEvaluation missensePassesFilter;
    VariantEvaluation downstreamFailsFilter;
    VariantEvaluation stopGainPassesFilter;
    VariantEvaluation predictedNonPathogenicMissense;

    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = new SiftScore(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = new SiftScore(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = new PolyPhenScore(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = new PolyPhenScore(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = new MutationTasterScore(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = new MutationTasterScore(MTASTER_FAIL_SCORE);

    @Before
    public void setUp() {

        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        // make the variant evaluations
        missensePassesFilter = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .pathogenicityData(new PathogenicityData(null, null, SIFT_PASS, null))
                .build();

        predictedNonPathogenicMissense = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .pathogenicityData(new PathogenicityData(POLYPHEN_FAIL, null, null, null))
                .build();

        downstreamFailsFilter = testVariantBuilder()
                .variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT)
                .pathogenicityData(new PathogenicityData(null, null, null, null))
                .build();

        stopGainPassesFilter = testVariantBuilder()
                .variantEffect(VariantEffect.STOP_GAINED)
                .pathogenicityData(new PathogenicityData(null, null, null, null))
                .build();
    }

    private VariantBuilder testVariantBuilder() {
        return new VariantBuilder(1, 1, "A", "T");
    }
    
    @Test
    public void testThatOffTargetNonPathogenicVariantsAreStillScoredAndFailFilterWhenPassAllVariantsSetFalse() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        float expectedScore = VariantTypePathogenicityScores.getPathogenicityScoreOf(downstreamFailsFilter.getVariantEffect());

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
        assertThat(filterResult.getScore(), equalTo(expectedScore));
    }

    @Test
    public void testThatOffTargetNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetTrue() {
        instance = new PathogenicityFilter(PASS_ALL_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        float expectedScore = VariantTypePathogenicityScores.getPathogenicityScoreOf(downstreamFailsFilter.getVariantEffect());

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
        assertThat(filterResult.getScore(), equalTo(expectedScore));

    }

    @Test
    public void testThatMissenseNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetTrue() {
        instance = new PathogenicityFilter(PASS_ALL_VARIANTS);

        FilterResult filterResult = instance.runFilter(predictedNonPathogenicMissense);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testThatMissenseNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetFalse() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        FilterResult filterResult = instance.runFilter(predictedNonPathogenicMissense);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    public void testDefaultMissenseVariantIsPredictedPathogenicIsTrue() {
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        assertThat(instance.variantIsPredictedPathogenic(type), is(true));
    }

    @Test
    public void testStopGainVariantIsPredictedPathogenicIsTrue() {
        VariantEffect type = VariantEffect.STOP_GAINED;
        assertThat(instance.variantIsPredictedPathogenic(type), is(true));
    }

    @Test
    public void testDownstreamVariantIsPredictedPathogenicIsFalse() {
        VariantEffect type = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        assertThat(instance.variantIsPredictedPathogenic(type), is(false));
    }

    @Test
    public void testCalculateScoreDownstream() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantEffect type = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissenseDefault() {
        PathogenicityData pathData = new PathogenicityData(null, null, null, null);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissenseSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_FAIL, MTASTER_FAIL, SIFT_PASS, null);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        float expected = 1 - SIFT_PASS.getScore();
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenAndSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_FAIL, SIFT_PASS, null);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        float expected = 1 - SIFT_PASS.getScore();
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenSiftAndMutTasterPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_PASS, null);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        float expected = MTASTER_PASS.getScore();
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testToString() {
        String expResult = "Pathogenicity filter: removePathFilterCutOff=false";
        String result = instance.toString();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testEqualToOtherPathogenicityFilter() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(false);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualToOtherPathogenicityFilter() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(true);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualToOtherFilterType() {
        instance = new PathogenicityFilter(false);
        Filter other = new FrequencyFilter(0.1f, true);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualToObjectOfDifferentType() {
        Object other = "a string";
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualToNullObject() {
        Object other = null;
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testHashCode() {
        instance = new PathogenicityFilter(false);
        PathogenicityFilter other = new PathogenicityFilter(false);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

}
