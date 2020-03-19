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

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CommandLineOptionsParser {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptionsParser.class);

    private final static Options options = new Options();

    static {
        options.addOption(new Option("h", "help", false, "Shows this help"));

        options.addOption(Option.builder()
                .longOpt("analysis")
                .desc("Path to analysis script file. This should be in yaml format.")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder()
                .longOpt("analysis-batch")
                .desc("Path to analysis batch file. This should be in plain text file with the path to a single analysis script file in yaml format on each line.")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder()
                .longOpt("sample")
                .desc("Path to sample or phenopacket file. This should be in JSON or YAML format.")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder()
                .longOpt("job")
                .desc("Path to job file. This should be in JSON or YAML format.")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder()
                .longOpt("preset")
                .desc("The Exomiser analysis preset for the input sample. One of 'exome' or 'genome'")
                .hasArg()
                .argName("string")
                .build());

        options.addOption(Option.builder()
                .longOpt("output")
                .desc("Path to outputOptions file. This should be in JSON or YAML format.")
                .hasArg()
                .argName("string")
                .build());
    }

    private CommandLineOptionsParser() {
    }

    public static CommandLine parse(String... args) {
        try {
            // Beware! - the command line parser will fail if any spring-related options are provided before the exomiser ones
            // ensure all exomiser commands are provided before any spring boot command.
            return new DefaultParser().parse(options, args, true);
        } catch (ParseException ex) {
            String message = "Unable to parse command line arguments. Please check you have typed the parameters correctly." +
                    " Use command --help for a list of commands.";
            throw new CommandLineParseError(message, ex);
        }
    }

    public static List<String> fileDependentOptions() {
        return options.getOptions().stream()
                .filter(option -> "file".equals(option.getArgName()))
                .map(Option::getLongOpt)
                .collect(toList());
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar exomiser-cli-{build.version}.jar [...]", options);
    }

    public static void printHelpAndExit() {
        printHelp();
        System.exit(0);
    }
}
