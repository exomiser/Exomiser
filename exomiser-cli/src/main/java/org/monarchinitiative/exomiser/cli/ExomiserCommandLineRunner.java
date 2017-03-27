/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.cli;

import org.apache.commons.cli.*;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.*;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;
import org.monarchinitiative.exomiser.core.writers.ResultsWriter;
import org.monarchinitiative.exomiser.core.writers.ResultsWriterFactory;
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
    private CommandLineOptionsParser commandLineOptionsParser;
    @Autowired
    private Options options;

    @Autowired
    private AnalysisParser analysisParser;
    @Autowired
    private SettingsParser settingsParser;
    @Autowired
    private Exomiser exomiser;
    @Autowired
    private ResultsWriterFactory resultsWriterFactory;

    @Value("buildVersion")
    private String buildVersion;

    @Override
    public void run(String... strings) {
        if (strings.length == 0) {
            printHelp();
            logger.error("Please supply some command line arguments - none found");
        }
        CommandLine commandLine = parseCommandLineOptions(strings);
        if (commandLine.hasOption("help")) {
            printHelp();
        }
        runAnalyses(commandLine);
    }

    private void runAnalyses(CommandLine commandLine) {
        if (commandLine.hasOption("analysis")) {
            Path analysisScript = Paths.get(commandLine.getOptionValue("analysis"));
            runAnalysisFromScript(analysisScript);
        } else if (commandLine.hasOption("analysis-batch")) {
            Path analysisBatchFile = Paths.get(commandLine.getOptionValue("analysis-batch"));
            List<Path> analysisScripts = new BatchFileReader().readPathsFromBatchFile(analysisBatchFile);
            logger.info("Running {} analyses from analysis batch file.", analysisScripts.size());
            //this *can* be run in parallel using parallelStream() at the expense of RAM in order to hold all the variants in memory.
            //like this:
            //analysisScripts.parallelStream().forEach(this::runAnalysisFromScript);
            //HOWEVER there may be threading issues so this needs investigation.
            analysisScripts.forEach(analysis ->{
                logger.info("Running analysis: {}", analysis);
                runAnalysisFromScript(analysis);
            });
        }
        //check the args for a batch file first as this option is otherwise ignored
        else if (commandLine.hasOption("batch-file")) {
            Path batchFilePath = Paths.get(commandLine.getOptionValue("batch-file"));
            List<Path> settingsFiles = new BatchFileReader().readPathsFromBatchFile(batchFilePath);
            logger.info("Running {} analyses from settings batch file.", settingsFiles.size());
            for (Path settingsFile : settingsFiles) {
                logger.info("Running settings: {}", settingsFile);
                Settings settings = commandLineOptionsParser.parseSettingsFile(settingsFile);
                runAnalysisFromSettings(settings);
            }
        } else {
            //make a single SettingsBuilder
            Settings settings = commandLineOptionsParser.parseCommandLine(commandLine);
            runAnalysisFromSettings(settings);
        }
    }

    private CommandLine parseCommandLineOptions(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException ex) {
            String message = "Unable to parse command line arguments. Please check you have typed the parameters correctly." +
                    " Use command --help for a list of commands.";
            throw new CommandLineParseError(message, ex);
        }
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String launchCommand = String.format("java -jar exomizer-cli-%s.jar [...]", buildVersion);
        formatter.printHelp(launchCommand, options);
    }

    private void runAnalysisFromScript(Path analysisScript) {
        Analysis analysis = analysisParser.parseAnalysis(analysisScript);
        OutputSettings outputSettings = analysisParser.parseOutputSettings(analysisScript);
        runAnalysisAndWriteResults(analysis, outputSettings);
    }

    private void runAnalysisFromSettings(Settings settings) {
        if (settings.isValid()) {
            Analysis analysis = settingsParser.parse(settings);
            runAnalysisAndWriteResults(analysis, settings);
        }
    }

    private void runAnalysisAndWriteResults(Analysis analysis, OutputSettings outputSettings) {
        AnalysisResults analysisResults = exomiser.run(analysis);
        writeResults(analysis, analysisResults, outputSettings);
    }

    private void writeResults(Analysis analysis, AnalysisResults analysisResults, OutputSettings outputSettings) {
        logger.info("Writing results");
        for (OutputFormat outFormat : outputSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = resultsWriterFactory.getResultsWriter(outFormat);
            resultsWriter.writeFile(analysis, analysisResults, outputSettings);
        }
    }

}
