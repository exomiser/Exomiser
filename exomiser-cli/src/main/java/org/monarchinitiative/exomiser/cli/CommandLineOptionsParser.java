/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class CommandLineOptionsParser {

    private static final Options options = new Options();

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
                .longOpt("batch")
                .desc("Path to cli batch file. This should be in plain text file with the cli input for an analysis on each line.")
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
                .longOpt("vcf")
                .desc("Path to sample VCF file. Also requires 'assembly' option to be defined.")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder()
                .longOpt("assembly")
                .desc("Genome assembly of sample VCF file. Either 'GRCh37' or 'GRCh38'")
                .hasArg()
                .argName("assembly")
                .build());

        options.addOption(Option.builder()
                .longOpt("ped")
                .desc("Path to sample PED file.")
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
                .desc("The Exomiser analysis preset for the input sample. One of 'exome', 'genome' or 'phenotype-only'")
                .hasArg()
                .argName("string")
                .build());

        options.addOption(Option.builder()
                .longOpt("output")
                .desc("Path to outputOptions file. This should be in JSON or YAML format.")
                .hasArg()
                .argName("string")
                .build());

        options.addOption(Option.builder()
                .longOpt("output-prefix")
                .desc("Path/filename without an extension to be prepended to the output file format options." +
                        " This option is EXCLUSIVE to the output-directory and output-filename options. DEPRECATED! Use --output-directory and/or --output-filename instead.")
                .hasArg()
                .argName("string")
                .build());

        options.addOption(Option.builder()
                .longOpt("output-directory")
                .desc("Directory where the output files should be written. This option is EXCLUSIVE to the output-prefix option.")
                .hasArg()
                .argName("path")
                .build());

        options.addOption(Option.builder()
                .longOpt("output-filename")
                .desc("Filename prefix for the output files. Will be generated from the input VCF filename if not specified. This option is EXCLUSIVE to the output-prefix option.")
                .hasArg()
                .argName("string")
                .build());

        options.addOption(Option.builder()
                .longOpt("output-format")
                .desc("A list of comma separated output format(s) e.g. HTML or HTML,JSON. Valid options include [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]. Note that HTML is the most human-friendly, JSON is the most detailed. (default = HTML,JSON)")
                .hasArgs()
                .valueSeparator(',')
                .argName("string")
                .build());
    }

    private CommandLineOptionsParser() {
    }

    public static CommandLine parse(String... args) {
        try {
            // Beware! - the command line parser will fail if any spring-related options are provided before the exomiser ones
            // ensure all exomiser commands are provided before any spring boot command.
            return validate(new DefaultParser().parse(options, args, true));
        } catch (ParseException ex) {
            String message = "Unable to parse command line arguments. Please check you have typed the parameters correctly." +
                    " Use command --help for a list of commands.";
            throw new CommandLineParseError(message, ex);
        }
    }

    private static CommandLine validate(CommandLine commandLine) {
        if (commandLine.hasOption("help") || commandLine.getOptions().length == 0) {
            // return the commandLine here as this will allow Main to print the help message
            return commandLine;
        }
        if (commandLine.hasOption("job") && commandLine.getOptions().length > 1) {
            throw new CommandLineParseError("job option is exclusive");
        }

        if (commandLine.hasOption("analysis") && commandLine.hasOption("preset")) {
            throw new CommandLineParseError("preset and analysis options are mutually exclusive");
        }

        if (commandLine.hasOption("sample") && commandLine.hasOption("assembly") && !commandLine.hasOption("vcf")) {
            throw new CommandLineParseError("assembly present without vcf option");
        }

        if (commandLine.hasOption("vcf") && !commandLine.hasOption("assembly")) {
            throw new CommandLineParseError("--assembly option required when specifying vcf!");
        }
        if (commandLine.hasOption("assembly")) {
            try {
                GenomeAssembly.parseAssembly(commandLine.getOptionValue("assembly"));
            } catch (Exception e) {
                throw new CommandLineParseError(e.getMessage());
            }
        }
        if (commandLine.hasOption("output-prefix") && (commandLine.hasOption("output-directory") || commandLine.hasOption("output-filename"))) {
            throw new CommandLineParseError("output-prefix option is exclusive to output-directory and output-filename options");
        }
        if (commandLine.hasOption("output-filename")) {
            String value = commandLine.getOptionValue("output-filename");
            if (value.contains(System.getProperty("file.separator"))) {
                throw new IllegalArgumentException("output-filename option should not contain a filesystem separator: " + value);
            }
        }
        if (!hasInputFileOption(commandLine)) {
            throw new CommandLineParseError("Missing an input file option!");
        }
        //check file paths exist before launching.
        checkFilesExist(commandLine);
        return commandLine;
    }

    private static boolean hasInputFileOption(CommandLine commandLine) {
        for (String option : fileDependentOptions()) {
            if (commandLine.hasOption(option)) {
                return true;
            }
        }
        return false;
    }

    private static void checkFilesExist(CommandLine commandLine) {
        for (String option : fileDependentOptions()) {
            if (commandLine.hasOption(option)) {
                Path optionPath = Paths.get(commandLine.getOptionValue(option));
                if (Files.notExists(optionPath)) {
                    throw new CommandLineParseError(String.format("%s file '%s' not found", option, optionPath));
                }
            }
        }
    }

    // protected for ease of testing
    protected static List<String> fileDependentOptions() {
        return options.getOptions().stream()
                .filter(option -> "file".equals(option.getArgName()))
                .map(Option::getLongOpt)
                .toList();
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar exomiser-cli-{build.version}.jar [...]", options);
    }
}
