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

package org.monarchinitiative.exomiser.core.analysis;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class JobReaderTest {

    @Test
    void readNonJobFileThrowsException() {
        Path someRandomVcfFile = Paths.get("src/test/resources/headerOnly.vcf");
        assertThrows(IllegalArgumentException.class, () -> JobReader.readJob(someRandomVcfFile));
    }

    @Test
    void readLegacyJobFromFile() {
        Path legacyAnalysisFile = Paths.get("src/test/resources/job/pfeiffer_analysis_v8_12.yml");
        JobProto.Job output = JobReader.readJob(legacyAnalysisFile);
        assertThat(output, equalTo(TestJobs.pfeifferSampleExomeJob()));
    }

    @Test
    void readSampleJobFromFile() {
        Path legacyAnalysisFile = Paths.get("src/test/resources/job/pfeiffer_job_sample.yml");
        JobProto.Job output = JobReader.readJob(legacyAnalysisFile);
        assertThat(output, equalTo(TestJobs.pfeifferSampleExomeJob()));
    }

    @Test
    void readPhenopacketJobFromFile() {
        Path legacyAnalysisFile = Paths.get("src/test/resources/job/pfeiffer_job_phenopacket.yml");
        JobProto.Job output = JobReader.readJob(legacyAnalysisFile);
        assertThat(output, equalTo(TestJobs.pfeifferPhenopacketExomeJob()));
    }

    @Test
    void readLegacyJobFromString() {
        String legacyAnalysis = "analysis:\n" +
                "  vcf: Pfeiffer.vcf\n" +
                "  genomeAssembly: hg19\n" +
                "  hpoIds: ['HP:0000001', 'HP:0000002']\n" +
                "  analysisMode: FULL\n" +
                "outputOptions:\n" +
                "  outputPrefix: results/Pfeiffer-hiphive-exome\n" +
                "  #out-format options: HTML, JSON, TSV_GENE, TSV_VARIANT, VCF (default: HTML)\n" +
                "  outputFormats: [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]";

        SampleProto.Sample sample = SampleProto.Sample.newBuilder()
                .setVcf("Pfeiffer.vcf")
                .setGenomeAssembly("hg19")
                .setPed("")
                .addAllHpoIds(List.of("HP:0000001", "HP:0000002"))
                .build();

        AnalysisProto.Analysis analysis = AnalysisProto.Analysis.newBuilder()
                .setAnalysisMode(AnalysisProto.AnalysisMode.valueOf("FULL"))
                .build();

        OutputProto.OutputOptions outputOptions = OutputProto.OutputOptions.newBuilder()
                .setOutputPrefix("results/Pfeiffer-hiphive-exome")
                .addAllOutputFormats(List.of("HTML", "JSON", "TSV_GENE", "TSV_VARIANT", "VCF"))
                .build();

        JobProto.Job expected = JobProto.Job.newBuilder()
                .setSample(sample)
                .setAnalysis(analysis)
                .setOutputOptions(outputOptions)
                .build();

        assertThat(JobReader.readJob(legacyAnalysis), equalTo(expected));
    }
}