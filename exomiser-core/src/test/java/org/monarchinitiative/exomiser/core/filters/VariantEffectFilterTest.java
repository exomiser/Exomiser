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
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testSynonymousVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(synonymousFailsFilter);

        FilterTestHelper.assertFailed(filterResult);
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
