package org.monarchinitiative.exomiser.cli.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DiffReanalysisCommandTest {

    @Test
    void testParseCommandArgs() {
        String originalFileName = "original.variants.tsv";
        String latestFileName = "latest.variants.tsv";

        DiffReanalysisCommand instance = CommandLineParser.parseArgs(new DiffReanalysisCommand(),
                "--original", originalFileName,
                "--latest", latestFileName)
                .orElseThrow();

        assertThat(instance.originalPath, equalTo(Path.of(originalFileName)));
        assertThat(instance.latestPath, equalTo(Path.of(latestFileName)));
    }
}