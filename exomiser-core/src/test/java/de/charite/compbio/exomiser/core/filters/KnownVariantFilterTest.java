/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.VariantEvaluation.VariantBuilder;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class KnownVariantFilterTest {

    private final VariantFilter instance = new KnownVariantFilter();
    
    private final FilterResult PASS_RESULT = new PassFilterResult(FilterType.KNOWN_VARIANT_FILTER, 1f);
    private final FilterResult FAIL_RESULT = new FailFilterResult(FilterType.KNOWN_VARIANT_FILTER, 0f);
    
    private VariantEvaluation buildVariantWithFrequencyData(FrequencyData frequencyData) {
        return new VariantBuilder(1, 1, "A", "T").frequencyData(frequencyData).build();
    }

    @Test
    public void testVariantType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
    }

    @Test
    public void testRunFilter_ReturnsFailResultWhenFilteringVariantWithRsId() {
        FrequencyData frequencyData = new FrequencyData(new RsId(12345));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }
    
    @Test
    public void testRunFilter_ReturnsFailResultWhenFilteringVariantWithKnownFrequency() {
        FrequencyData frequencyData = new FrequencyData(null, new Frequency(1f));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }

    @Test
    public void testRunFilter_ReturnsPassResultWhenFilteringVariantWithNoRepresentationInDatabase() {
        FrequencyData frequencyData = new FrequencyData();
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(PASS_RESULT));
    }
    
}
