package org.monarchinitiative.exomiser.cli.command;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class AnalysisCommandTest {

    private static AnalysisCommand parseArgs(String... args) {
        return CommandLineParser.parseArgs(new AnalysisCommand(), args).orElseThrow();
    }

    private String resource(String fileName) {
        return "src/test/resources/" + fileName;
    }

    private Path resourcePath(String fileName) {
        return Path.of(resource(fileName));
    }

    @Test
    void parseIllegalArgument() {
        assertThrows(CommandLineParseError.class, () -> parseArgs("--analysis"));
    }

    @Test
    void parseIllegalAnalysisPresetCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--analysis", resource("pfeiffer-analysis-v8-12.yml"),
                "--preset", "exome").validate());
    }

    @Test
    void parseIllegalJobCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--job", resource("pfeiffer-job-sample.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalJobOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--job", resource("pfeiffer-job-sample.yml"),
                "--output", "src/test/resources/pfeiffer-output-options.yml"
        ));
    }

    @Test
    void parseIllegalPresetOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--preset", "exome",
                "--output", resource("pfeiffer-output-options.yml")
        ));
    }

    @Test
    void parseIllegalOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--output", resource("pfeiffer-output-options.yml")
        ));
    }

    @Test
    void parseIllegalSampleAssemblyCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                "--sample", resource("pfeiffer-job-sample.yml"),
                "--assembly", "GRCh37"
        ));
    }

    @Test
    void parseIllegalMissingAssemblyAnalysisVcfCombination() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                        "--analysis", resource("pfeiffer-analysis-v8-12.yml"),
                        "--vcf", resource("Pfeiffer.vcf")),
                "--assembly option required when specifying vcf!");
    }

    @Test
    void parseIllegalAssemblyValue() {
        assertThrows(CommandLineParseError.class, () -> parseArgs(
                        "--sample", resource("pfeiffer-job-sample.yml"),
                        "--assembly", "wibble"),
                "'wibble' is not a valid/supported genome assembly."
        );
    }

    @Test
    void parseWillStopIngnoreUnknownArgumentBefore() {
        // This happens due to the stopAtNonOptions = true argument in DefaultParser().parse(options, args, true)
        var instance = parseArgs(
                "-wibble",
                "--analysis", resource("exome-analysis.yml"),
                "--output", "output/path");
        assertThat(instance.analysisOptions.analysisPath, equalTo(resourcePath("exome-analysis.yml")));
    }

    @Test
    void parseWillIgnoreUnknownArgumentAfter() {
        var instance = parseArgs(
                "--analysis", resource("exome-analysis.yml"),
                "-wibble");
        assertThat(instance.analysisOptions.analysisPath, equalTo(resourcePath("exome-analysis.yml")));
    }

    @Test
    void parseAnalysis() {
        var instance = parseArgs(
                "--analysis", resource("exome-analysis.yml"), "--wibble");
        assertThat(instance.analysisOptions.analysisPath, equalTo(resourcePath("exome-analysis.yml")));
    }

    @Test
    void parseSample() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"));
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
    }

    @Test
    void parseSampleAndVcf() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--vcf", resource("Pfeiffer.vcf"),
                "--assembly", "hg19");
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
        AnalysisCommand.VcfOptions vcfOptions = instance.sampleOptions.vcfOptions;
        assertThat(vcfOptions.vcfPath, equalTo(resourcePath("Pfeiffer.vcf")));
        assertThat(vcfOptions.assembly, equalTo(GenomeAssembly.HG19));
    }

    @Test
    void parseIllegalOutputFileName() {
        AnalysisCommand analysisCommand = new AnalysisCommand();
        String samplePath = resource("exome-analysis.yml");
        String vcfPath = resource("Pfeiffer.vcf");
        assertThrows(CommandLineParseError.class, () -> CommandLineParser.parseArgs(analysisCommand,
                        "--sample", samplePath,
                        "--vcf", vcfPath,
                        "--assembly", "hg19",
                        "--output-file-name", "results/pfeiffer-exome-analysis-results"
                )
        , "output-file-name option should not contain a filesystem separator: results/pfeiffer-exome-analysis-results");
    }

    @Test
    void parseOutputOptionsPath() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--output-options", resource("output-options.yml")
        );
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
        assertThat(instance.outputOptions.outputOptionsPath, equalTo(resourcePath("output-options.yml")));
    }

    @Test
    void parseOutputDirectory() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--output-directory", "some/custom/out-directory"
        );
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
        assertThat(instance.outputOptions.outputDirectory, equalTo(Path.of("some/custom/out-directory")));
    }

    @Test
    void parseOutputFileName() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--output-file-name", "custom-filename"
        );
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
        assertThat(instance.outputOptions.outputFileName, equalTo("custom-filename"));
    }

    @Test
    void parseOutputFormat() {
        var instance = parseArgs(
                "--sample", resource("exome-analysis.yml"),
                "--output-format", "TSV_VARIANT,TSV_GENE"
        );
        assertThat(instance.sampleOptions.samplePath, equalTo(resourcePath("exome-analysis.yml")));
        assertThat(instance.outputOptions.outputFormats, equalTo(List.of(OutputProto.OutputFormat.TSV_VARIANT, OutputProto.OutputFormat.TSV_GENE)));
    }

    @Test
    void parseOutput() {
        assertThrows(CommandLineParseError.class, () -> parseArgs("--output", "output/path"),
                "Missing an input file option!");
    }

    @Test
    void parsePreset() {
        assertThrows(CommandLineParseError.class, () -> parseArgs("--preset", "exome"),
                "Missing an input file option!");
    }

    // TODO: should be a JobCommand
//    @Test
//    void parseJob() {
//        CommandLine commandLine = CommandLineOptionsParser.parse("--job", resource("exome-analysis.yml"));
//        assertTrue(commandLine.hasOption("job"));
//        assertThat(commandLine.getOptionValue("job"), equalTo(resource("exome-analysis.yml")));
//    }

}