/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.phenopackets.schema.v1.core.Sex;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SampleProtoConverterTest {

    private final SampleProtoConverter instance = new SampleProtoConverter();

    private final SampleProto.Sample.Builder protoSample = SampleProto.Sample.newBuilder()
            .setProband(TestPedigrees.affectedChild().getId())
            .addAllHpoIds(List.of("HP:0000001"))
            .setGenomeAssembly("hg19")
            .setVcf("")
            .setSex(Sex.MALE)
            .setAge(SampleProto.Age.newBuilder().setYears(3).setMonths(4).setDays(5).build())
            .setPed(TestPedigrees.trioChildAffectedPedPath().toString());

    private final Sample.Builder sample = Sample.builder()
            .probandSampleName(TestPedigrees.affectedChild().getId())
            .hpoIds(List.of("HP:0000001"))
            .genomeAssembly(GenomeAssembly.HG19)
            .sex(Pedigree.Individual.Sex.MALE)
            .age(Age.of(3, 4, 5))
            .pedigree(TestPedigrees.trioChildAffected());

    @Test
    void toProto() {
        org.phenopackets.schema.v1.core.Pedigree pedigree = PhenopacketPedigreeConverter.toPhenopacketPedigree(TestPedigrees
                .trioChildAffected());
        assertThat(instance.toProto(sample.build()), equalTo(protoSample.clearPed().setPedigree(pedigree).build()));
    }

    @Test
    void toDomainDefaultInstance() {
        assertThat(instance.toDomain(SampleProto.Sample.getDefaultInstance()), equalTo(Sample.builder().build()));
    }

    @Test
    void toDomain() {
        assertThat(instance.toDomain(protoSample.build()), equalTo(sample.build()));
    }

    @Test
    void toDomainNoAge() {
        SampleProto.Sample protoSampleNoAge = protoSample
                .clearAge()
                .build();

        Sample sampleNoAge = sample
                .age(Age.unknown())
                .build();

        assertThat(instance.toDomain(protoSampleNoAge), equalTo(sampleNoAge));
    }

    @Test
    void toDomainNoSexNoPed() {
        SampleProto.Sample protoSampleNoSex = protoSample
                .clearSex()
                .clearPedigreeData()
                .build();

        Sample sampleNoSex = sample
                .sex(Pedigree.Individual.Sex.UNKNOWN)
                .pedigree(Pedigree.empty())
                .build();

        assertThat(instance.toDomain(protoSampleNoSex), equalTo(sampleNoSex));
    }

    @Test
    void toDomainGenomeAssemblyDefault() {
        SampleProto.Sample protoSampleNoGenomeAssembly = protoSample
                .clearGenomeAssembly()
                .build();
        assertThat(protoSampleNoGenomeAssembly.getGenomeAssembly(), equalTo(""));

        Sample converted = instance.toDomain(protoSampleNoGenomeAssembly);
        assertThat(converted.getGenomeAssembly(), equalTo(GenomeAssembly.defaultBuild()));
    }
}