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

package org.monarchinitiative.exomiser.cli.commands.batch;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.monarchinitiative.exomiser.cli.pico.CommandParserResult;
import org.phenopackets.schema.v1.core.HtsFile;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.monarchinitiative.exomiser.cli.commands.TestData.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class BatchFileReaderTest {

    @Test
    void testAnalyseCommand() {
        var commandLine = new CommandLine(new AnalyseCommand()).setCaseInsensitiveEnumValuesAllowed(true);
        var commandParser = new CommandParser<AnalyseCommand>(commandLine);
        String line = "--sample examples/pfeiffer-phenopacket.yml --vcf examples/Pfeiffer.vcf.gz --assembly hg19 --preset exome";
        CommandParserResult<AnalyseCommand> commandParserResult = commandParser.parseArgs(line.split("\\s+"));
        assertThat(commandParserResult.isCommand(), is(true));
    }

    @Test
    void testReadJobsFromBatchFile() {
        List<JobProto.Job> jobs = BatchFileReader.readJobsFromBatchFile(Path.of("src/test/resources/test-analysis-batch-commands.txt"));
        assertThat(jobs.size(), equalTo(5));
        // --sample src/test/resources/pfeiffer-phenopacket.json
        JobProto.Job phenopacketExomePresetJob = JobProto.Job.newBuilder()
                .setPhenopacket(PHENOPACKET)
                .setPreset(AnalysisProto.Preset.EXOME)
                .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                .build();

        // --sample src/test/resources/pfeiffer-phenopacket.json --vcf src/test/resources/Pfeiffer.vcf --assembly hg19
        Path vcfPath = Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath();
        HtsFile htsFile = HtsFile.newBuilder().setUri(vcfPath.toUri().toString()).setHtsFormat(HtsFile.HtsFormat.VCF).setGenomeAssembly("GRCh37").build();
        JobProto.Job expected = JobProto.Job.newBuilder()
                .setPhenopacket(PHENOPACKET.toBuilder().setHtsFiles(0, htsFile).build())
                .setPreset(AnalysisProto.Preset.EXOME)
                .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                .build();

        // --sample src/test/resources/pfeiffer-sample.json    --vcf src/test/resources/Pfeiffer.vcf --assembly hg19
        JobProto.Job updateSampleJsonHg19AssemblyNoAnalysisOption = JobProto.Job.newBuilder()
                .setSample(SAMPLE.toBuilder().setVcf(vcfPath.toString()).setGenomeAssembly("GRCh37").build())
                .setPreset(AnalysisProto.Preset.EXOME)
                .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                .build();

        // --sample src/test/resources/pfeiffer-sample.yml --vcf src/test/resources/Pfeiffer.vcf --assembly GRCh37 --preset GENOME
        JobProto.Job updateSampleWithGenomePreset = JobProto.Job.newBuilder()
                .setSample(SAMPLE.toBuilder().setVcf(vcfPath.toString()).setGenomeAssembly("GRCh37").build())
                .setPreset(AnalysisProto.Preset.GENOME)
                .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                .build();

        // --analysis src/test/resources/pfeiffer-analysis-v8-12.yml
        JobProto.Job oldAnalysis = PFEIFFER_SAMPLE_JOB;

        assertThat(jobs.size(), equalTo(5));
        assertThat(jobs.get(0), equalTo(phenopacketExomePresetJob));
        assertThat(jobs.get(1), equalTo(expected));
        assertThat(jobs.get(2), equalTo(updateSampleJsonHg19AssemblyNoAnalysisOption));
        assertThat(jobs.get(3), equalTo(updateSampleWithGenomePreset));
        assertThat(jobs.get(4), equalTo(oldAnalysis));
    }
}