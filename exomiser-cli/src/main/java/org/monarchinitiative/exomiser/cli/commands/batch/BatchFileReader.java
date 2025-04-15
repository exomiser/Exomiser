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

import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;
import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.monarchinitiative.exomiser.cli.pico.CommandParserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Reads in Exomiser batch files and returns a list of Paths to the
 * settings/analysis files. The reader expects a single path per line.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileReader {

    private static final Logger logger = LoggerFactory.getLogger(BatchFileReader.class);

    private BatchFileReader() {
        //non-instantiable static utility class
    }

    public static List<JobProto.Job> readJobsFromBatchFile(Path batchFile) {
        logger.info("Processing batch file {}", batchFile);
        try (Stream<String> lines = Files.lines(batchFile, StandardCharsets.UTF_8)) {
            return lines
                    .filter(commentLines())
                    .filter(emptyLines())
                    .map(line -> {
                        logger.info("Parsing command '{}'", line);
                        var commandLine = new CommandLine(new AnalyseCommand()).setCaseInsensitiveEnumValuesAllowed(true);
                        var commandParser = new CommandParser<AnalyseCommand>(commandLine);
                        return commandParser.parseArgs(line.split("\\s+"));
                    })
                    .map(CommandParserResult::command)
                    .filter(Objects::nonNull)
                    .map(AnalyseCommand::readJob)
                    .toList();
        } catch (Exception ex) {
            throw new CommandLineParseError("Error reading batch file " + batchFile, ex);
        }
    }

    private static Predicate<String> commentLines() {
        return line -> !line.startsWith("#");
    }

    private static Predicate<String> emptyLines() {
        return line -> !line.isEmpty();
    }

}
