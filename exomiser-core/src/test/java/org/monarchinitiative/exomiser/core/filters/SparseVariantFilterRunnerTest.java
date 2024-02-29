/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.filters.FilterType.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SparseVariantFilterRunnerTest {

    private final SparseVariantFilterRunner instance = new SparseVariantFilterRunner();

    @Mock
    private VariantEffectFilter variantEffectFilter;
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

    @BeforeEach
    public void setUp() {

        passesAllFilters = TestFactory.variantBuilder(1, 1, "A", "T").build();
        failsAllFilters = TestFactory.variantBuilder(2, 2, "A", "T").build();
        passesQualityFrequencyFilter = TestFactory.variantBuilder(3, 3, "A", "T").build();
        passesTargetQualityFilter = TestFactory.variantBuilder(4, 4, "A", "T").build();

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
        FilterResult passFrequencyResult = new PassFilterResult(FilterType.FREQUENCY_FILTER);
        FilterResult failFrequencyResult = new FailFilterResult(FilterType.FREQUENCY_FILTER);

        Mockito.when(frequencyFilter.runFilter(passesAllFilters)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(failsAllFilters)).thenReturn(failFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passFrequencyResult);
        Mockito.when(frequencyFilter.runFilter(passesTargetQualityFilter)).thenReturn(failFrequencyResult);
    }

    private void setUpPathogenicityMocks() {
        FilterResult pass = new PassFilterResult(FilterType.PATHOGENICITY_FILTER);
        FilterResult fail = new FailFilterResult(FilterType.PATHOGENICITY_FILTER);

        Mockito.when(pathogenicityFilter.runFilter(passesAllFilters)).thenReturn(pass);
        Mockito.when(pathogenicityFilter.runFilter(failsAllFilters)).thenReturn(fail);
        Mockito.when(pathogenicityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(fail);
        Mockito.when(pathogenicityFilter.runFilter(passesTargetQualityFilter)).thenReturn(fail);
    }

    private void setUpQualityMocks() {
        Mockito.when(qualityFilter.getFilterType()).thenReturn(FilterType.QUALITY_FILTER);

        FilterResult passQualityResult = new PassFilterResult(FilterType.QUALITY_FILTER);
        FilterResult failQualityResult = new FailFilterResult(FilterType.QUALITY_FILTER);

        Mockito.when(qualityFilter.runFilter(passesAllFilters)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(failsAllFilters)).thenReturn(failQualityResult);
        Mockito.when(qualityFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(passQualityResult);
        Mockito.when(qualityFilter.runFilter(passesTargetQualityFilter)).thenReturn(passQualityResult);
    }

    private void setUpTargetMocks() {
        FilterResult passTargetResult = new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER);
        FilterResult failTargetResult = new FailFilterResult(FilterType.VARIANT_EFFECT_FILTER);

        Mockito.when(variantEffectFilter.runFilter(passesAllFilters)).thenReturn(passTargetResult);
        Mockito.when(variantEffectFilter.runFilter(failsAllFilters)).thenReturn(failTargetResult);
        Mockito.when(variantEffectFilter.runFilter(passesQualityFrequencyFilter)).thenReturn(failTargetResult);
        Mockito.when(variantEffectFilter.runFilter(passesTargetQualityFilter)).thenReturn(passTargetResult);
    }

    @Test
    public void testRunOnlyReturnsVariantPassingAllFilters() {
        //The order of these filters is important
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(pathogenicityFilter);
        filters.add(variantEffectFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);

        filters.forEach(filter -> instance.run(filter, variantEvaluations));

        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.VARIANT_EFFECT_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.FREQUENCY_FILTER), is(true));
        assertThat(passesAllFilters.passedFilter(FilterType.PATHOGENICITY_FILTER), is(true));
        //filters not run should return false
        assertThat(passesAllFilters.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));

        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.getPassedFilterTypes().isEmpty(), is(true));

        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.getPassedFilterTypes().isEmpty(), is(true));

        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.getPassedFilterTypes().isEmpty(), is(true));

        FilterResultCount pathCount = new FilterResultCount(PATHOGENICITY_FILTER, 1, 3);
        FilterResultCount effectCount = new FilterResultCount(VARIANT_EFFECT_FILTER, 1, 0);
        FilterResultCount qualityCount = new FilterResultCount(QUALITY_FILTER, 1, 0);
        FilterResultCount frequencyCount = new FilterResultCount(FREQUENCY_FILTER, 1, 0);
        assertThat(instance.filterCounts(), equalTo(List.of(pathCount, effectCount, qualityCount, frequencyCount)));
    }

    @Test
    public void testRunOnlyReturnsVariantPassingAllFiltersDifferentOrder() {
        //The order of these filters is important
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(variantEffectFilter);
        filters.add(qualityFilter);
        filters.add(frequencyFilter);
        filters.add(pathogenicityFilter);

        filters.forEach(filter -> instance.run(filter, variantEvaluations));

        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER,
                        FilterType.FREQUENCY_FILTER,
                        FilterType.PATHOGENICITY_FILTER)));
        //filters not run should return false
        assertThat(passesAllFilters.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));

        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.getPassedFilterTypes().isEmpty(), is(true));

        assertThat(passesTargetQualityFilter.passedFilters(), is(false));
        assertThat(passesTargetQualityFilter.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER)));

        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.getPassedFilterTypes().isEmpty(), is(true));
    }

    @Test
    public void testRunReturnsVariantsPassingVariantEffectAndQualityFilters() {
        //The order of these filters is important
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(variantEffectFilter);
        filters.add(qualityFilter);

        filters.forEach(filter -> instance.run(filter, variantEvaluations));

        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER)));
        //filters not run should return false
        assertThat(passesAllFilters.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));

        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.getPassedFilterTypes().isEmpty(), is(true));

        assertThat(passesTargetQualityFilter.passedFilters(), is(true));
        assertThat(passesTargetQualityFilter.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER)));

        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.getPassedFilterTypes().isEmpty(), is(true));
    }

    @Test
    public void testRunReturnsVariantsPassingQualityAndVariantEffectFilters() {
        //The order of these filters is important
        List<VariantFilter> filters = new ArrayList<>();
        filters.add(qualityFilter);
        filters.add(variantEffectFilter);

        filters.forEach(filter -> instance.run(filter, variantEvaluations));

        assertThat(passesAllFilters.passedFilters(), is(true));
        assertThat(passesAllFilters.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER)));
        //filters not run should return false
        assertThat(passesAllFilters.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));

        assertThat(passesQualityFrequencyFilter.passedFilters(), is(false));
        assertThat(passesQualityFrequencyFilter.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.QUALITY_FILTER)));

        assertThat(passesTargetQualityFilter.passedFilters(), is(true));
        assertThat(passesTargetQualityFilter.getPassedFilterTypes(),
                equalTo(EnumSet.of(FilterType.VARIANT_EFFECT_FILTER,
                        FilterType.QUALITY_FILTER)));

        assertThat(failsAllFilters.passedFilters(), is(false));
        assertThat(failsAllFilters.getPassedFilterTypes().isEmpty(), is(true));
    }

    @Test
    public void testRunWithOneFilterOnlyReturnsVariantPassingAllFilters() {

        VariantFilter filterToPass = qualityFilter;

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesTargetQualityFilter);
        expResult.add(passesQualityFrequencyFilter);
        expResult.add(passesAllFilters);

        List<VariantEvaluation> result = instance.run(filterToPass, variantEvaluations);

        assertThat(result, equalTo(expResult));
        for (VariantEvaluation variantEvaluation : result) {
            assertPassedFilterAndFailedAllOthers(variantEvaluation, filterToPass);
        }

    }

    @Test
    public void testRunWithOneFilterUsingStreamOnlyReturnsVariantPassingAllFilters() {

        VariantFilter filterToPass = qualityFilter;

        List<VariantEvaluation> expResult = new ArrayList<>();
        expResult.add(passesTargetQualityFilter);
        expResult.add(passesQualityFrequencyFilter);
        expResult.add(passesAllFilters);

        List<VariantEvaluation> result = variantEvaluations.stream()
                .filter(variantEvaluation -> {
                    instance.run(filterToPass, variantEvaluation);
                    return variantEvaluation.passedFilters();
                })
                .collect(toList());

        assertThat(result, equalTo(expResult));
        for (VariantEvaluation variantEvaluation : result) {
            assertPassedFilterAndFailedAllOthers(variantEvaluation, filterToPass);
        }

    }

    private void assertPassedFilterAndFailedAllOthers(VariantEvaluation variantEvaluation, VariantFilter filterToPass) {
        assertThat(variantEvaluation.passedFilters(), is(true));
        assertThat(variantEvaluation.passedFilter(filterToPass.getFilterType()), is(true));
        //filters not run should return false
        assertThat(variantEvaluation.passedFilter(FilterType.VARIANT_EFFECT_FILTER), is(false));
        assertThat(variantEvaluation.passedFilter(FilterType.FREQUENCY_FILTER), is(false));
        assertThat(variantEvaluation.passedFilter(FilterType.PATHOGENICITY_FILTER), is(false));
        assertThat(variantEvaluation.passedFilter(FilterType.ENTREZ_GENE_ID_FILTER), is(false));
    }
}
