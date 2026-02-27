package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class BatchCommandTest {

    BatchCommand instance;
    CommandLine commandLine;

    @BeforeEach
    void setup() {
        instance = new BatchCommand();
        commandLine = new CommandLine(instance)
                .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @Test
    void hasHelp() {
        CommandLine.ParseResult parseResult = commandLine.parseArgs("-h");
        assertThat(parseResult.isUsageHelpRequested(), is(true));
    }

    @Test
    void batchFilePathParameter() {
        commandLine.parseArgs("batch.txt");
        assertThat(instance.batchFilePath, equalTo(Path.of("batch.txt")));
    }

    @Test
    void dryRunOption() {
        commandLine.parseArgs("batch.txt", "--dry-run");
        assertThat(instance.dryRun, is(true));
    }
}
