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

package org.monarchinitiative.exomiser.cli;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class CommandLineOptionsParserTest {

    @Test
    void parseIllegalArgument() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse("--analysis"));
    }

    @Test
    void parseWillIgnoreUnknownArgument() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--wibble");
        assertThat(commandLine.getOptions().length, equalTo(0));
    }

    @Test
    void parseAnalysis() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--analysis", "analysis/path");
        assertTrue(commandLine.hasOption("analysis"));
        assertThat(commandLine.getOptionValue("analysis"), equalTo("analysis/path"));
    }

    @Test
    void parseAnalysisBatch() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--analysis-batch", "analysis-batch/path");
        assertTrue(commandLine.hasOption("analysis-batch"));
        assertThat(commandLine.getOptionValue("analysis-batch"), equalTo("analysis-batch/path"));
    }

    @Test
    void parseSample() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--sample", "sample/path");
        assertTrue(commandLine.hasOption("sample"));
        assertThat(commandLine.getOptionValue("sample"), equalTo("sample/path"));
    }

    @Test
    void parseOutput() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--output", "output/path");
        assertTrue(commandLine.hasOption("output"));
        assertThat(commandLine.getOptionValue("output"), equalTo("output/path"));
    }

    @Test
    void parsePreset() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--preset", "exome");
        assertTrue(commandLine.hasOption("preset"));
        assertThat(commandLine.getOptionValue("preset"), equalTo("exome"));
    }

    @Test
    void parseJob() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--job", "job/path");
        assertTrue(commandLine.hasOption("job"));
        assertThat(commandLine.getOptionValue("job"), equalTo("job/path"));
    }

    @Test
    void printHelp() {
        CommandLineOptionsParser.printHelp();
    }

    @Test
    void fileDependentOptions() {
        assertThat(CommandLineOptionsParser.fileDependentOptions(), equalTo(List.of("analysis", "analysis-batch", "sample", "job")));
    }
}