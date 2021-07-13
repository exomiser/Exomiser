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

package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.FEMALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.AFFECTED;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.UNAFFECTED;
import static org.monarchinitiative.exomiser.core.model.Pedigree.justProband;

class IncompletePenetranceAlleleCalculatorTest {

    private final Contig chr1 = GenomicAssemblies.GRCh37p13().contigById(1);
    private final Pedigree twoAffected = Pedigree.of(
            Individual.builder().id("proband").motherId("affectedMother").fatherId("unaffectedFather").sex(MALE).status(AFFECTED).build(),
            Individual.builder().id("affectedMother").sex(FEMALE).status(AFFECTED).build(),
            Individual.builder().id("unaffectedFather").sex(MALE).status(UNAFFECTED).build()
    );

    @Test
    void findCompatibleVariantsSingleton() {
        Pedigree singleton = justProband("proband");

        // TODO - make a testVariantbuilder to use chr, pos etc...
        VariantEvaluation variant = VariantEvaluation.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "A", "T")
                .sampleGenotypes(Map.of("proband", SampleGenotype.het()))
                .build();

        IncompletePenetranceAlleleCalculator instance = new IncompletePenetranceAlleleCalculator(singleton);
        assertThat(instance.findCompatibleVariants(List.of(variant)), equalTo(List.of(variant)));
    }

    @Test
    void findCompatibleVariantsTrio() {
        Map<String, SampleGenotype> sampleGenotypes = Map.of(
                "proband", SampleGenotype.het(),
                "affectedMother", SampleGenotype.homAlt(),
                "unaffectedFather", SampleGenotype.homRef()
        );

        VariantEvaluation variant = VariantEvaluation.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "A", "T")
                .sampleGenotypes(sampleGenotypes)
                .build();

        IncompletePenetranceAlleleCalculator instance = new IncompletePenetranceAlleleCalculator(twoAffected);
        assertThat(instance.findCompatibleVariants(List.of(variant)), equalTo(List.of(variant)));
    }

    @Test
    void canBePresentInUnaffected() {
        Map<String, SampleGenotype> sampleGenotypes = Map.of(
                "proband", SampleGenotype.het(),
                "affectedMother", SampleGenotype.homAlt(),
                "unaffectedFather", SampleGenotype.het()
        );

        VariantEvaluation variant = VariantEvaluation.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "A", "T")
                .sampleGenotypes(sampleGenotypes)
                .build();

        IncompletePenetranceAlleleCalculator instance = new IncompletePenetranceAlleleCalculator(twoAffected);
        assertThat(instance.findCompatibleVariants(List.of(variant)), equalTo(List.of(variant)));
    }

    @Test
    void mustBePresentInAllAffected() {
        Map<String, SampleGenotype> sampleGenotypes = Map.of(
                "proband", SampleGenotype.het(),
                "affectedMother", SampleGenotype.homRef(),
                "unaffectedFather", SampleGenotype.het()
        );

        VariantEvaluation variant = VariantEvaluation.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "A", "T")
                .sampleGenotypes(sampleGenotypes)
                .build();

        IncompletePenetranceAlleleCalculator instance = new IncompletePenetranceAlleleCalculator(twoAffected);
        assertThat(instance.findCompatibleVariants(List.of(variant)), equalTo(List.of()));
    }
}