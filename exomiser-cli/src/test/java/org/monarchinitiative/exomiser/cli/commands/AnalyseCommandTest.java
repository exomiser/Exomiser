package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.monarchinitiative.exomiser.cli.pico.CommandParserResult;
import org.monarchinitiative.exomiser.core.analysis.sample.PhenopacketPedigreeReader;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.proto.ProtoParser;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.*;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import picocli.CommandLine.ParameterException;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.monarchinitiative.exomiser.cli.commands.TestData.*;
import static org.monarchinitiative.exomiser.core.writers.OutputFormat.*;

class AnalyseCommandTest {

    AnalyseCommand instance;
    CommandLine commandLine;

    @BeforeEach
    void setup() {
        instance = new AnalyseCommand();
        commandLine = new CommandLine(instance)
                .setCaseInsensitiveEnumValuesAllowed(true)
        ;
    }

    private AnalyseCommand parseAnalyseCommand(String... args) {
        CommandParser<AnalyseCommand> analyseCommandParser = new CommandParser<>(commandLine);
        CommandParserResult<AnalyseCommand> commandParserResult = analyseCommandParser.parseArgs(args);
        return commandParserResult.command();
    }

    @Test
    void hasHelp() {
        CommandLine.ParseResult parseResult = commandLine.parseArgs("-h");
        assertThat(parseResult.isUsageHelpRequested(), is(true));
    }

    private String resource(String fileName) {
        return "src/test/resources/" + fileName;
    }

