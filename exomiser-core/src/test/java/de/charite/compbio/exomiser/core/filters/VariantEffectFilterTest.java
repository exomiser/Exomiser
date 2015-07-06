/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEffectFilterTest {

    private VariantEffectFilter instance;
    private Set<VariantEffect> offTargetVariantEffects;

    private VariantEvaluation missensePassesFilter;
    private VariantEvaluation synonymousFailsFilter;

    @Before
    public void setUp() {
        offTargetVariantEffects = EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT);

        instance = new VariantEffectFilter(offTargetVariantEffects);

        missensePassesFilter = testVariantBuilder().variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        synonymousFailsFilter = testVariantBuilder().variantEffect(VariantEffect.SYNONYMOUS_VARIANT).build();
    }

    private VariantEvaluation.VariantBuilder testVariantBuilder() {
        return new VariantEvaluation.VariantBuilder(1, 1, "A", "T");
    }

    @Test
    public void testGetOffTargetTypes() {
        assertThat(instance.getOffTargetVariantTypes(), equalTo(offTargetVariantEffects));
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.VARIANT_EFFECT_FILTER));
    }

    @Test
    public void testMissenseVariantPassesFilter() {
        FilterResult filterResult = instance.runFilter(missensePassesFilter);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testSynonymousVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(synonymousFailsFilter);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherFilter() {
        PathogenicityFilter obj = new PathogenicityFilter(true);
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testEqualOtherTagetFilter() {
        VariantEffectFilter obj = new VariantEffectFilter(offTargetVariantEffects);
        assertThat(instance.equals(obj), is(true));
    }

    @Test
    public void testHashCode() {
        VariantEffectFilter anotherTargetFilter = new VariantEffectFilter(offTargetVariantEffects);
        assertThat(instance.hashCode(), equalTo(anotherTargetFilter.hashCode()));
    }
}
