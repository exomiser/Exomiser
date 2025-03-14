package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.MutuallyExclusiveArgsException;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
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
    void parseIllegalJobCombination() {
        assertThrows(MutuallyExclusiveArgsException.class, () -> commandLine.parseArgs(
                "--job", resource("pfeiffer-job-sample.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalJobOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> commandLine.parseArgs(
                "--job", resource("pfeiffer-job-sample.yml"),
                "--output", "src/test/resources/pfeiffer-output-options.yml"
        ));
    }

    @Test
    void parseIllegalPresetOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> commandLine.parseArgs(
                "--preset", "exome",
                "--output", resource("pfeiffer-output-options.yml")
        ));
    }

    @Test
    void parseIllegalOutputCombination() {
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(
                "--output", resource("pfeiffer-output-options.yml")
        ));
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
        assertThrows(CommandLine.ParameterException.class, () -> commandLine.parseArgs(
                        "--sample", resource("pfeiffer-job-sample.yml"),
                        "--assembly", "wibble"),
                "'wibble' is not a valid/supported genome assembly."
        );
    }

    @Test
    void parseWillStopAtUnknownArgumentBefore() {
        // This happens due to the stopAtNonOptions = true argument in DefaultParser().parse(options, args, true)
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(
                "-wibble", "--analysis", "analysis/path"));
    }

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
                "--analysis", resource("exome-analysis.yml"), "--wibble");
        assertThat(parseResult.matchedOptions().size(), equalTo(1));
        assertTrue(parseResult.hasMatchedOption("analysis"));
        assertThat(parseResult.matchedOptionValue("analysis", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

//    @Test
//    void parseAnalysisBatch() {
//        org.apache.commons.cli.CommandLine commandLine = CommandLineOptionsParser.parse(
//                "--analysis-batch", resource("exome-analysis.yml"));
//        assertTrue(commandLine.hasOption("analysis-batch"));
//        assertThat(commandLine.getOptionValue("analysis-batch"), equalTo(resource("exome-analysis.yml")));
//    }

    @Test
    void parseSample() {
        var parseResult = commandLine.parseArgs(
                "--sample", resource("exome-analysis.yml"));
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(resource("exome-analysis.yml")));
    }

    @Test
    void parseSampleAndVcf() {
        String[] args = {"--sample","src/test/resources/pfeiffer-phenopacket.json", "--vcf", "src/test/resources/Pfeiffer.vcf", "--assembly", "hg19"};
        var parseResult = commandLine.parseArgs(args);
//        var parseResult = commandLine.parseArgs(
//                "--sample", resource("pfeiffer-phenopacket.json"),
//                "--vcf", resource("Pfeiffer.vcf"),
//                "--assembly", "hg19"
//        );
        assertTrue(parseResult.hasMatchedOption("sample"));
        assertThat(parseResult.matchedOptionValue("sample", Path.of("")), equalTo(Path.of(resource("pfeiffer-phenopacket.json"))));
        assertTrue(parseResult.hasMatchedOption("vcf"));
        assertThat(parseResult.matchedOptionValue("vcf", Path.of("")), equalTo(Path.of(resource("Pfeiffer.vcf"))));
        assertTrue(parseResult.hasMatchedOption("assembly"));
        assertThat(parseResult.matchedOptionValue("assembly", GenomeAssembly.HG38), equalTo(GenomeAssembly.HG19));
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


    @Test
    void parseOutput() {
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs("--output", "output/path"),
                "Missing an input file option!");
    }

    @Test
    void parsePreset() {
        assertThrows(MissingParameterException.class, () -> commandLine.parseArgs("--preset", "exome"),
                "Missing an input file option!");
    }

    @Test
    void parseJob() {
        var parseResult = commandLine.parseArgs("--job", resource("exome-analysis.yml"));
        assertTrue(parseResult.hasMatchedOption("job"));
        assertThat(parseResult.matchedOptionValue("job", Path.of("")), equalTo(Path.of(resource("exome-analysis.yml"))));
    }

}
