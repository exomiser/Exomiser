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
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AnalysisConverterTest {

    private final GenomeAssembly hg38 = GenomeAssembly.HG38;
    private final Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
    private final String probandSampleName = "manuel";
    private final List<String> hpoIds = List.of("HP:0000001");

    @Test
    void testAnalysisJustPhenotype() {
        Analysis analysis = Analysis.builder()
//                .genomeAssembly(hg38)
//                .vcfPath(vcfPath)
//                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)
                .build();

        Sample sample = AnalysisConverter.toSample(analysis);

        assertThat(sample.getGenomeAssembly(), equalTo(GenomeAssembly.defaultBuild()));
        assertThat(sample.getVcfPath(), equalTo(null));
        assertThat(sample.getProbandSampleName(), equalTo(""));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));
        assertThat(sample.getAge(), equalTo(Age.unknown()));
        assertThat(sample.getSex(), equalTo(Pedigree.Individual.Sex.UNKNOWN));
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
    }

    @Test
    void testAnalysisNoPedigree() {
        Analysis analysis = Analysis.builder()
                .genomeAssembly(hg38)
                .vcfPath(vcfPath)
                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)
                .build();

        Sample sample = AnalysisConverter.toSample(analysis);

        assertThat(sample.getGenomeAssembly(), equalTo(hg38));
        assertThat(sample.getVcfPath(), equalTo(vcfPath));
        assertThat(sample.getProbandSampleName(), equalTo(probandSampleName));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));
        assertThat(sample.getAge(), equalTo(Age.unknown()));
        assertThat(sample.getSex(), equalTo(Pedigree.Individual.Sex.UNKNOWN));
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
    }

    @Test
    void testAnalysisJustProbandPedigree() {
        Pedigree pedigree = Pedigree.justProband(probandSampleName);

        Analysis analysis = Analysis.builder()
                .genomeAssembly(hg38)
                .vcfPath(vcfPath)
                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)
                .pedigree(pedigree)
                .build();

        Sample sample = AnalysisConverter.toSample(analysis);

        assertThat(sample.getGenomeAssembly(), equalTo(hg38));
        assertThat(sample.getVcfPath(), equalTo(vcfPath));
        assertThat(sample.getProbandSampleName(), equalTo(probandSampleName));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));
        assertThat(sample.getAge(), equalTo(Age.unknown()));
        assertThat(sample.getSex(), equalTo(Pedigree.Individual.Sex.UNKNOWN));
        assertThat(sample.getPedigree(), equalTo(pedigree));
    }

    @Test
    void testAnalysisProbandOnlyPedigree() {
        Pedigree pedigree = Pedigree.of(Pedigree.Individual.builder()
                .id(probandSampleName)
                .sex(Pedigree.Individual.Sex.MALE)
                .status(Pedigree.Individual.Status.AFFECTED)
                .build());

        Analysis analysis = Analysis.builder()
                .genomeAssembly(hg38)
                .vcfPath(vcfPath)
                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)
                .pedigree(pedigree)
                .build();

        Sample sample = AnalysisConverter.toSample(analysis);

        assertThat(sample.getGenomeAssembly(), equalTo(hg38));
        assertThat(sample.getVcfPath(), equalTo(vcfPath));
        assertThat(sample.getProbandSampleName(), equalTo(probandSampleName));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));
        assertThat(sample.getAge(), equalTo(Age.unknown()));
        assertThat(sample.getSex(), equalTo(Pedigree.Individual.Sex.MALE));
        assertThat(sample.getPedigree(), equalTo(pedigree));
    }

    @Test
    void testAnalysisProbandNotInPedigree() {
        Pedigree pedigree = Pedigree.of(Pedigree.Individual.builder()
                .id("Bart")
                .sex(Pedigree.Individual.Sex.MALE)
                .status(Pedigree.Individual.Status.AFFECTED)
                .build());

        Analysis analysis = Analysis.builder()
                .genomeAssembly(hg38)
                .vcfPath(vcfPath)
                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)
                .pedigree(pedigree)
                .build();

        assertThrows(IllegalArgumentException.class, () -> AnalysisConverter.toSample(analysis));
    }
}