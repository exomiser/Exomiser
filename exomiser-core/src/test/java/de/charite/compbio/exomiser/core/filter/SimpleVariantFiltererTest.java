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
public class SimpleVariantFiltererTest {

    private SimpleVariantFilterer instance;
    
    //Frequency filter data
    @Mock
    private Filter frequencyFilter;
    //Quality filter data
    @Mock
    private Filter qualityFilter;   
    //Target filter data
    @Mock
    private Filter targetFilter;    
    
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

    public SimpleVariantFiltererTest() {
    }

    @Before
    public void setUp() {

        instance = new SimpleVariantFilterer();
        
        MockitoAnnotations.initMocks(this);

        Mockito.when(frequencyDao.getFrequencyData(null)).thenReturn(null);

                
        Mockito.when(passesAllFilters.passesFilters()).thenReturn(true);
        Mockito.when(failsAllFilters.passesFilters()).thenReturn(false);
        Mockito.when(passesQualityFrequencyFilter.passesFilters()).thenReturn(false);
        Mockito.when(passesTargetQualityFilter.passesFilters()).thenReturn(false);
        
        Mockito.when(frequencyFilter.getFilterType()).thenReturn(FilterType.FREQUENCY_FILTER);
        Mockito.when(qualityFilter.getFilterType()).thenReturn(FilterType.QUALITY_FILTER);
        Mockito.when(targetFilter.getFilterType()).thenReturn(FilterType.TARGET_FILTER);

        
        Mockito.when(frequencyFilter.filterVariant(passesAllFilters)).thenReturn(true);
        Mockito.when(frequencyFilter.filterVariant(failsAllFilters)).thenReturn(false);
        Mockito.when(frequencyFilter.filterVariant(passesQualityFrequencyFilter)).thenReturn(true);
        Mockito.when(frequencyFilter.filterVariant(passesTargetQualityFilter)).thenReturn(false);
        
        Mockito.when(qualityFilter.filterVariant(passesAllFilters)).thenReturn(true);
        Mockito.when(qualityFilter.filterVariant(failsAllFilters)).thenReturn(false);
        Mockito.when(qualityFilter.filterVariant(passesQualityFrequencyFilter)).thenReturn(true);
        Mockito.when(qualityFilter.filterVariant(passesTargetQualityFilter)).thenReturn(true);
        
        Mockito.when(targetFilter.filterVariant(passesAllFilters)).thenReturn(true);
        Mockito.when(targetFilter.filterVariant(failsAllFilters)).thenReturn(false);
        Mockito.when(targetFilter.filterVariant(passesQualityFrequencyFilter)).thenReturn(false);
        Mockito.when(targetFilter.filterVariant(passesTargetQualityFilter)).thenReturn(true);
        
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
//        SimpleVariantFilterer.useDestructiveFiltering(filters, variantEvaluations);
//
//        //then
//        Assert.assertThat(variantEvaluations, equalTo(expResult));
//    }
    
    @Test
    public void testUseNonDestructiveFilteringReturnsAllVariantEvaluations() {
        List<Filter> filters = new ArrayList<>();
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

        List<VariantEvaluation> result = instance.filterVariants(filters, variantEvaluations);
        Assert.assertThat(result, equalTo(expResult));
    }
}
