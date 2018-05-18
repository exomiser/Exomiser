/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisParser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.writers.AnalysisResultsWriter;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class ExomiserCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserCommandLineRunner.class);

    @Autowired
    private Options options;

    @Autowired
    private AnalysisParser analysisParser;
    @Autowired
    private Exomiser exomiser;

    @Value("buildVersion")
    private String buildVersion;

    @Override
    public void run(String... strings) {
        if (strings.length == 0) {
            logger.error("Please supply some command line arguments - none found");
            printHelpAndExit();
        }
        CommandLine commandLine = parseCommandLineOptions(strings);
        if (commandLine.hasOption("help")) {
            printHelpAndExit();
        }
        logger.info("Exomiser running...");
        runAnalyses(commandLine);
    }

    private void runAnalyses(CommandLine commandLine) {
        if (commandLine.hasOption("analysis")) {
            Path analysisScript = Paths.get(commandLine.getOptionValue("analysis"));
            runAnalysisFromScript(analysisScript);
        } else if (commandLine.hasOption("analysis-batch")) {
            Path analysisBatchFile = Paths.get(commandLine.getOptionValue("analysis-batch"));
            List<Path> analysisScripts = BatchFileReader.readPathsFromBatchFile(analysisBatchFile);
            logger.info("Running {} analyses from analysis batch file.", analysisScripts.size());
            //this *could* be run in parallel using parallelStream() at the expense of RAM in order to hold all the variants in memory.
            //HOWEVER there may be threading issues so this needs investigation.
            analysisScripts.forEach(analysis ->{
                logger.info("Running analysis: {}", analysis);
                runAnalysisFromScript(analysis);
            });
        }
    }

    private CommandLine parseCommandLineOptions(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            // Beware! - the command line parser will fail if any spring-related options are provided before the exomiser ones
            // ensure all exomiser commands are provided before any spring boot command.
            return parser.parse(options, args, true);
        } catch (ParseException ex) {
            String message = "Unable to parse command line arguments. Please check you have typed the parameters correctly." +
                    " Use command --help for a list of commands.";
            throw new CommandLineParseError(message, ex);
        }
    }

    private void printHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        String launchCommand = String.format("java -jar exomiser-cli-%s.jar [...]", buildVersion);
        formatter.printHelp(launchCommand, options);
        System.exit(0);
    }

    private void runAnalysisFromScript(Path analysisScript) {
        Analysis analysis = analysisParser.parseAnalysis(analysisScript);
        OutputSettings outputSettings = analysisParser.parseOutputSettings(analysisScript);
        runAnalysisAndWriteResults(analysis, outputSettings);
    }

    private void runAnalysisAndWriteResults(Analysis analysis, OutputSettings outputSettings) {
        AnalysisResults analysisResults = exomiser.run(analysis);
        AnalysisResultsWriter.writeToFile(analysis, analysisResults, outputSettings);
    }

}
