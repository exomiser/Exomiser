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
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class CommandLineOptionsParserTest {


    private String resource(String fileName) {
        return "src/test/resources/" + fileName;
    }

    @Test
    void parseIllegalArgument() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse("--analysis"));
    }

    @Test
    void parseIllegalAnalysisPresetCombination() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse(
                "--analysis", resource("pfeiffer_analysis_v8_12.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalJobCombination() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse(
                "--job", resource("pfeiffer_job_sample.yml"),
                "--preset", "exome"));
    }

    @Test
    void parseIllegalJobOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse(
                "--job", resource("pfeiffer_job_sample.yml"),
                "--output", "src/test/resources/pfeiffer_output_options.yml"
        ));
    }

    @Test
    void parseIllegalPresetOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse(
                "--preset", "exome",
                "--output", resource("pfeiffer_output_options.yml")
        ));
    }

    @Test
    void parseIllegalOutputCombination() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse(
                "--output", resource("pfeiffer_output_options.yml")
        ));
    }

    @Test
    void parseWillStopAtUnknownArgumentBefore() {
        // This happens due to the stopAtNonOptions = true argument in DefaultParser().parse(options, args, true)
        CommandLine commandLine = CommandLineOptionsParser.parse(
                "-wibble", "--output", "output/path");
        assertThat(commandLine.getOptions().length, equalTo(0));
    }

    @Test
    void parseWillStopAtUnknownArgumentAfter() {
        CommandLine commandLine = CommandLineOptionsParser.parse(
                "--analysis", resource("exome-analysis.yml"), "-wibble");
        assertEquals(commandLine.getOptions().length, 1);
        assertTrue(commandLine.hasOption("analysis"));
        assertThat(commandLine.getOptionValue("analysis"), equalTo(resource("exome-analysis.yml")));
    }

    @Test
    void parseAnalysis() {
        CommandLine commandLine = CommandLineOptionsParser.parse(
                "--analysis", resource("exome-analysis.yml"), "--wibble");
        assertTrue(commandLine.hasOption("analysis"));
        assertThat(commandLine.getOptionValue("analysis"), equalTo(resource("exome-analysis.yml")));
    }

    @Test
    void parseAnalysisBatch() {
        CommandLine commandLine = CommandLineOptionsParser.parse(
                "--analysis-batch", resource("exome-analysis.yml"));
        assertTrue(commandLine.hasOption("analysis-batch"));
        assertThat(commandLine.getOptionValue("analysis-batch"), equalTo(resource("exome-analysis.yml")));
    }

    @Test
    void parseSample() {
        CommandLine commandLine = CommandLineOptionsParser.parse(
                "--sample", resource("exome-analysis.yml"));
        assertTrue(commandLine.hasOption("sample"));
        assertThat(commandLine.getOptionValue("sample"), equalTo(resource("exome-analysis.yml")));
    }

    @Test
    void parseOutput() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse("--output", "output/path"),
                "Missing an input file option!");
    }

    @Test
    void parsePreset() {
        assertThrows(CommandLineParseError.class, () -> CommandLineOptionsParser.parse("--preset", "exome"),
                "Missing an input file option!");
    }

    @Test
    void parseJob() {
        CommandLine commandLine = CommandLineOptionsParser.parse("--job", resource("exome-analysis.yml"));
        assertTrue(commandLine.hasOption("job"));
        assertThat(commandLine.getOptionValue("job"), equalTo(resource("exome-analysis.yml")));
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