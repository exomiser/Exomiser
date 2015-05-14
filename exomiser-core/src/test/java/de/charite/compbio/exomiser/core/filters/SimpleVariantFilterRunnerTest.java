/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import static de.charite.compbio.exomiser.core.filters.FilterType.*;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner.VariantFilterRunner;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.EnumSet;
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

    private List<VariantEvaluation> variantEvaluations;
    
    @Before
    public void setUp() {

        passesAllFilters = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        failsAllFilters = new VariantEvaluation.VariantBuilder(1, 2, "A", "T").build();
        passesQualityFrequencyFilter = new VariantEvaluation.VariantBuilder(1, 3, "A", "T").build();
        passesTargetQualityFilter = new VariantEvaluation.VariantBuilder(1, 4, "A", "T").build();

        makeVariantEvaluations();
                
        setUpFrequencyMocks();
        setUpQualityMocks();
        setUpTargetMocks();
    }

    private void makeVariantEvaluations() {
        variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesTargetQualityFilter);
        variantEvaluations.add(passesQualityFrequencyFilter);
        variantEvaluations.add(failsAllFilters);
        variantEvaluations.add(passesAllFilters);
    }

    private void setUpFrequencyMocks() {
        Mockito.when(frequencyFilter.getFilterType()).thenReturn(FREQUENCY_FILTER);

        FilterResult passFrequencyResult = new FrequencyFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failFrequencyResult = new FrequencyFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(frequencyFilter.runFilter(passesAllFilters)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(failsAllFilters)).thenReturn(failFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesTargetQualityFilter)).thenReturn(failFrequencyResult);
    }

    private void setUpQualityMocks() {
        Mockito.when(qualityFilter.getFilterType()).thenReturn(QUALITY_FILTER);

        FilterResult passQualityResult = new QualityFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failQualityResult = new QualityFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(qualityFilter.runFilter(passesAllFilters)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(failsAllFilters)).thenReturn(failQualityResult);
        Mockito.when(qualityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(passesTargetQualityFilter)).thenReturn(passQualityResult);
    }

    private void setUpTargetMocks() {
        Mockito.when(targetFilter.getFilterType()).thenReturn(TARGET_FILTER);

        FilterResult passTargetResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failTargetResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(targetFilter.runFilter(passesAllFilters)).thenReturn(passTargetResult);
        Mockito.when(targetFilter.runFilter(failsAllFilters)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesTargetQualityFilter)).thenReturn(passTargetResult);
    }

    private void printVariantFilterStatus(String variantName, VariantEvaluation varEval) {
        System.out.printf("%s: Passed:%s Failed:%s%n", variantName, varEval.getFilterResults().keySet(), varEval.getFailedFilterTypes());
    }
    
    private void assertPassedFilters(VariantEvaluation variant, FilterType... filterTypes) {
        for (FilterType type : filterTypes) {
            assertThat(variant.passedFilter(type), is(true));
        }
    }

    private void assertFailedFilters(VariantEvaluation variant, FilterType... filterTypes) {
        for (FilterType type : filterTypes) {
            assertThat(variant.passedFilter(type), is(false));
        }
    }
    
    @Test
    public void testRun_AddsFilterResultsToVariant() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesAllFilters);

        List<VariantEvaluation> result = new VariantFilterRunner().run(filters).over(variantEvaluations).usingSimpleFiltering();
        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertPassedFilters(passesAllFilters, TARGET_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);
    }

    @Test
    public void testUseNonDestructiveFilteringReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> result = new VariantFilterRunner().run(filters).over(variantEvaluations).usingSimpleFiltering();
        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertPassedFilters(passesAllFilters, TARGET_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertPassedFilters(failsAllFilters);
        assertFailedFilters(failsAllFilters, TARGET_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertPassedFilters(passesQualityFrequencyFilter, QUALITY_FILTER, FREQUENCY_FILTER);
        assertFailedFilters(passesQualityFrequencyFilter, TARGET_FILTER);

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertPassedFilters(passesTargetQualityFilter, QUALITY_FILTER, TARGET_FILTER);
        assertFailedFilters(passesTargetQualityFilter, FREQUENCY_FILTER);
    }

    @Test
    public void testUseNonDestructiveFilteringWithOneFilterReturnsAllVariantEvaluations() {

        List<VariantEvaluation> result = new VariantFilterRunner()
                .run(frequencyFilter)
                .over(variantEvaluations)
                .usingSimpleFiltering();

        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertPassedFilters(passesAllFilters, FREQUENCY_FILTER);

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertFailedFilters(failsAllFilters, FREQUENCY_FILTER);

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(true));
        assertPassedFilters(passesQualityFrequencyFilter, FREQUENCY_FILTER);

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertFailedFilters(passesTargetQualityFilter, FREQUENCY_FILTER);
    }

    @Test
    public void testUseNonDestructiveFilteringWithTwoChainedFiltersReturnsAllVariantEvaluations() {

        List<VariantEvaluation> result = new VariantFilterRunner()
                .run(frequencyFilter)
                .run(targetFilter)
                .over(variantEvaluations)
                .usingSimpleFiltering();

        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertPassedFilters(passesAllFilters, TARGET_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertFailedFilters(failsAllFilters, TARGET_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertPassedFilters(passesQualityFrequencyFilter, FREQUENCY_FILTER);
        assertFailedFilters(passesQualityFrequencyFilter, TARGET_FILTER);

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertPassedFilters(passesTargetQualityFilter, TARGET_FILTER);
        assertFailedFilters(passesTargetQualityFilter, FREQUENCY_FILTER);
    }

    @Test
    public void testUseNonDestructiveFilteringUsingInterfaceRunReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        FilterRunner variantFilterRunner = new SimpleVariantFilterRunner();
        List<VariantEvaluation> result = variantFilterRunner.run(filters, variantEvaluations);

        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertPassedFilters(passesAllFilters, TARGET_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertFailedFilters(failsAllFilters, TARGET_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertPassedFilters(passesQualityFrequencyFilter, QUALITY_FILTER, FREQUENCY_FILTER);
        assertFailedFilters(passesQualityFrequencyFilter, TARGET_FILTER);

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertPassedFilters(passesTargetQualityFilter, QUALITY_FILTER, TARGET_FILTER);
        assertFailedFilters(passesTargetQualityFilter, FREQUENCY_FILTER);
    }

}
