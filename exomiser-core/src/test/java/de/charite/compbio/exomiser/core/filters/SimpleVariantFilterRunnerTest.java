/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner.VariantFilterRunner;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleVariantFilterRunnerTest {

    //Frequency run data
    @Mock
    private FrequencyFilter frequencyFilter;
    //Quality run data
    @Mock
    private QualityFilter qualityFilter;
    //Target run data
    @Mock
    private TargetFilter targetFilter;

    private VariantEvaluation passesAllFilters;
    private VariantEvaluation failsAllFilters;
    private VariantEvaluation passesQualityFrequencyFilter;
    private VariantEvaluation passesTargetQualityFilter;

    @Mock
    private FrequencyDao frequencyDao;

    @Before
    public void setUp() {

        Mockito.when(frequencyDao.getFrequencyData(null)).thenReturn(null);

        passesAllFilters = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        failsAllFilters = new VariantEvaluation.VariantBuilder(1, 2, "A", "T").build();
        passesQualityFrequencyFilter = new VariantEvaluation.VariantBuilder(1, 3, "A", "T").build();
        passesTargetQualityFilter = new VariantEvaluation.VariantBuilder(1, 4, "A", "T").build();

        Mockito.when(frequencyFilter.getFilterType()).thenReturn(FilterType.FREQUENCY_FILTER);
        Mockito.when(qualityFilter.getFilterType()).thenReturn(FilterType.QUALITY_FILTER);
        Mockito.when(targetFilter.getFilterType()).thenReturn(FilterType.TARGET_FILTER);

        FilterResult passFrequencyResult = new FrequencyFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failFrequencyResult = new FrequencyFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(frequencyFilter.runFilter(passesAllFilters)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(failsAllFilters)).thenReturn(failFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesTargetQualityFilter)).thenReturn(failFrequencyResult);

        FilterResult passQualityResult = new QualityFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failQualityResult = new QualityFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(qualityFilter.runFilter(passesAllFilters)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(failsAllFilters)).thenReturn(failQualityResult);
        Mockito.when(qualityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(passesTargetQualityFilter)).thenReturn(passQualityResult);

        FilterResult passTargetResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failTargetResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(targetFilter.runFilter(passesAllFilters)).thenReturn(passTargetResult);
        Mockito.when(targetFilter.runFilter(failsAllFilters)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesTargetQualityFilter)).thenReturn(passTargetResult);

    }

    private List<VariantEvaluation> makeVariantsList() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesTargetQualityFilter);
        variantEvaluations.add(passesQualityFrequencyFilter);
        variantEvaluations.add(failsAllFilters);
        variantEvaluations.add(passesAllFilters);
        return variantEvaluations;
    }

//    @Test
//    public void testUseDestructiveFilteringOnlyReturnsPassesAllFiltersVariant() {
//        List<Filter> filters = new ArrayList<>();
//        filters.add(targetFilter);
//        filters.add(qualityFilter);
//        filters.add(frequencyFilter);
//
//        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
//        variantEvaluations.add(passesTargetQualityFilter);
//        variantEvaluations.add(passesQualityFrequencyFilter);
//        variantEvaluations.add(failsAllFilters);
//        variantEvaluations.add(passesAllFilters);
//
//        List<VariantEvaluation> expResult = new ArrayList<>();
//        expResult.add(passesAllFilters);
//
//        List<VariantEvaluation> result = instance.useDestructiveFiltering(filters, variantEvaluations);
//        Assert.assertThat(result, equalTo(expResult));
//    }
//
//    @Test
//    public void testUseDestructiveFilteringMaintainsOriginalVariantEvaluations() {
//        List<Filter> filters = new ArrayList<>();
//        filters.add(targetFilter);
//        filters.add(qualityFilter);
//        filters.add(frequencyFilter);
//
//        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
//        variantEvaluations.add(passesTargetQualityFilter);
//        variantEvaluations.add(passesQualityFrequencyFilter);
//        variantEvaluations.add(failsAllFilters);
//        variantEvaluations.add(passesAllFilters);
//
//        List<VariantEvaluation> expResult = new ArrayList<>();
//        expResult.add(passesTargetQualityFilter);
//        expResult.add(passesQualityFrequencyFilter);
//        expResult.add(failsAllFilters);
//        expResult.add(passesAllFilters);
//        
//        //when
//        SimpleVariantFilterRunner.useDestructiveFiltering(filters, variantEvaluations);
//
//        //then
//        Assert.assertThat(variantEvaluations, equalTo(expResult));
//    }
    @Test
    public void testUseNonDestructiveFilteringReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> variantEvaluations = makeVariantsList();
        List<VariantEvaluation> expResult = makeVariantsList();

        List<VariantEvaluation> result = new VariantFilterRunner().run(filters).over(variantEvaluations).usingSimpleFiltering();
        assertThat(result, equalTo(expResult));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(false));

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(false));
    }

    @Test
    public void testUseNonDestructiveFilteringWithOneFilterReturnsAllVariantEvaluations() {

        List<VariantEvaluation> variantEvaluations = makeVariantsList();
        List<VariantEvaluation> expResult = makeVariantsList();

        List<VariantEvaluation> result = new VariantFilterRunner().run(frequencyFilter).over(variantEvaluations).usingSimpleFiltering();

        assertThat(result, equalTo(expResult));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(false));

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(true));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(false));
    }

    @Test
    public void testUseNonDestructiveFilteringWithTwoChainedFiltersReturnsAllVariantEvaluations() {

        List<VariantEvaluation> variantEvaluations = makeVariantsList();
        List<VariantEvaluation> expResult = makeVariantsList();

        List<VariantEvaluation> result = new VariantFilterRunner().run(frequencyFilter).run(targetFilter).over(variantEvaluations).usingSimpleFiltering();

        assertThat(result, equalTo(expResult));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(false));

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(false));
    }

    @Test
    public void testUseNonDestructiveFilteringUsingInterfaceRunReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> variantEvaluations = makeVariantsList();
        List<VariantEvaluation> expResult = makeVariantsList();

        FilterRunner variantFilterRunner = new SimpleVariantFilterRunner();
        List<VariantEvaluation> result = variantFilterRunner.run(filters, variantEvaluations);

        assertThat(result, equalTo(expResult));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(false));
        assertThat(failsAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(false));

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.TARGET_FILTER), is(false));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesQualityFrequencyFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(true));

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesTargetQualityFilter.passedFilter(FilterType.FREQUENCY_FILTER), is(false));
    }

    private void printVariantFilterStatus(String variantName, VariantEvaluation varEval) {
        System.out.printf("%s: Passed:%s Failed:%s%n", variantName, varEval.getFilterResults().keySet(), varEval.getFailedFilterTypes());
    }
}
