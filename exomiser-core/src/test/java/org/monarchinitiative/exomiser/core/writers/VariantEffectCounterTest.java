/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantEffectCounterTest {


    @Test
    void singleSampleNoVariantSingleType() {
        VariantEffectCounter instance = new VariantEffectCounter(ImmutableList.of("Arthur"), ImmutableList.of());
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        System.out.println(result);
        assertThat(result, equalTo(ImmutableList.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList
                .of(0)))));
    }

    @Test
    void multiSampleNoVariantSingleType() {
        VariantEffectCounter instance = new VariantEffectCounter(ImmutableList.of("Arthur", "Ford"), ImmutableList.of());
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        System.out.println(result);
        assertThat(result, equalTo(ImmutableList.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList
                .of(0,0)))));
    }


    @Test
    void singleSampleSingleVariantSingleType() {

        VariantEvaluation missense = VariantEvaluation.builder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(ImmutableMap.of("Arthur", SampleGenotype.het()))
                .build();

        VariantEffectCounter instance = new VariantEffectCounter(ImmutableList.of("Arthur"), ImmutableList.of(missense));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        System.out.println(result);
        assertThat(result, equalTo(ImmutableList.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList
                .of(1)))));
    }

    @Test
    void multiSampleSingleVariantSingleType() {

        ImmutableMap<String, SampleGenotype> sampleGenotypes = ImmutableMap.of(
                "Arthur", SampleGenotype.het(),
                "Zaphod", SampleGenotype.homRef(),
                "Trillian", SampleGenotype.homAlt()
        );
        VariantEvaluation missense = VariantEvaluation.builder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(sampleGenotypes)
                .build();

        VariantEffectCounter instance = new VariantEffectCounter(sampleGenotypes.keySet()
                .asList(), ImmutableList.of(missense));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        System.out.println(result);
        assertThat(result, equalTo(ImmutableList.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList
                .of(1, 0, 1)))));
    }

    @Test
    void multiSampleMultiVariantSingleType() {

        ImmutableMap<String, SampleGenotype> missenseOneSampleGenotypes = ImmutableMap.of(
                "Arthur", SampleGenotype.het(),
                "Zaphod", SampleGenotype.homRef(),
                "Trillian", SampleGenotype.homAlt()
        );
        VariantEvaluation missenseOne = VariantEvaluation.builder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseOneSampleGenotypes)
                .build();

        ImmutableMap<String, SampleGenotype> missenseTwoSampleGenotypes = ImmutableMap.of(
                "Arthur", SampleGenotype.het(),
                "Zaphod", SampleGenotype.het(),
                "Trillian", SampleGenotype.homRef()
        );
        VariantEvaluation missenseTwo = VariantEvaluation.builder(2, 54321, "C", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseTwoSampleGenotypes)
                .build();


        VariantEffectCounter instance = new VariantEffectCounter(missenseOneSampleGenotypes.keySet()
                .asList(), ImmutableList.of(missenseOne, missenseTwo));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        System.out.println(result);
        assertThat(result, equalTo(ImmutableList.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList
                .of(2, 1, 1)))));
    }

    @Test
    void multiSampleMultiVariantMultiType() {

        ImmutableMap<String, SampleGenotype> missenseOneSampleGenotypes = ImmutableMap.of(
                "Arthur", SampleGenotype.het(),
                "Zaphod", SampleGenotype.homRef(),
                "Trillian", SampleGenotype.homAlt()
        );
        VariantEvaluation missenseOne = VariantEvaluation.builder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseOneSampleGenotypes)
                .build();

        ImmutableMap<String, SampleGenotype> stopGainedSampleGenotypes = ImmutableMap.of(
                "Arthur", SampleGenotype.het(),
                "Zaphod", SampleGenotype.het(),
                "Trillian", SampleGenotype.homRef()
        );
        VariantEvaluation stopGained = VariantEvaluation.builder(2, 54321, "C", "G")
                .variantEffect(VariantEffect.STOP_GAINED)
                .sampleGenotypes(stopGainedSampleGenotypes)
                .build();


        VariantEffectCounter instance = new VariantEffectCounter(missenseOneSampleGenotypes.keySet()
                .asList(), ImmutableList.of(missenseOne, stopGained));

        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT, VariantEffect.STOP_GAINED));
        System.out.println(result);

        ImmutableList<VariantEffectCount> expected = ImmutableList.of(
                new VariantEffectCount(VariantEffect.STOP_GAINED, ImmutableList.of(1, 1, 0)),
                new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, ImmutableList.of(1, 0, 1))
        );
        assertThat(result, equalTo(expected));
    }
}