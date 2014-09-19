/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.filter.SimpleVariantFilterRunner.VariantFilterRunner;
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
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleVariantFilterRunnerTest {
   
    //Frequency run data
    @Mock
    private VariantFilter frequencyFilter;
    //Quality run data
    @Mock
    private VariantFilter qualityFilter;   
    //Target run data
    @Mock
    private VariantFilter targetFilter;    
    
    @Mock
    private VariantEvaluation passesAllFilters; 
    @Mock
    private VariantEvaluation failsAllFilters;
    @Mock
    private VariantEvaluation passesQualityFrequencyFilter;
    @Mock
    private VariantEvaluation passesTargetQualityFilter;
    
    @Mock
    private FrequencyDao frequencyDao;

    public SimpleVariantFilterRunnerTest() {
    }

    @Before
    public void setUp() {
        
        MockitoAnnotations.initMocks(this);

        Mockito.when(frequencyDao.getFrequencyData(null)).thenReturn(null);

                
        Mockito.when(passesAllFilters.passedFilters()).thenReturn(true);
        Mockito.when(failsAllFilters.passedFilters()).thenReturn(false);
        Mockito.when(passesQualityFrequencyFilter.passedFilters()).thenReturn(false);
        Mockito.when(passesTargetQualityFilter.passedFilters()).thenReturn(false);
        
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

        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesTargetQualityFilter);
        variantEvaluations.add(passesQualityFrequencyFilter);
        variantEvaluations.add(failsAllFilters);
        variantEvaluations.add(passesAllFilters);

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesTargetQualityFilter);
        expResult.add(passesQualityFrequencyFilter);
        expResult.add(failsAllFilters);
        expResult.add(passesAllFilters);

        List<VariantEvaluation> result = new VariantFilterRunner().run(filters).over(variantEvaluations).usingSimpleFiltering();
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testUseNonDestructiveFilteringWithOneFilterReturnsAllVariantEvaluations() {
        
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesTargetQualityFilter);
        variantEvaluations.add(passesQualityFrequencyFilter);
        variantEvaluations.add(failsAllFilters);
        variantEvaluations.add(passesAllFilters);

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesTargetQualityFilter);
        expResult.add(passesQualityFrequencyFilter);
        expResult.add(failsAllFilters);
        expResult.add(passesAllFilters);

        List<VariantEvaluation> result = new VariantFilterRunner().run(frequencyFilter).over(variantEvaluations).usingSimpleFiltering();
        
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testUseNonDestructiveFilteringUsingInterfaceRunReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(passesTargetQualityFilter);
        variantEvaluations.add(passesQualityFrequencyFilter);
        variantEvaluations.add(failsAllFilters);
        variantEvaluations.add(passesAllFilters);

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesTargetQualityFilter);
        expResult.add(passesQualityFrequencyFilter);
        expResult.add(failsAllFilters);
        expResult.add(passesAllFilters);

        FilterRunner variantFilterRunner = new SimpleVariantFilterRunner();
        List<VariantEvaluation> result = variantFilterRunner.run(filters, variantEvaluations);
        
        assertThat(result, equalTo(expResult));
    }
}
