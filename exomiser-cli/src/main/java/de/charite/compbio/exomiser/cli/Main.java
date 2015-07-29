/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.config.MainConfig;
import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.AnalysisFactory;
import de.charite.compbio.exomiser.core.AnalysisMode;
import de.charite.compbio.exomiser.core.AnalysisParser;
import de.charite.compbio.exomiser.core.AnalysisRunner;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.writers.OutputSettings;
import de.charite.compbio.exomiser.core.writers.ResultsWriter;
import de.charite.compbio.exomiser.core.writers.ResultsWriterFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main class for calling off the command line in the Exomiser package.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_OUTPUT_DIR = "results";

    private AnnotationConfigApplicationContext applicationContext;

    private Options options;

    private Exomiser exomiser;
    private ResultsWriterFactory resultsWriterFactory;
    private AnalysisParser analysisParser;
    private AnalysisFactory analysisFactory;
    private String buildVersion;

    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }

    private void run(String[] args) {
        setup();
        showSplash();
        //TODO: this should return a list of Analysis- either convert the settings/cli input to an Analysis or add one directly from an analysis yaml file
        //then move ExomiserSettings into this package from core.
        runAnalyses(args);
        logger.info("Exomising finished - Bye!");
    }

    private void setup() {
        Locale.setDefault(Locale.UK);
        logger.info("Locale set to {}", Locale.getDefault());
        Path jarFilePath = getJarFilePath();
        applicationContext = setUpApplicationContext(jarFilePath);
        createDefaultOutputDirIfNotExists(jarFilePath);

        options = applicationContext.getBean(Options.class);

        exomiser = applicationContext.getBean(Exomiser.class);
        resultsWriterFactory = applicationContext.getBean(ResultsWriterFactory.class);
        analysisParser = applicationContext.getBean(AnalysisParser.class);
        analysisFactory = applicationContext.getBean(AnalysisFactory.class);

        buildVersion = (String) applicationContext.getBean("buildVersion");
    }

    private Path getJarFilePath() {
        //Get Spring started - this contains the configuration of the application
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        try {
            return Paths.get(codeSource.getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            logger.error("Unable to find jar file", ex);
            throw new RuntimeException("Unable to find jar file", ex);
        }
    }

    private AnnotationConfigApplicationContext setUpApplicationContext(Path jarFilePath) {
        //this is set here so that Spring can load
        System.setProperty("jarFilePath", jarFilePath.toString());
        applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
        return applicationContext;
    }

    private void createDefaultOutputDirIfNotExists(Path jarFilePath) {
        Path defaultOutputDir = jarFilePath.resolve(DEFAULT_OUTPUT_DIR);
        try {
            if (!Files.exists(defaultOutputDir)) {
                Files.createDirectory(defaultOutputDir);
            }
        } catch (IOException ex) {
            logger.error("Unable to create default output directory for results {}", defaultOutputDir, ex);
        }
    }

    private void showSplash() {
        String splash
                = "\n\n"
                + " Welcome to:               \n"
                + "  _____ _            _____                     _               \n"
                + " |_   _| |__   ___  | ____|_  _____  _ __ ___ (_)___  ___ _ __ \n"
                + "   | | | '_ \\ / _ \\ |  _| \\ \\/ / _ \\| '_ ` _ \\| / __|/ _ \\ '__|\n"
                + "   | | | | | |  __/ | |___ >  < (_) | | | | | | \\__ \\  __/ |   \n"
                + "   |_| |_| |_|\\___| |_____/_/\\_\\___/|_| |_| |_|_|___/\\___|_|   \n"
                + "                                                               \n"
                + " A Tool to Annotate and Prioritize Exome Variants     v" + buildVersion + "\n";

        System.out.println(splash);
    }

    private void runAnalyses(String[] args) {
        CommandLineOptionsParser commandLineOptionsParser = applicationContext.getBean(CommandLineOptionsParser.class);
        try {
            Parser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, args);
            if (args.length == 0 || commandLine.hasOption("help")) {
                printHelp();
                System.exit(0);
            }

            if (commandLine.hasOption("analysis")) {
                Path analysisScript = Paths.get(commandLine.getOptionValue("analysis"));
                runAnalysisFromScript(analysisScript);
            } else if (commandLine.hasOption("analysis-batch")) {
                Path analysisBatchFile = Paths.get(commandLine.getOptionValue("analysis-batch"));
                List<Path> analysisScripts = new BatchFileReader().readPathsFromBatchFile(analysisBatchFile);
                //this *can* be run in parallel using parallelStream() at the expense of RAM in order to hold all the variants in memory.
                //like this:
                //analysisScripts.parallelStream().forEach(this::runAnalysisFromScript);
                //HOWEVER there may be threading issues so this needs investigation.
                analysisScripts.forEach(this::runAnalysisFromScript);
            }

            //check the args for a batch file first as this option is otherwise ignored 
            else if (commandLine.hasOption("batch-file")) {
                Path batchFilePath = Paths.get(commandLine.getOptionValue("batch-file"));
                List<Path> settingsFiles = new BatchFileReader().readPathsFromBatchFile(batchFilePath);
                for (Path settingsFile : settingsFiles) {
                    SettingsBuilder settingsBuilder = commandLineOptionsParser.parseSettingsFile(settingsFile);
                    runAnalysisFromSettings(settingsBuilder);
                }
            } else {
                //make a single SettingsBuilder
                SettingsBuilder settingsBuilder = commandLineOptionsParser.parseCommandLine(commandLine);
                runAnalysisFromSettings(settingsBuilder);
            }
        } catch (ParseException ex) {
            printHelp();
            logger.error("Unable to parse command line arguments. Please check you have typed the parameters correctly.", ex);
        }
    }

    private void runAnalysisFromScript(Path analysisScript) {
        Analysis analysis = analysisParser.parseAnalysis(analysisScript);
        OutputSettings outputSettings = analysisParser.parseOutputSettings(analysisScript);
        runAnalysis(analysis);
        writeResults(analysis, outputSettings);
    }

    private void runAnalysisFromSettings(SettingsBuilder settingsBuilder) {
        ExomiserSettings settings = settingsBuilder.build();
        if (settings.isValid()) {
            Analysis analysis = exomiser.setUpExomiserAnalysis(settings);
            runAnalysis(analysis);
            writeResults(analysis, settings);
        }
    }

    private void runAnalysis(Analysis analysis) {
        AnalysisRunner runner = makeAnalysisRunner(analysis);
        runner.runAnalysis(analysis);
    }

    private AnalysisRunner makeAnalysisRunner(Analysis analysis) {
        AnalysisMode analysisMode = analysis.getAnalysisMode();
        logger.info("Running analysis in {} mode", analysisMode);
        switch (analysisMode) {
            case FULL:
                return analysisFactory.getFullAnalysisRunner();
            case SPARSE:
                return analysisFactory.getSparseAnalysisRunner();
            case PASS_ONLY:
                return analysisFactory.getPassOnlyAnalysisRunner();
            default:
                //this guy takes up the least RAM
                return analysisFactory.getPassOnlyAnalysisRunner();
        }
    }

    private void writeResults(Analysis analysis, OutputSettings outputSettings) {
        logger.info("Writing results");
        for (OutputFormat outFormat : outputSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = resultsWriterFactory.getResultsWriter(outFormat);
            resultsWriter.writeFile(analysis, outputSettings);
        }
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String launchCommand = String.format("java -jar exomizer-cli-%s.jar [...]", buildVersion);
        formatter.printHelp(launchCommand, options);
    }
}
