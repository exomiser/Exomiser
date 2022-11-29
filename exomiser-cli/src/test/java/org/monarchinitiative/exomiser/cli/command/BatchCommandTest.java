package org.monarchinitiative.exomiser.cli.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class BatchCommandTest {

    private final Path batchCommands = Path.of("src/test/resources/test-analysis-batch-commands.txt");

    @Test
    void parseBatchFilePath() {
        var instance = CommandLineParser.parseArgs(new BatchCommand(),
                batchCommands.toString())
                .orElseThrow();
        assertThat(instance.batchFile, equalTo(batchCommands));
    }

    @Test
    void parseBatchAnalysisJobs() {
        var instance = CommandLineParser.parseArgs(new BatchCommand(),
                batchCommands.toString())
                .orElseThrow();
        assertFalse(instance.parseJobs().isEmpty());
    }

}