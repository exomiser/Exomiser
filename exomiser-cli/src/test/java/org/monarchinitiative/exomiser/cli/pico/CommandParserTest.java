package org.monarchinitiative.exomiser.cli.pico;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.cli.ExomiserCli;
import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.commands.AnnotateCommand;
import org.monarchinitiative.exomiser.cli.commands.BatchCommand;
import org.monarchinitiative.exomiser.cli.commands.ExomiserCommand;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CommandParserTest {

    CommandParser<ExomiserCommand> commandParser;

    @BeforeEach
    void setup() {
        commandParser = new CommandParser<>(ExomiserCli.newExomiserCommandLine());
    }

    @Test
    void noCommand() {
        var result = commandParser.parseArgs();
        assertThat(result.isHelp(), is(true));
        assertThat(result.command(), nullValue());
    }

    @Test
    void helpCommand() {
        var result = commandParser.parseArgs("--help");
        assertThat(result.isHelp(), is(true));
    }

    @Test
    void versionCommand() {
        var result = commandParser.parseArgs("--version");
        assertThat(result.isVersion(), is(true));
    }

    @Test
    void emptyCommand() {
        var result = commandParser.parseArgs("");
        assertThat(result.isError(), is(true));
        assertThat(result.command(), nullValue());
    }

    @Test
    void unknownCommand() {
        var result = commandParser.parseArgs("unknown", "--option", "12345");
        assertThat(result.isError(), is(true));
    }

    @Nested
    class AnalysisCommandTests {

        @Test
        void failsOnUnknownCommand() {
            var parserResult = commandParser.parseArgs("analyse", "--sample", "sample.phenopacket.json", "--vcf", "variants.vcf", "--assembly", "hg38", "--preset", "exome", "--logging.level=trace");
            assertThat(parserResult.isError(), is(true));
        }

        @Test
        void happyPath() {
            var parserResult = commandParser.parseArgs("analyse", "--sample", "sample.phenopacket.json", "--vcf", "variants.vcf", "--assembly", "hg38", "--preset", "exome");
            assertThat(parserResult.isCommand(), is(true));
            assertThat(parserResult.command(), instanceOf(AnalyseCommand.class));
        }

        @Test
        void analysisNoAssembly() {
            var parserResult = commandParser.parseArgs("analyse", "--sample", "sample.phenopacket.json", "--vcf", "variants.vcf", "--preset", "exome");
            assertThat(parserResult.isError(), is(true));
        }
    }

    @Nested
    class AnnotateCommandTests {

        @Test
        void help() {
            var result = commandParser.parseArgs("annotate", "--help");
            assertThat(result.isHelp(), is(true));
        }

        @Test
        void vcfAndVariantAreExclusiveOptions() {
            var parserResult = commandParser.parseArgs("annotate", "--vcf", "variants.vcf", "-v", "1-12345-A-C", "--assembly", "hg19");
            assertThat(parserResult.isError(), is(true));
        }

        @Test
        void assemblyIsRequired() {
            var parserResult = commandParser.parseArgs("annotate", "--vcf", "variants.vcf");
            assertThat(parserResult.isError(), is(true));
        }

        @Test
        void validVariantCommand() {
            var parserResult = commandParser.parseArgs("annotate", "-v", "1-12345-A-C", "--assembly", "hg19");
            assertThat(parserResult.isCommand(), is(true));
            assertThat(parserResult.command(), instanceOf(AnnotateCommand.class));
            AnnotateCommand annotateCommand = (AnnotateCommand) parserResult.command();
            assertThat(annotateCommand.inputOption.variant, equalTo(new AnnotateCommand.VariantCoordinates("1", 12345, 12345, "A", "C")));
            assertThat(annotateCommand.genomeAssembly, equalTo(GenomeAssembly.HG19));
        }
    }

    @Nested
    class BatchCommandTests {

        @Test
        void help() {
            var result = commandParser.parseArgs("batch", "--help");
            assertThat(result.isHelp(), is(true));
        }

        @Test
        void missingInputFileParameter() {
            var result = commandParser.parseArgs("batch", "--dry-run");
            assertThat(result.isError(), is(true));
        }

        @Test
        void noOption() {
            var result = commandParser.parseArgs("batch", "batch-file.txt");
            assertThat(result.isCommand(), is(true));
            assertThat(result.command(), instanceOf(BatchCommand.class));
        }

        @Test
        void dryRunOption() {
            var result = commandParser.parseArgs("batch", "--dry-run", "batch-file.txt");
            assertThat(result.isCommand(), is(true));
            assertThat(result.command(), instanceOf(BatchCommand.class));
        }
    }
}