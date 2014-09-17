/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.filter.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.PathogenicityFilterScore;
import de.charite.compbio.exomiser.core.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
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

    PathogenicityFilter instance;

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
    VariantEvaluation missenseFailsFilter;

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
        
        instance = new PathogenicityFilter(false);

        //make the variant evaluations
        missensePassesFilter = new VariantEvaluation(mockMissensePassesFilterVariant);
        PathogenicityData missensePassPathData = new PathogenicityData(null, null, SIFT_PASS, null);
        missensePassesFilter.setPathogenicityData(missensePassPathData);

        missenseFailsFilter = new VariantEvaluation(mockMissenseFailsFilterVariant);
        PathogenicityData missenseFailPathData = new PathogenicityData(POLYPHEN_FAIL, null, null, null);
        missenseFailsFilter.setPathogenicityData(missenseFailPathData);
        
        downstreamFailsFilter = new VariantEvaluation(mockNonPathogenicVariant);
        PathogenicityData downstreamPathData = new PathogenicityData(null, null, null, null);
        downstreamFailsFilter.setPathogenicityData(downstreamPathData);

        stopGainPassesFilter = new VariantEvaluation(mockPathogenicNonMissense);
        PathogenicityData stopGainPathData = new PathogenicityData(null, null, null, null);
        stopGainPassesFilter.setPathogenicityData(stopGainPathData);

    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    public void testDefaultMissenseVariantPassesFilter() {
        PathogenicityData pathData = new PathogenicityData(null, null, null, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(true));
    }

    @Test
    public void testSiftPassesFilter() {
        PathogenicityData pathData = new PathogenicityData(null, null, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(true));
    }

    @Test
    public void testSiftFailsFilter() {
        PathogenicityData pathData = new PathogenicityData(null, null, SIFT_FAIL, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(false));
    }

    @Test
    public void testPolyPhenPassesFilter() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, null, null, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(true));
    }

    @Test
    public void testPolyPhenFailsFilter() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_FAIL, null, null, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(false));
    }

    @Test
    public void testMutationTasterPassesFilter() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(true));
    }

    @Test
    public void testMutationTasterFailsFilter() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_FAIL, null, null);
        VariantType type = VariantType.MISSENSE;
        assertThat(instance.passesFilter(type, pathData), is(false));
    }

    @Test
    public void testStopGainPassesFilter() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantType type = VariantType.STOPGAIN;
        assertThat(instance.passesFilter(type, pathData), is(true));
    }

    @Test
    public void testDownstreamFailsFilter() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantType type = VariantType.DOWNSTREAM;
        assertThat(instance.passesFilter(type, pathData), is(false));
    }

    @Test
    public void testCalculateScoreDownstream() {
        PathogenicityData pathData = new PathogenicityData(null, MTASTER_PASS, null, null);
        VariantType type = VariantType.DOWNSTREAM;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        FilterScore expectedScore = new PathogenicityFilterScore(expected);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expectedScore));
    }

    @Test
    public void testCalculateScoreMissenseDefault() {
        PathogenicityData pathData = new PathogenicityData(null, null, null, null);
        VariantType type = VariantType.MISSENSE;
        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        FilterScore expectedScore = new PathogenicityFilterScore(expected);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expectedScore));
    }

    @Test
    public void testCalculateScoreMissenseSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_FAIL, MTASTER_FAIL, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        float expected = 1 - SIFT_PASS.getScore();
        FilterScore expectedScore = new PathogenicityFilterScore(expected);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expectedScore));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenAndSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_FAIL, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        float expected = 1 - SIFT_PASS.getScore();
        FilterScore expectedScore = new PathogenicityFilterScore(expected);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expectedScore));
    }

    @Test
    public void testCalculateScoreMissensePolyPhenSiftAndMutTasterPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_PASS, null);
        VariantType type = VariantType.MISSENSE;
        float expected = MTASTER_PASS.getScore();
        FilterScore expectedScore = new PathogenicityFilterScore(expected);
        assertThat(instance.calculateFilterScore(type, pathData), equalTo(expectedScore));
    }

    @Test
    public void testFilterVariants() {
        List<VariantEvaluation> variantList = new ArrayList<>();
        variantList.add(downstreamFailsFilter);
        variantList.add(missensePassesFilter);
        variantList.add(stopGainPassesFilter);
        variantList.add(missenseFailsFilter);

        instance.filter(variantList);

        Set failedFilterSet = EnumSet.of(FilterType.PATHOGENICITY_FILTER);

        assertThat(downstreamFailsFilter.passedFilters(), is(false));
        assertThat(downstreamFailsFilter.getFailedFilterTypes(), equalTo(failedFilterSet));

        assertThat(missensePassesFilter.passedFilters(), is(true));
        assertThat(missensePassesFilter.getFailedFilterTypes().isEmpty(), is(true));

        assertThat(stopGainPassesFilter.passedFilters(), is(true));
        assertThat(stopGainPassesFilter.getFailedFilterTypes().isEmpty(), is(true));

        assertThat(missenseFailsFilter.passedFilters(), is(false));
        assertThat(missenseFailsFilter.getFailedFilterTypes(), equalTo(failedFilterSet));
    }

    @Test
    public void testToString() {
        String expResult = "Pathogenicity filter: keepNonPathogenicMissense=false";
        String result = instance.toString();
        assertThat(result, equalTo(expResult));
    }

}
