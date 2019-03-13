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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class KnownVariantFilterTest {

    private final VariantFilter instance = new KnownVariantFilter();
    
    private final FilterResult PASS_RESULT = new PassFilterResult(FilterType.KNOWN_VARIANT_FILTER);
    private final FilterResult FAIL_RESULT = new FailFilterResult(FilterType.KNOWN_VARIANT_FILTER);
    
    private VariantEvaluation buildVariantWithFrequencyData(FrequencyData frequencyData) {
        return VariantEvaluation.builder(1, 1, "A", "T").frequencyData(frequencyData).build();
    }

    @Test
    public void testVariantType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
    }

    @Test
    public void testRunFilterReturnsFailResultWhenFilteringVariantWithRsId() {
        FrequencyData frequencyData = FrequencyData.of(RsId.of(12345));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }
    
    @Test
    public void testRunFilterReturnsFailResultWhenFilteringVariantWithKnownFrequency() {
        FrequencyData frequencyData = FrequencyData.of(RsId.empty(), Frequency.of(FrequencySource.THOUSAND_GENOMES, 1f));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }

    @Test
    public void testRunFilterReturnsPassResultWhenFilteringVariantWithNoRepresentationInDatabase() {
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(FrequencyData.empty());
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(PASS_RESULT));
    }
    
}
