/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.filters.Filter;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class PathogenicityFilterTest {

    private PathogenicityFilter instance;

    private static final boolean PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS = false;
    private static final boolean PASS_ALL_VARIANTS = true;

    @Mock
    Variant mockNonPathogenicVariant;
    @Mock
    Variant mockPathogenicNonMissense;
    @Mock
    Variant mockMissensePassesFilterVariant;
    @Mock
    Variant mockMissenseFailsFilterVariant;

    VariantEvaluation downstreamFailsFilter;
    VariantEvaluation stopGainPassesFilter;
    VariantEvaluation missensePassesFilter;
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

    public PathogenicityFilterTest() {

    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        //set-up the methods to mock-out having to construct mentally heavy Variant objects just to get the variant type
        Mockito.when(mockMissensePassesFilterVariant.getVariantTypeConstant()).thenReturn(VariantType.MISSENSE);
        Mockito.when(mockMissenseFailsFilterVariant.getVariantTypeConstant()).thenReturn(VariantType.MISSENSE);
        Mockito.when(mockNonPathogenicVariant.getVariantTypeConstant()).thenReturn(VariantType.DOWNSTREAM);
        Mockito.when(mockPathogenicNonMissense.getVariantTypeConstant()).thenReturn(VariantType.STOPGAIN);

        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        //make the variant evaluations
        missensePassesFilter = new VariantEvaluation(mockMissensePassesFilterVariant);
        PathogenicityData missensePassPathData = new PathogenicityData(null, null, SIFT_PASS, null);
        missensePassesFilter.setPathogenicityData(missensePassPathData);

        predictedNonPathogenicMissense = new VariantEvaluation(mockMissenseFailsFilterVariant);
        PathogenicityData missenseFailPathData = new PathogenicityData(POLYPHEN_FAIL, null, null, null);
        predictedNonPathogenicMissense.setPathogenicityData(missenseFailPathData);

        downstreamFailsFilter = new VariantEvaluation(mockNonPathogenicVariant);
        PathogenicityData downstreamPathData = new PathogenicityData(null, null, null, null);
        downstreamFailsFilter.setPathogenicityData(downstreamPathData);

        stopGainPassesFilter = new VariantEvaluation(mockPathogenicNonMissense);
        PathogenicityData stopGainPathData = new PathogenicityData(null, null, null, null);
        stopGainPassesFilter.setPathogenicityData(stopGainPathData);

    }

    @Test
    public void testThatOffTargetNonPathogenicVariantsAreStillScoredAndFailFilterWhenPassAllVariantsSetFalse() {
        instance = new PathogenicityFilter(PASS_ONLY_PATHOGENIC_AND_MISSENSE_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        float expectedScore = VariantTypePathogenicityScores.getPathogenicityScoreOf(downstreamFailsFilter.getVariantType());

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
        assertThat(filterResult.getScore(), equalTo(expectedScore));
    }

    @Test
    public void testThatOffTargetNonPathogenicVariantsAreStillScoredAndPassFilterWhenPassAllVariantsSetTrue() {
        instance = new PathogenicityFilter(PASS_ALL_VARIANTS);

        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);

        float expectedScore = VariantTypePathogenicityScores.getPathogenicityScoreOf(downstreamFailsFilter.getVariantType());

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
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.variantIsPredictedPathogenic(type), is(true));
    }

    @Test
    public void testStopGainVariantIsPredictedPathogenicIsTrue() {
        VariantType type = VariantType.STOPGAIN;
        assertThat(instance.variantIsPredictedPathogenic(type), is(true));
    }

    @Test
    public void testDownstreamVariantIsPredictedPathogenicIsFalse() {
        VariantType type = VariantType.DOWNSTREAM;
        assertThat(instance.variantIsPredictedPathogenic(type), is(false));
    }

    @Test
    public void testCalculateScoreDownstream() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantType type = VariantType.DOWNSTREAM;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissenseDefault() {
        PathogenicityData pathData = new PathogenicityData(null, null, null, null);
        VariantType type = VariantType.MISSENSE;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissenseSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_FAIL, MTASTER_FAIL, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        //float expected = 1 - SIFT_PASS.getScore();
        float expected = 0.97f;//default for no CADD
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenAndSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_FAIL, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        //float expected = 1 - SIFT_PASS.getScore();
        float expected = 0.97f;//default for no CADD
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expected));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenSiftAndMutTasterPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        //float expected = MTASTER_PASS.getScore();
        float expected = 0.97f;//default for no CADD
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
