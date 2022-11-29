package org.monarchinitiative.exomiser.cli.command;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineParserTest {

    private String resource(String fileName) {
        return "src/test/resources/" + fileName;
    }

    @Test
    void parseAnalysis() {
        List<JobProto.Job> jobs = CommandLineParser.parseArgs(new ExomiserCommands(), "analysis", "--analysis", resource("pfeiffer-analysis-v8-12.yml"))
                .stream()
                .flatMap(JobParserCommand.parseJobStream())
                .toList();
        assertThat(jobs.size(), equalTo(1));
    }

    @Test
    void parseAnalysisHelpOption() {
        assertThat(CommandLineParser.parseArgs(new ExomiserCommands(), "analysis", "-h"), equalTo(Optional.empty()));
    }

    @Test
    void parseAnalysisIncorrectCommand() {
        assertThrows(CommandLineParseError.class, ()-> CommandLineParser.parseArgs(new ExomiserCommands(), "analysis", "wibble"));
    }

    @Test
    void parseBatch() {
        List<JobProto.Job> jobs = CommandLineParser.parseArgs(new ExomiserCommands(), "batch",
                resource("test-analysis-batch-commands.txt"))
                .stream()
                .flatMap(JobParserCommand.parseJobStream())
                .toList();

        assertFalse(jobs.isEmpty());
    }

}