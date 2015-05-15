/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class SparseVariantFilterRunnerTest {

    @InjectMocks
    private SparseVariantFilterRunner instance;
    
    @Mock
    private VariantDataService variantEvaluationFactory;
    
    @Mock
    private TargetFilter targetFilter;
    @Mock
    private QualityFilter qualityFilter;
    @Mock
    private FrequencyFilter frequencyFilter;
    @Mock
    private PathogenicityFilter pathogenicityFilter;
    
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
        setUpPathogenicityMocks();
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
        Mockito.when(frequencyFilter.getFilterType()).thenReturn(FilterType.FREQUENCY_FILTER);
        
        FilterResult passFrequencyResult = new FrequencyFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failFrequencyResult = new FrequencyFilterResult(0f, FilterResultStatus.FAIL);
        
        Mockito.when(frequencyFilter.runFilter(passesAllFilters)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(failsAllFilters)).thenReturn(failFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesTargetQualityFilter)).thenReturn(failFrequencyResult);
    }

    private void setUpPathogenicityMocks() {
        Mockito.when(pathogenicityFilter.getFilterType()).thenReturn(FilterType.PATHOGENICITY_FILTER);

        FilterResult pass = new PathogenicityFilterResult(1f, FilterResultStatus.PASS);
        FilterResult fail = new PathogenicityFilterResult(0f, FilterResultStatus.FAIL);
        
        Mockito.when(pathogenicityFilter.runFilter(passesAllFilters)).thenReturn(pass);
        Mockito.when(pathogenicityFilter.runFilter(failsAllFilters)).thenReturn(fail);
        Mockito.when(pathogenicityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(fail);
        Mockito.when(pathogenicityFilter.runFilter(passesTargetQualityFilter)).thenReturn(fail);
    }

    private void setUpQualityMocks() {
        Mockito.when(qualityFilter.getFilterType()).thenReturn(FilterType.QUALITY_FILTER);

        FilterResult passQualityResult = new QualityFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failQualityResult = new QualityFilterResult(0f, FilterResultStatus.FAIL);
        
        Mockito.when(qualityFilter.runFilter(passesAllFilters)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(failsAllFilters)).thenReturn(failQualityResult);
        Mockito.when(qualityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(passesTargetQualityFilter)).thenReturn(passQualityResult);
    }

    private void setUpTargetMocks() {
        Mockito.when(targetFilter.getFilterType()).thenReturn(FilterType.TARGET_FILTER);

        FilterResult passTargetResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
        FilterResult failTargetResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);

        Mockito.when(targetFilter.runFilter(passesAllFilters)).thenReturn(passTargetResult);
        Mockito.when(targetFilter.runFilter(failsAllFilters)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(failTargetResult);
        Mockito.when(targetFilter.runFilter(passesTargetQualityFilter)).thenReturn(passTargetResult);
    }

    @Test
    public void testRun_OnlyReturnsVariantPassingAllFilters() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);
        filters.add(pathogenicityFilter);

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesAllFilters);

        List<VariantEvaluation> result = instance.run(filters, variantEvaluations);
        assertThat(result, equalTo(expResult));
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.TARGET_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.PATHOGENICITY_FILTER), is(true));
        //filters not run should return false 
        assertThat(passesAllFilters.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));
    }

    @Test
    public void testRun_WithNoFiltersReturnsOriginalVariantEvaluations() {
        List<VariantFilter> filters = Collections.emptyList();
      
        List<VariantEvaluation> result = instance.run(filters, variantEvaluations);
        assertThat(result, equalTo(variantEvaluations));
    }
}
