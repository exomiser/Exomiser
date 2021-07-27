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

package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.SampleData;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.SampleGenotypes;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantEffectCounterTest {


    private final String arthur = "Arthur";
    private final String zaphod = "Zaphod";
    private final String trillian = "Trillian";

    private final List<String> sampleNames = List.of(arthur, zaphod, trillian);

    @Test
    void singleSampleNoVariantSingleType() {
        VariantEffectCounter instance = new VariantEffectCounter(List.of("Arthur"), List.of());
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        assertThat(result, equalTo(List.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(0)))));
    }

    @Test
    void multiSampleNoVariantSingleType() {
        VariantEffectCounter instance = new VariantEffectCounter(List.of("Arthur", "Ford"), List.of());
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        assertThat(result, equalTo(List.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(0, 0)))));
    }

    @Test
    void singleSampleSingleVariantSingleType() {
        VariantEvaluation missense = TestFactory.variantBuilder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(arthur, SampleGenotype.het()))
                .build();

        VariantEffectCounter instance = new VariantEffectCounter(List.of("Arthur"), List.of(missense));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        assertThat(result, equalTo(List.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(1)))));
    }

    @Test
    void multiSampleSingleVariantSingleType() {

        SampleGenotypes sampleGenotypes = SampleGenotypes.of(
                arthur, SampleGenotype.het(),
                zaphod, SampleGenotype.homRef(),
                trillian, SampleGenotype.homAlt()
        );
        VariantEvaluation missense = TestFactory.variantBuilder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(sampleGenotypes)
                .build();

        VariantEffectCounter instance = new VariantEffectCounter(sampleNames, List.of(missense));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        assertThat(result, equalTo(List.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(1, 0, 1)))));
    }

    @Test
    void multiSampleMultiVariantSingleType() {

        SampleGenotypes missenseOneSampleGenotypes = SampleGenotypes.of(
                arthur, SampleGenotype.het(),
                zaphod, SampleGenotype.homRef(),
                trillian, SampleGenotype.homAlt()
        );
        VariantEvaluation missenseOne = TestFactory.variantBuilder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseOneSampleGenotypes)
                .build();

        SampleGenotypes missenseTwoSampleGenotypes = SampleGenotypes.of(
                SampleData.of(arthur, SampleGenotype.het()),
                SampleData.of(zaphod, SampleGenotype.het()),
                SampleData.of(trillian, SampleGenotype.homRef())
        );
        VariantEvaluation missenseTwo = TestFactory.variantBuilder(2, 54321, "C", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseTwoSampleGenotypes)
                .build();


        VariantEffectCounter instance = new VariantEffectCounter(sampleNames, List.of(missenseOne, missenseTwo));
        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT));
        assertThat(result, equalTo(List.of(new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(2, 1, 1)))));
    }

    @Test
    void multiSampleMultiVariantMultiType() {

        SampleGenotypes missenseOneSampleGenotypes = SampleGenotypes.of(
                arthur, SampleGenotype.het(),
                zaphod, SampleGenotype.homRef(),
                trillian, SampleGenotype.homAlt()
        );
        VariantEvaluation missenseOne = TestFactory.variantBuilder(1, 12345, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(missenseOneSampleGenotypes)
                .build();

        SampleGenotypes stopGainedSampleGenotypes = SampleGenotypes.of(
                arthur, SampleGenotype.het(),
                zaphod, SampleGenotype.het(),
                trillian, SampleGenotype.homRef()
        );
        VariantEvaluation stopGained = TestFactory.variantBuilder(2, 54321, "C", "G")
                .variantEffect(VariantEffect.STOP_GAINED)
                .sampleGenotypes(stopGainedSampleGenotypes)
                .build();


        VariantEffectCounter instance = new VariantEffectCounter(sampleNames, List.of(missenseOne, stopGained));

        List<VariantEffectCount> result = instance.getVariantEffectCounts(EnumSet.of(VariantEffect.MISSENSE_VARIANT, VariantEffect.STOP_GAINED));

        List<VariantEffectCount> expected = List.of(
                new VariantEffectCount(VariantEffect.STOP_GAINED, List.of(1, 1, 0)),
                new VariantEffectCount(VariantEffect.MISSENSE_VARIANT, List.of(1, 0, 1))
        );
        assertThat(result, equalTo(expected));
    }
}