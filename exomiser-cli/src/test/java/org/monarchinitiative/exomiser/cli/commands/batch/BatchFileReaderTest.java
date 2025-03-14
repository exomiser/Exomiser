/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.cli.commands.batch;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.monarchinitiative.exomiser.cli.pico.CommandParserResult;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class BatchFileReaderTest {

    @Test
    void testAnalyseCommand() {
        var commandLine = new CommandLine(new AnalyseCommand()).setCaseInsensitiveEnumValuesAllowed(true);
        var commandParser = new CommandParser<AnalyseCommand>(commandLine);
        String line = "--sample examples/pfeiffer-phenopacket.yml --vcf examples/Pfeiffer.vcf.gz --assembly hg19 --preset exome";
        CommandParserResult<AnalyseCommand> commandParserResult = commandParser.parseArgs(line.split("\\s+"));
        assertThat(commandParserResult.isCommand(), is(true));
    }

    @Test
    void testReadJobsFromBatchFile() {
        assertThat(BatchFileReader.readJobsFromBatchFile(Path.of("src/test/resources/test-analysis-batch-commands.txt")).size(), equalTo(5));
    }
}