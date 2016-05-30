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

package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.analysis.AnalysisParser;
import de.charite.compbio.exomiser.core.analysis.Settings;
import de.charite.compbio.exomiser.core.analysis.SettingsParser;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.writers.OutputSettings;
import de.charite.compbio.exomiser.core.writers.ResultsWriter;
import de.charite.compbio.exomiser.core.writers.ResultsWriterFactory;
import org.apache.commons.cli.*;
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
        runAnalyses(strings);
    }

    private void runAnalyses(String[] args) {
        CommandLine commandLine = parseCommandLineOptions(args);
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
            analysisScripts.forEach(analysis ->{logger.info("Running analysis: {}", analysis); runAnalysisFromScript(analysis);});
        }
        //check the args for a batch file first as this option is otherwise ignored
        else if (commandLine.hasOption("batch-file")) {
            Path batchFilePath = Paths.get(commandLine.getOptionValue("batch-file"));
            List<Path> settingsFiles = new BatchFileReader().readPathsFromBatchFile(batchFilePath);
            logger.info("Running {} analyses from settings batch file.", settingsFiles.size());
            for (Path settingsFile : settingsFiles) {
                logger.info("Running settings: {}", settingsFile);
                Settings.SettingsBuilder settingsBuilder = commandLineOptionsParser.parseSettingsFile(settingsFile);
                runAnalysisFromSettings(settingsBuilder);
            }
        } else {
            //make a single SettingsBuilder
            Settings.SettingsBuilder settingsBuilder = commandLineOptionsParser.parseCommandLine(commandLine);
            runAnalysisFromSettings(settingsBuilder);
        }
    }

    private CommandLine parseCommandLineOptions(String[] args) {
        Parser parser = new GnuParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                printHelp();
                System.exit(0);
            }
            if (args.length == 0) {
                printHelp();
                logger.error("Please supply some command line arguments - none found");
                System.exit(0);
            }
            return commandLine;
        } catch (ParseException ex) {
            printHelp();
            logger.error("Unable to parse command line arguments. Please check you have typed the parameters correctly.", ex);
            System.exit(0);
            return null;
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

    private void runAnalysisFromSettings(Settings.SettingsBuilder settingsBuilder) {
        Settings settings = settingsBuilder.build();
        if (settings.isValid()) {
            Analysis analysis = settingsParser.parse(settings);
            runAnalysisAndWriteResults(analysis, settings);
        }
    }

    private void runAnalysisAndWriteResults(Analysis analysis, OutputSettings outputSettings) {
        exomiser.run(analysis);
        writeResults(analysis, outputSettings);
    }

    private void writeResults(Analysis analysis, OutputSettings outputSettings) {
        logger.info("Writing results");
        for (OutputFormat outFormat : outputSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = resultsWriterFactory.getResultsWriter(outFormat);
            resultsWriter.writeFile(analysis, outputSettings);
        }
    }

}
