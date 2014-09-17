/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Assert;
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

    private SimpleVariantFilterRunner instance;
    
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

        instance = new SimpleVariantFilterRunner();
        
        MockitoAnnotations.initMocks(this);

        Mockito.when(frequencyDao.getFrequencyData(null)).thenReturn(null);

                
        Mockito.when(passesAllFilters.passedFilters()).thenReturn(true);
        Mockito.when(failsAllFilters.passedFilters()).thenReturn(false);
        Mockito.when(passesQualityFrequencyFilter.passedFilters()).thenReturn(false);
        Mockito.when(passesTargetQualityFilter.passedFilters()).thenReturn(false);
        
        Mockito.when(frequencyFilter.getFilterType()).thenReturn(FilterType.FREQUENCY_FILTER);
        Mockito.when(qualityFilter.getFilterType()).thenReturn(FilterType.QUALITY_FILTER);
        Mockito.when(targetFilter.getFilterType()).thenReturn(FilterType.TARGET_FILTER);

        
        Mockito.when(frequencyFilter.filter(passesAllFilters)).thenReturn(true);
        Mockito.when(frequencyFilter.filter(failsAllFilters)).thenReturn(false);
        Mockito.when(frequencyFilter.filter(passesQualityFrequencyFilter)).thenReturn(true);
        Mockito.when(frequencyFilter.filter(passesTargetQualityFilter)).thenReturn(false);
        
        Mockito.when(qualityFilter.filter(passesAllFilters)).thenReturn(true);
        Mockito.when(qualityFilter.filter(failsAllFilters)).thenReturn(false);
        Mockito.when(qualityFilter.filter(passesQualityFrequencyFilter)).thenReturn(true);
        Mockito.when(qualityFilter.filter(passesTargetQualityFilter)).thenReturn(true);
        
        Mockito.when(targetFilter.filter(passesAllFilters)).thenReturn(true);
        Mockito.when(targetFilter.filter(failsAllFilters)).thenReturn(false);
        Mockito.when(targetFilter.filter(passesQualityFrequencyFilter)).thenReturn(false);
        Mockito.when(targetFilter.filter(passesTargetQualityFilter)).thenReturn(true);
        
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

        List<VariantEvaluation> result = instance.run(filters, variantEvaluations);
        Assert.assertThat(result, equalTo(expResult));
    }
}
