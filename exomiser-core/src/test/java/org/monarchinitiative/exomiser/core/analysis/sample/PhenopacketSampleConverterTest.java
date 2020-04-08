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
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PhenopacketSampleConverterTest {

    public final String subjectId = "manuel";

    private final Individual subject = Individual.newBuilder()
            .setId(subjectId)
            .setAgeAtCollection(org.phenopackets.schema.v1.core.Age.newBuilder().setAge("P3Y").build())
            .build();

    private final Path vcfFile = Paths.get("src/test/resources/Pfeiffer.vcf");

    private final HtsFile vcf = HtsFile.newBuilder()
            .setGenomeAssembly("GRCh37")
            .setHtsFormat(HtsFile.HtsFormat.VCF)
            .setUri(vcfFile.toUri().toString())
            .build();

    private final PhenotypicFeature craniosynostosis = PhenotypicFeature.newBuilder()
            .setType(OntologyClass.newBuilder().setId("HP:0001363").setLabel("Craniosynostosis").build())
            .build();

    private final Phenopacket phenopacket = Phenopacket.newBuilder()
            .setSubject(subject)
            .addPhenotypicFeatures(craniosynostosis)
            .addHtsFiles(vcf)
            .build();

    @Test
    void wellFormedPhenopacket() {
        Sample instance = PhenopacketSampleConverter.toExomiserSample(phenopacket);

        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
        assertThat(instance.getVcfPath(), equalTo(vcfFile.toAbsolutePath()));
        assertThat(instance.getProbandSampleName(), equalTo(subjectId));
        assertThat(instance.getPedigree(), equalTo(Pedigree.justProband(subjectId)));
        assertThat(instance.getAge(), equalTo(Age.of(3, 0, 0)));
        assertThat(instance.getHpoIds(), equalTo(List.of("HP:0001363")));
    }

    @Test
    void convertFamilyProbandOnlyNoPedigree() {
        Family family = Family.newBuilder()
                .setProband(phenopacket)
                .build();

        Sample expected = Sample.builder()
                .genomeAssembly(GenomeAssembly.HG19)
                .vcfPath(vcfFile.toAbsolutePath())
                .probandSampleName(subjectId)
                .pedigree(Pedigree.justProband(subjectId))
                .age(Age.of(3, 0, 0))
                .hpoIds(List.of("HP:0001363"))
                .build();
        assertThat(PhenopacketSampleConverter.toExomiserSample(family), equalTo(expected));
    }

    @Test
    void convertFamilyProbandWithPedigree() {
        Family family = Family.newBuilder()
                .setProband(phenopacket)
                .setPedigree(org.phenopackets.schema.v1.core.Pedigree.newBuilder()
                        .addPersons(org.phenopackets.schema.v1.core.Pedigree.Person.newBuilder()
                                .setIndividualId(subjectId)
                                .setSex(Sex.MALE)
                                .setAffectedStatus(org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus.AFFECTED)
                                .build())
                        .build())
                .build();

        Pedigree.Individual individual = Pedigree.Individual.builder()
                .id(subjectId)
                .sex(Pedigree.Individual.Sex.MALE)
                .status(Pedigree.Individual.Status.AFFECTED)
                .build();

        Sample expected = Sample.builder()
                .genomeAssembly(GenomeAssembly.HG19)
                .vcfPath(vcfFile.toAbsolutePath())
                .probandSampleName(subjectId)
                .pedigree(Pedigree.of(individual))
                .age(Age.of(3, 0, 0))
                .hpoIds(List.of("HP:0001363"))
                .build();
        assertThat(PhenopacketSampleConverter.toExomiserSample(family), equalTo(expected));
    }

    @Test
    void convertFamilyProbandWithMultiSampleVcf() {
        Family family = Family.newBuilder()
                .setProband(phenopacket)
                .addHtsFiles(HtsFile.newBuilder()
                        .setHtsFormat(HtsFile.HtsFormat.VCF)
                        .setGenomeAssembly("GRCh38")
                        .setUri("file:///data/multisample.vcf.gz")
                        .build())
                .build();

        Sample expected = Sample.builder()
                .genomeAssembly(GenomeAssembly.HG38)
                .vcfPath(Paths.get("/data/multisample.vcf.gz"))
                .probandSampleName(subjectId)
                .pedigree(Pedigree.justProband(subjectId))
                .age(Age.of(3, 0, 0))
                .hpoIds(List.of("HP:0001363"))
                .build();
        assertThat(PhenopacketSampleConverter.toExomiserSample(family), equalTo(expected));
    }
}