    @Test
    void parseIllegalArgument() {
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs("--analysis"));
    }

    @Test
    void parseIllegalAnalysisPresetCombination() {
        assertThrows(MutuallyExclusiveArgsException.class, () -> commandLine.parseArgs(
                "--analysis", resource("pfeiffer-analysis-v8-12.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalAnalysisJobCombination() {
        assertThrows(MutuallyExclusiveArgsException.class, () -> commandLine.parseArgs(
                "--analysis", resource("pfeiffer-analysis-v8-12.yml"),
                "--job", resource("pfeiffer-job-sample.yml")));
    }

    @Test
    void parseIllegalJobPresetCombination() {
        assertThrows(MutuallyExclusiveArgsException.class, () -> commandLine.parseArgs(
                "--job", resource("pfeiffer-job-sample.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalSampleAssemblyCombination() {
        Exception exception = assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(
                "--sample", resource("pfeiffer-job-sample.yml"),
                "--assembly", "GRCh37"
        ));
        assertThat(exception.getMessage(), equalTo("Error: Missing required argument(s): --vcf=<vcfPath>"));
    }

    @Test
    void parseIllegalMissingAssemblyAnalysisVcfCombination() {
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(
                        "--analysis", resource("pfeiffer-analysis-v8-12.yml"),
                        "--vcf", resource("Pfeiffer.vcf")),
                "--assembly option required when specifying vcf!");
    }

    @Test
    void parseIllegalAssemblyValue() {
        assertThrows(ParameterException.class, () -> commandLine.parseArgs(
                        "--sample", resource("pfeiffer-job-sample.yml"),
                        "--assembly", "wibble"),
                "'wibble' is not a valid/supported genome assembly."
        );
    }

    @Test
    void readIllegalAnalysisBatchOutputNoSampleCombination() {
        // pre-15.0.0 option. This has been replaced with the 'batch' command.
        assertThrows(ParameterException.class, () -> commandLine.parseArgs(
                "--analysis-batch", "src/test/resources/batch-analysis-job.txt"
        ));
    }

    @Test
    void parseWillStopAtUnknownArgumentBefore() {
        // This happens due to the stopAtNonOptions = true argument in DefaultParser().parse(options, args, true)
        assertThrows(CommandLine.UnmatchedArgumentException.class, () -> commandLine.parseArgs(
                "-wibble", "--analysis", "analysis/path"));
    }

    @Disabled("Old behaviour - delete if still valid on release")
    @Test
    void parseWillStopAtUnknownArgumentAfter() {
        var parseResult = commandLine.parseArgs(
                "--analysis", resource("exome-analysis.yml"), "-wibble");
        assertThat(parseResult.matchedOptions().size(), equalTo(1));
        assertTrue(parseResult.hasMatchedOption("analysis"));
        assertThat(parseResult.matchedOptionValue("analysis", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

    @Test
    void parseAnalysis() {
        var parseResult = commandLine.parseArgs(
                "--analysis", resource("exome-analysis.yml"));
        assertThat(parseResult.matchedOptions().size(), equalTo(1));
        assertTrue(parseResult.hasMatchedOption("analysis"));
        assertThat(parseResult.matchedOptionValue("analysis", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

    @Test
    void readCliSampleIllegalPreset() {
        assertThrows(ParameterException.class, () -> commandLine.parseArgs(
                "--sample", "src/test/resources/pfeiffer-sample.yml",
                "--preset=wibble!"
        ));
    }

    @Test
    void parseSample() {
        var parseResult = commandLine.parseArgs(
                "--sample", resource("exome-analysis.yml"));
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

    @Test
    void parseSampleAndVcf() {
        var parseResult = commandLine.parseArgs(
                "--sample", "src/test/resources/pfeiffer-phenopacket.json",
                "--vcf", "src/test/resources/Pfeiffer.vcf",
                "--assembly", "hg19"
        );
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(Path.of(resource("pfeiffer-phenopacket.json"))));
        assertTrue(parseResult.hasMatchedOption("vcf"));
        assertThat(parseResult.matchedOptionValue("vcf", Path.of("")), equalTo(Path.of(resource("Pfeiffer.vcf"))));
        assertTrue(parseResult.hasMatchedOption("assembly"));
        assertThat(parseResult.matchedOptionValue("assembly", GenomeAssembly.HG38), equalTo(GenomeAssembly.HG19));
    }

    @Test
    void parseSamplePedAndVcf() {
        var parseResult = commandLine.parseArgs(
                "--sample", "src/test/resources/pfeiffer-phenopacket.json",
                "--ped", "src/test/resources/pfeiffer-singleton.ped",
                "--vcf", "src/test/resources/Pfeiffer.vcf",
                "--assembly", "hg19"
        );
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(Path.of(resource("pfeiffer-phenopacket.json"))));
        assertTrue(parseResult.hasMatchedOption("vcf"));
        assertThat(parseResult.matchedOptionValue("vcf", Path.of("")), equalTo(Path.of(resource("Pfeiffer.vcf"))));
        assertTrue(parseResult.hasMatchedOption("assembly"));
        assertThat(parseResult.matchedOptionValue("assembly", GenomeAssembly.HG38), equalTo(GenomeAssembly.HG19));
    }

    @Test
    void parseJob() {
        var parseResult = commandLine.parseArgs("--job", resource("exome-analysis.yml"));
        assertTrue(parseResult.hasMatchedOption("job"));
        assertThat(parseResult.matchedOptionValue("job", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

    @Test
    void parseOutputFormat() {
        var parseResult = commandLine.parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--vcf", resource("Pfeiffer.vcf"),
                "--assembly", "hg19",
                "--output-format", "TSV_GENE,JSON,HTML,TSV_VARIANT");
        assertTrue(parseResult.hasMatchedOption("output-format"));
        assertThat(parseResult.matchedOptionValue("output-format", List.of()), containsInAnyOrder(TSV_GENE, JSON, HTML, TSV_VARIANT));
    }

    @Test
    void testIllegalOutputFormatArguments() {
        Throwable error = assertThrows(ParameterException.class, () -> commandLine.parseArgs("--analysis", "src/test/resources/test-analysis-exome.yml", "--output-format", "HTML,FOO,BAR"));
        assertThat(error.getMessage(), equalTo("Invalid value for option '--output-format' (<outputFormats>): expected one of [HTML, VCF, TSV_GENE, TSV_VARIANT, JSON] (case-insensitive) but was 'FOO'"));
    }

    @Disabled("--output-prefix option has been *deprecated*")
    @Test
    void parseSampleVcfAndOutputPrefix() {
        var parseResult = commandLine.parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--vcf", resource("Pfeiffer.vcf"),
                "--assembly", "hg19",
                "--preset", "exome",
                "--output-prefix", "results/pfeiffer-exome-analysis-results"
        );
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
        assertTrue(parseResult.hasMatchedOption("vcf"));
        assertThat(parseResult.matchedOptionValue("vcf", Path.of("")), equalTo(Path.of(resource("Pfeiffer.vcf"))));
        assertTrue(parseResult.hasMatchedOption("assembly"));
        assertThat(parseResult.matchedOptionValue("assembly", GenomeAssembly.HG38), equalTo(GenomeAssembly.HG19));
        assertTrue(parseResult.hasMatchedOption("output-prefix"));
        assertThat(parseResult.matchedOptionValue("output-prefix", ""), equalTo("results/pfeiffer-exome-analysis-results"));
    }

    @Nested
    class ValidateTests {

        @Test
        void validInput() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", resource("pfeiffer-phenopacket.yml"),
                    "--analysis", resource("exome-analysis.yml"),
                    "--vcf", resource("Pfeiffer.vcf"),
                    "--assembly", "GRCh37",
                    "--output-directory", "results",
                    "--output-filename", "pfeiffer-exome-analysis-results",
                    "--output-format", "HTML,TSV_GENE,TSV_VARIANT"
            );
            assertThat(analyseCommand.validate(), equalTo(true));
        }

        @Test
        void parseIllegalJobVcfCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--job", resource("pfeiffer-job-sample.yml"),
                    "--vcf", resource("Pfeiffer.vcf"),
                    "--assembly", "GRCh37"
            );
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parseIllegalJobSampleCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--job", resource("pfeiffer-job-sample.yml"),
                    "--sample", resource("pfeiffer-family.yml")
            );
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parseInvalidJobOutputCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--job", resource("pfeiffer-job-sample.yml"),
                    "--output", resource("pfeiffer-output-options.yml"));
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parseOutputOnlyIsInvalid() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--output", "src/main/resources/examples/output-options.yml");
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parsePresetOnlyIsInvalid() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--preset", "exome");
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parseInvalidPresetOutputCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--preset", "exome",
                    "--output", resource("pfeiffer-output-options.yml"));
            assertThat(analyseCommand.validate(), equalTo(false));
        }

        @Test
        void parseInvalidOutputCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--output", resource("pfeiffer-output-options.yml"));
            assertThat(analyseCommand.validate(), equalTo(false));
        }

    }

    @Nested
    class ReadJobsTests {

        @Test
        void testOutputFormatOptionToOverwriteAnalysis() {
            // the test-analysis-exome.yml file contains all output_options and gets overwritten with HTML
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/test-analysis-exome.yml",
                    "--output-format", "HTML");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job.getOutputOptions().getOutputFormatsList(), equalTo(List.of("HTML")));
        }

        @Test
        void testMultipleOutputFormatOptionsOverwriteAnalysis() {
            // the test-analysis-exome.yml file contains all output_options and gets overwritten with HTML
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/test-analysis-exome.yml",
                    "--output-format", "TSV_GENE,TSV_VARIANT,VCF");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job.getOutputOptions().getOutputFormatsList(), containsInAnyOrder("TSV_GENE", "TSV_VARIANT", "VCF"));
        }

        @Test
        void testGivenNoOutputFormatDoesNotOverrideAnalysisWithDefaultOutputFormat() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--analysis", "src/test/resources/test-analysis-exome.yml");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job.getOutputOptions().getOutputFormatsList(), containsInAnyOrder("HTML", "TSV_GENE", "JSON", "TSV_VARIANT", "VCF"));
        }

        @Test
        void readIllegalAnalysisOutputNoSampleCombination() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            assertThrows(ParameterException.class, analyseCommand::readJob);
        }

        @Test
        void readAnalysisIncorrectInputFile() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/application.properties"
            );
            Throwable throwable = assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), equalTo("Unable to parse analysis from file src/test/resources/application.properties please check the format"));
        }

        @Test
        void readNewAnalysisIncorrectInputFile() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/application.properties",
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml"
            );
            Throwable throwable = assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), equalTo("Unable to parse analysis from file src/test/resources/application.properties please check the format"));
        }

        @Test
        void readSampleIncorrectInputFile() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--sample", "src/test/resources/application.properties"
            );
            Throwable throwable = assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), startsWith("Unable to parse sample from file "));
        }

        @Test
        void readCliAnalysisLegacy() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--analysis", "src/test/resources/pfeiffer-analysis-v8-12.yml");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job, equalTo(PFEIFFER_SAMPLE_JOB));
        }

        @Test
        void readCliAnalysisWithJobSample() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--analysis", "src/test/resources/pfeiffer-job-sample.yml");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job, equalTo(PFEIFFER_SAMPLE_JOB));
        }

        @Test
        void readCliNewAnalysisOnlyThrowsException() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--analysis", "src/test/resources/exome-analysis.yml");
            Throwable throwable = assertThrows(ParameterException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), containsString("Missing --sample option!"));
        }

        @Test
        void readCliJobSample() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--job", "src/test/resources/pfeiffer-job-sample.yml");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job, equalTo(PFEIFFER_SAMPLE_JOB));
        }

        @Test
        void readCliJobPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand("--job", "src/test/resources/pfeiffer-job-phenopacket.yml");
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job, equalTo(PFEIFFER_PHENOPACKET_JOB));
        }

        @Test
        void readCliSampleAnalysisOutput() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            JobProto.Job job = analyseCommand.readJob();
            assertThat(job, equalTo(PFEIFFER_SAMPLE_JOB));
        }

        @Test
        void throwsExceptionWithFileSeparatorInOutputFileName() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output-filename", "src/test/resources/pfeiffer-output-options.yml"
            );
            Throwable throwable = assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), startsWith("output-filename option should not contain a filesystem separator:"));
        }

        @Test
        void readCliSampleOnlyWithSample() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setSample(SAMPLE)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleWithOutputPrefix() {
            String outputPrefixOption = "wibble";
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--output-prefix", outputPrefixOption
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setSample(SAMPLE)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    // output-prefix option is now deprecated and ignored
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleOnlyWithPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleOnlyWithPhenopacketAndVcf() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "GRCh37"
            );
            JobProto.Job job = analyseCommand.readJob();

            HtsFile userSpecifiedVcf = HtsFile.newBuilder()
                    .setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setUri(Path.of("src/test/resources/Pfeiffer.vcf").toUri().toString())
                    .setGenomeAssembly("GRCh37")
                    .build();

            Phenopacket updated = PHENOPACKET.toBuilder().setHtsFiles(0, userSpecifiedVcf).build();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(updated)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleAnalysisVcfOutput() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "GRCh37",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );

            SampleProto.Sample updated = SampleProto.Sample.newBuilder()
                    .setVcf(Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath().toString())
                    .setGenomeAssembly("GRCh37")
                    .build();

            JobProto.Job expected = PFEIFFER_SAMPLE_JOB.toBuilder().mergeSample(updated).build();

            assertThat(analyseCommand.readJob(), equalTo(expected));
        }

        @Test
        void readCliSampleLegacyAnalysisVcfOutput() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/pfeiffer-analysis-v8-12.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "GRCh38",
                    "--ped", "src/test/resources/pfeiffer-singleton.ped",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );

            SampleProto.Sample updated = SampleProto.Sample.newBuilder()
                    .setVcf(Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath().toString())
                    .setGenomeAssembly("GRCh38")
                    .setPed(Path.of("src/test/resources/pfeiffer-singleton.ped").toAbsolutePath().toString())
                    .build();

            JobProto.Job expected = PFEIFFER_SAMPLE_JOB.toBuilder().mergeSample(updated).build();

            assertThat(analyseCommand.readJob(), equalTo(expected));
        }

        @Test
        void readCliSamplePresetExome() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--preset=exome"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setSample(SAMPLE)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliFamilyExome() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-family.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setFamily(FAMILY)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliFamilyExomeWithVcfOption() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-family.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "GRCh37"
            );
            JobProto.Job job = analyseCommand.readJob();

            Path vcfPath = Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath();

            HtsFile htsFile = HtsFile.newBuilder()
                    .setUri(vcfPath.toUri().toString()).setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setGenomeAssembly("GRCh37")
                    .build();
            Family family = FAMILY.toBuilder().clearHtsFiles().addHtsFiles(0, htsFile).build();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setFamily(family)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliFamilyExomeWithVcfAndPedigreeOption() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-family.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "GRCh37",
                    "--ped", "src/test/resources/pfeiffer-singleton.ped"
            );
            JobProto.Job job = analyseCommand.readJob();

            Path vcfPath = Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath();

            HtsFile htsFile = HtsFile.newBuilder()
                    .setUri(vcfPath.toUri().toString()).setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setGenomeAssembly("GRCh37")
                    .build();
            Family family = FAMILY.toBuilder()
                    .clearHtsFiles().addHtsFiles(0, htsFile)
                    .setPedigree(PhenopacketPedigreeReader.readPedFile(Path.of("src/test/resources/pfeiffer-singleton.ped")))
                    .build();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setFamily(family)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliPhenopacketWithVcfAndPedigreeOption() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "hg19",
                    "--ped", "src/test/resources/pfeiffer-singleton.ped"
            );
            JobProto.Job job = analyseCommand.readJob();

            Path vcfPath = Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath();

            HtsFile htsFile = HtsFile.newBuilder()
                    .setUri(vcfPath.toUri().toString()).setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setGenomeAssembly("GRCh37")
                    .build();

            Phenopacket phenopacket = ProtoParser.parseFromJsonOrYaml(Phenopacket.newBuilder(), Path.of("src/test/resources/pfeiffer-phenopacket.yml"))
                    .clearHtsFiles()
                    .build();

            Family family = Family.newBuilder()
                    .setId(phenopacket.getId())
                    .setProband(phenopacket)
                    .addHtsFiles(htsFile)
                    .setPedigree(PhenopacketPedigreeReader.readPedFile(Path.of("src/test/resources/pfeiffer-singleton.ped")))
                    .build();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setFamily(family)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }


        @Test
        void readCliSamplePresetGenome() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-sample.yml",
                    "--preset=genome"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setSample(SAMPLE)
                    .setPreset(AnalysisProto.Preset.GENOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSamplePresetWithPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--preset=exome"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSamplePresetOutputWithPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--preset=exome",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(OUTPUT)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleOutputWithPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(OUTPUT)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleAnalysisWithPhenopacket() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setAnalysis(ANALYSIS)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleAnalysisWithOutputPrefixOption() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output-prefix", "some/custom/output-directory/pfeiffer"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setAnalysis(ANALYSIS)
                    // output-prefix is no longer supported
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS)
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleAnalysisWithOutputDirectoryAndFileNameOptions() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output-directory", "some/custom/output-directory",
                    "--output-filename", "pfeiffer"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setAnalysis(ANALYSIS)
                    .setOutputOptions(DEFAULT_OUTPUT_OPTIONS.toBuilder()
                            .clearOutputPrefix()
                            .setOutputDirectory("some/custom/output-directory")
                            .setOutputFileName("pfeiffer"))
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleOutputWithPhenopacketOutputDirectoryOverridesYamlOptions() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--output-directory", "some/custom/output-directory",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(OUTPUT.toBuilder()
                            .clearOutputPrefix()
                            .setOutputDirectory("some/custom/output-directory"))
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void readCliSampleOutputWithPhenopacketOutputFileNameOverridesYamlOptions() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--output-filename", "custom-filename",
                    "--output", "src/test/resources/pfeiffer-output-options.yml"
            );
            JobProto.Job job = analyseCommand.readJob();

            JobProto.Job expected = JobProto.Job.newBuilder()
                    .setPhenopacket(PHENOPACKET)
                    .setPreset(AnalysisProto.Preset.EXOME)
                    .setOutputOptions(OUTPUT.toBuilder()
                            .clearOutputPrefix()
                            .setOutputFileName("custom-filename"))
                    .build();

            assertThat(job, equalTo(expected));
        }

        @Test
        void throwExcetionWithNonOutputSettingsFile() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--analysis", "src/test/resources/exome-analysis.yml",
                    "--output", "src/test/resources/application.properties"
            );
            Throwable throwable = assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
            assertThat(throwable.getMessage(), equalTo("Unable to parse outputOptions from file src/test/resources/application.properties please check the format"));
        }

        @Test
        void readCliLegacyAnalysisWithVcfOverrideRequiresAssembly() {
            assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(
                    "--analysis", "src/test/resources/pfeiffer-analysis-v8-12.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf"
            ), "Error: Missing required argument(s): --assembly=<assembly>");
        }

        @Test
        void readCliLegacyAnalysisWithVcfOverrideSetsNewAssembly() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--analysis", "src/test/resources/pfeiffer-analysis-v8-12.yml",
                    "--vcf", "src/test/resources/Pfeiffer.vcf",
                    "--assembly", "hg38"
            );
            JobProto.Job job = analyseCommand.readJob();

            SampleProto.Sample updated = SampleProto.Sample.newBuilder()
                    .setVcf(Path.of("src/test/resources/Pfeiffer.vcf").toAbsolutePath().toString())
                    .setGenomeAssembly("GRCh38")
                    .build();

            JobProto.Job expected = PFEIFFER_SAMPLE_JOB.toBuilder().mergeSample(updated).build();

            assertThat(job, equalTo(expected));
        }

        @Disabled("Potentially confusing, but still legal input")
        @Test
        void readCliSampleWithLegacyAnalysisThrowsException() {
            AnalyseCommand analyseCommand = parseAnalyseCommand(
                    "--sample", "src/test/resources/pfeiffer-phenopacket.yml",
                    "--analysis", "src/test/resources/pfeiffer-analysis-v8-12.yml"
            );
            // the old analysis is really now a job, so it could be read as a Job, but then the sample would be over-writing
            // the sample details supplied in the old analysis job which can make for confusing behaviour. For this reason
            // I've chosen to make this strictly require the new analysis.yml format.
            assertThrows(IllegalArgumentException.class, analyseCommand::readJob);
        }
    }

}
