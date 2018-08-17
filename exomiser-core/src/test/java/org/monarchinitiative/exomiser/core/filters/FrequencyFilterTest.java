/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.filters;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FrequencyFilterTest {

    private static final FilterResult PASS_FREQUENCY_FILTER_RESULT = FilterResult.pass(FilterType.FREQUENCY_FILTER);
    private static final FilterResult FAIL_FREQUENCY_FILTER_RESULT = FilterResult.fail(FilterType.FREQUENCY_FILTER);

    private FrequencyFilter instance;

    private static final float FREQ_THRESHOLD = 0.1f;

    @Before
    public void setUp() throws Exception {
        instance = new FrequencyFilter(FREQ_THRESHOLD);
    }

    private Frequency passFrequency(FrequencySource source) {
        return Frequency.valueOf(FREQ_THRESHOLD - 0.02f, source);
    }

    private Frequency failFrequency(FrequencySource source) {
        return Frequency.valueOf(FREQ_THRESHOLD + 1.0f, source);
    }

    private VariantEvaluation makeVariantEvaluation(Frequency... frequencies) {
        return VariantEvaluation.builder(1, 1, "A", "T")
                .frequencyData(FrequencyData.of(frequencies))
                .build();
    }

    @Test
    public void getMaxFrequencyCutoff() {
        assertThat(instance.getMaxFreq(), equalTo(FREQ_THRESHOLD));
    } 
    
    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithNegativeFrequency() {
        instance = new FrequencyFilter(-1f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithFrequencyGreaterThanOneHundredPercent() {
        instance = new FrequencyFilter(101f);
    }

    @Test
    public void testFilterPassesVariantEvaluationWithNoFrequencyData() {
        VariantEvaluation variantEvaluation = makeVariantEvaluation();
        assertThat(instance.runFilter(variantEvaluation), equalTo(PASS_FREQUENCY_FILTER_RESULT));
    }

    @Test
    public void testFilterPassesVariantEvaluationWithFrequencyUnderThreshold() {
        VariantEvaluation variantEvaluation = makeVariantEvaluation(passFrequency(FrequencySource.ESP_ALL));
        assertThat(instance.runFilter(variantEvaluation), equalTo(PASS_FREQUENCY_FILTER_RESULT));
    }

    @Test
    public void testFilterPassesVariantEvaluationWithAllFrequenciesUnderThreshold() {
        VariantEvaluation variantEvaluation = makeVariantEvaluation(
                passFrequency(FrequencySource.ESP_ALL),
                passFrequency(FrequencySource.THOUSAND_GENOMES)
        );
        assertThat(instance.runFilter(variantEvaluation), equalTo(PASS_FREQUENCY_FILTER_RESULT));
    }

    @Test
    public void testFilterFailsVariantEvaluationWithFrequencyOverThreshold() {
        VariantEvaluation variantEvaluation = makeVariantEvaluation(failFrequency(FrequencySource.ESP_ALL));
        assertThat(instance.runFilter(variantEvaluation), equalTo(FAIL_FREQUENCY_FILTER_RESULT));
    }

    @Test
    public void testFilterFailsVariantEvaluationWithAtLeastFrequencyOverThreshold() {
        VariantEvaluation variantEvaluation = makeVariantEvaluation(
                passFrequency(FrequencySource.THOUSAND_GENOMES),
                failFrequency(FrequencySource.ESP_ALL)
        );
        assertThat(instance.runFilter(variantEvaluation), equalTo(FAIL_FREQUENCY_FILTER_RESULT));
    }

    @Test
    public void testHashCode() {
        FrequencyFilter otherFilter = new FrequencyFilter(FREQ_THRESHOLD);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherObject() {
        Object obj = "Not equal to this";
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherFrequencyFilterWithDifferentThreshold() {
        FrequencyFilter otherFilter = new FrequencyFilter(FREQ_THRESHOLD + 1f);
        assertThat(instance.equals(otherFilter), is(false));
    }

    @Test
    public void testEqualsSelf() {
        assertThat(instance.equals(instance), is(true));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
        assertThat(instance.toString().isEmpty(), is(false));
    }

}
