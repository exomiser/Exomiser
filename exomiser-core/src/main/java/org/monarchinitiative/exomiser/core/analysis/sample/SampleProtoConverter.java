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

import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.util.PedFiles;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.proto.ProtoConverter;

import java.nio.file.Paths;

import static org.monarchinitiative.exomiser.core.analysis.sample.PhenopacketConverter.toExomiserSex;
import static org.monarchinitiative.exomiser.core.analysis.sample.PhenopacketConverter.toPhenopacketSex;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class SampleProtoConverter implements ProtoConverter<Sample, SampleProto.Sample> {

    @Override
    public SampleProto.Sample toProto(Sample sample) {
        return SampleProto.Sample.newBuilder()
                .setProband(sample.getProbandSampleName())
                .setSex(toPhenopacketSex(sample.getSex()))
                .setAge(toProtoAge(sample.getAge()))
                .addAllHpoIds(sample.getHpoIds())
                .setGenomeAssembly(sample.getGenomeAssembly().toString())
                .setVcf(sample.hasVcf() ? sample.getVcfPath().toString() : "")
                .setPedigree(toProtoPedigree(sample.getPedigree()))
                .build();
    }

    private SampleProto.Age toProtoAge(Age age) {
        return SampleProto.Age.newBuilder()
                .setYears(age.getYears())
                .setMonths(age.getMonths())
                .setDays(age.getDays())
                .build();
    }

    private org.phenopackets.schema.v1.core.Pedigree toProtoPedigree(Pedigree pedigree) {
        return PhenopacketConverter.toPhenopacketPedigree(pedigree);
    }

    @Override
    public Sample toDomain(SampleProto.Sample protoSample) {
        return Sample.builder()
                .probandSampleName(protoSample.getProband())
                .sex(toExomiserSex(protoSample.getSex()))
                .age(toDomainAge(protoSample.getAge()))
                .hpoIds(protoSample.getHpoIdsList())
                .genomeAssembly(parseGenomeAssembly(protoSample.getGenomeAssembly()))
                .vcfPath(protoSample.getVcf().isEmpty() ? null : Paths.get(protoSample.getVcf()))
                .pedigree(toDomainPedigree(protoSample))
                .build();
    }

    private GenomeAssembly parseGenomeAssembly(String protoAssembly) {
        return (protoAssembly == null || protoAssembly.isEmpty()) ? GenomeAssembly.defaultBuild() : GenomeAssembly.parseAssembly(protoAssembly);
    }

    private Age toDomainAge(SampleProto.Age age) {
        if (age.equals(SampleProto.Age.getDefaultInstance())) {
            return Age.unknown();
        }
        return Age.of(age.getYears(), age.getMonths(), age.getDays());
    }

    private Pedigree toDomainPedigree(SampleProto.Sample protoSample) {
        if (protoSample.hasPedigree()) {
            return PhenopacketConverter.toExomiserPedigree(protoSample.getPedigree());
        }
        String samplePed = protoSample.getPed();
        return samplePed.isEmpty() ? Pedigree.empty() : PedFiles.readPedigree(Paths.get(samplePed));
    }
}
