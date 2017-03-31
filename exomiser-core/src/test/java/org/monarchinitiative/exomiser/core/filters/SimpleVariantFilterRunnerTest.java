/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceMock;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.filters.FilterType.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantFilterRunnerTest {

    private SimpleVariantFilterRunner instance;
    
    private VariantDataService variantDataService;
    
    //Frequency run data
    private VariantFilter frequencyFilter;
    
    //Quality run data
    private static final double PASS_QUALITY = 1000;
    private static final double FAIL_QUALITY = 0;
    private final QualityFilter qualityFilter = new QualityFilter(PASS_QUALITY - 1);
    
    //Target run data
    private static final VariantEffect FAIL_VARIANT_EFFECT = VariantEffect.SEQUENCE_VARIANT;
    private static final VariantEffect PASS_VARIANT_EFFECT = VariantEffect.MISSENSE_VARIANT;
    private final VariantEffectFilter targetFilter = new VariantEffectFilter(EnumSet.of(FAIL_VARIANT_EFFECT));

    private VariantEvaluation passesAllFilters;
    private VariantEvaluation failsAllFilters;
    private VariantEvaluation passesQualityFrequencyFilter;
    private VariantEvaluation passesTargetQualityFilter;

    private List<VariantEvaluation> variantEvaluations;
    
    @Before
    public void setUp() {

        passesAllFilters = new VariantEvaluation.Builder(1, 1, "A", "T").quality(PASS_QUALITY).variantEffect(PASS_VARIANT_EFFECT).build();
        failsAllFilters = new VariantEvaluation.Builder(2, 2, "A", "T").quality(FAIL_QUALITY).variantEffect(FAIL_VARIANT_EFFECT).build();
        passesQualityFrequencyFilter = new VariantEvaluation.Builder(3, 3, "A", "T").quality(PASS_QUALITY).variantEffect(FAIL_VARIANT_EFFECT).build();
        passesTargetQualityFilter = new VariantEvaluation.Builder(4, 4, "A", "T").quality(PASS_QUALITY).variantEffect(PASS_VARIANT_EFFECT).build();

        variantEvaluations = Arrays.asList(passesAllFilters, 
                                           failsAllFilters, 
                                           passesQualityFrequencyFilter, 
                                           passesTargetQualityFilter);
         
        variantDataService = new VariantDataServiceMock(mockFrequencyData(), null, null, null);
        
        frequencyFilter = new FrequencyDataProvider(variantDataService, EnumSet.of(FrequencySource.UNKNOWN), new FrequencyFilter(1f));
        
        instance = new SimpleVariantFilterRunner();
    }

    private Map<Variant, FrequencyData> mockFrequencyData() {
        FrequencyData passFrequency = new FrequencyData(RsId.valueOf(12345), Frequency.valueOf(0.01f, FrequencySource.UNKNOWN));
        FrequencyData failFrequency = new FrequencyData(RsId.valueOf(54321), Frequency.valueOf(100f, FrequencySource.UNKNOWN));

        Map<Variant, FrequencyData> frequecyData = new HashMap<>();
        frequecyData.put(passesAllFilters, passFrequency);
        frequecyData.put(failsAllFilters, failFrequency);
        frequecyData.put(passesQualityFrequencyFilter, passFrequency);
        frequecyData.put(passesTargetQualityFilter, failFrequency);
        return frequecyData;
    }

    private void printVariantFilterStatus(String variantName, VariantEvaluation varEval) {
//        System.out.printf("%s: Passed:%s Failed:%s%n", variantName, varEval.getFilterResults().keySet(), varEval.getFailedFilterTypes());
        System.out.printf("%s: %s%n", variantName, varEval);
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
    
    private void assertFailsEverything(VariantEvaluation variantEvaluation) {
        assertThat(variantEvaluation.passedFilters(), is(false));
        Set<FilterType> allFilterTypes = EnumSet.allOf(FilterType.class);
        //filters not run should return false
        allFilterTypes.forEach(filterType -> {
            assertThat(variantEvaluation.passedFilter(filterType), is(false));
        });
    }

    @Test
    public void testUseNonDestructiveFilteringUsingInterfaceRunReturnsAllVariantEvaluations() {
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(targetFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        List<VariantEvaluation> result = instance.run(filters, variantEvaluations);

        assertThat(result, equalTo(variantEvaluations));

        printVariantFilterStatus("passesAllFilters", passesAllFilters);
        assertThat(passesAllFilters.passedFilters(), is(true));
        assertPassedFilters(passesAllFilters, VARIANT_EFFECT_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("failsAllFilters", failsAllFilters);
        assertThat(failsAllFilters.passedFilters(), is(false));
        assertFailedFilters(failsAllFilters, VARIANT_EFFECT_FILTER, QUALITY_FILTER, FREQUENCY_FILTER);

        printVariantFilterStatus("passesQualityFrequencyFilter", passesQualityFrequencyFilter);
        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertPassedFilters(passesQualityFrequencyFilter, QUALITY_FILTER, FREQUENCY_FILTER);
        assertFailedFilters(passesQualityFrequencyFilter, VARIANT_EFFECT_FILTER);

        printVariantFilterStatus("passesTargetQualityFilter", passesTargetQualityFilter);
        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertPassedFilters(passesTargetQualityFilter, QUALITY_FILTER, VARIANT_EFFECT_FILTER);
        assertFailedFilters(passesTargetQualityFilter, FREQUENCY_FILTER);
    }

    @Test
    public void testRun_WithOneFilterReturnsAllVariants() {
        
        VariantFilter filterToPass = qualityFilter;
                  
        List<VariantEvaluation> result = instance.run(filterToPass, variantEvaluations);
        
        assertThat(result, equalTo(variantEvaluations));
        
        assertPassedFilterAndFailedAllOthers(passesAllFilters, filterToPass);
        assertPassedFilterAndFailedAllOthers(passesQualityFrequencyFilter, filterToPass);
        assertPassedFilterAndFailedAllOthers(passesTargetQualityFilter, filterToPass);

        assertFailsEverything(failsAllFilters);
        
    }

    private void assertPassedFilterAndFailedAllOthers(VariantEvaluation variantEvaluation, VariantFilter filterToPass) {
        assertThat(variantEvaluation.passedFilters(), is(true));
        assertThat(variantEvaluation.passedFilter(filterToPass.getFilterType()), is(true));
        
        Set<FilterType> allOtherFilterTypes = EnumSet.allOf(FilterType.class);
        allOtherFilterTypes.remove(filterToPass.getFilterType());
        //filters not run should return false
        allOtherFilterTypes.forEach(otherFilterType -> {
            assertThat(variantEvaluation.passedFilter(otherFilterType), is(false));
        });
    }
       
    @Test
    public void testRun_WithTwoFiltersInSuccessionReturnsAllVariants() {
        
        VariantFilter firstFilterToPass = qualityFilter;
                  
        List<VariantEvaluation> result = instance.run(qualityFilter, variantEvaluations);
        assertThat(result, equalTo(variantEvaluations));     
        
        assertPassedFilterAndFailedAllOthers(passesAllFilters, firstFilterToPass);
        assertPassedFilterAndFailedAllOthers(passesQualityFrequencyFilter, firstFilterToPass);
        assertPassedFilterAndFailedAllOthers(passesTargetQualityFilter, firstFilterToPass);

        assertFailsEverything(failsAllFilters);

        //run a second filter
        VariantFilter secondFilterToPass = targetFilter;
        
        List<VariantEvaluation> secondResults = instance.run(secondFilterToPass, variantEvaluations);
        
        assertThat(secondResults, equalTo(variantEvaluations));     
        assertPassedFilters(passesAllFilters, firstFilterToPass.getFilterType(), secondFilterToPass.getFilterType());     
        System.out.println(passesAllFilters);

        assertPassedFilters(passesQualityFrequencyFilter, firstFilterToPass.getFilterType());    
        assertFailedFilters(passesQualityFrequencyFilter, secondFilterToPass.getFilterType());
        System.out.println(passesQualityFrequencyFilter);

        assertPassedFilters(passesTargetQualityFilter, firstFilterToPass.getFilterType(), secondFilterToPass.getFilterType());     
        System.out.println(passesTargetQualityFilter);

        assertFailsEverything(failsAllFilters);     
        System.out.println(failsAllFilters);
    }

}
