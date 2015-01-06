/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.config.MainConfig;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.model.Exomiser;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import de.charite.compbio.exomiser.core.writer.ResultsWriter;
import de.charite.compbio.exomiser.core.writer.ResultsWriterFactory;
import de.charite.compbio.exomiser.priority.Priority;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private static AnnotationConfigApplicationContext applicationContext;

    private static Options options;

    private static String buildVersion;
    private static String buildTimestamp;
        
    public static void main(String[] args) {

        setup();
        showSplash();

        List<ExomiserSettings> sampleSettings = parseArgs(args);

        logger.info("Running exome analyis on {} samples:", sampleSettings.size());
        for (ExomiserSettings settings : sampleSettings) {
            runAnalysis(settings);
        }

    }

    private static void showSplash() {
        String splash = 
            "\n\n" +
            " Welcome to:               \n" +
            "  _____ _            _____                     _               \n" +
            " |_   _| |__   ___  | ____|_  _____  _ __ ___ (_)___  ___ _ __ \n" +
            "   | | | '_ \\ / _ \\ |  _| \\ \\/ / _ \\| '_ ` _ \\| / __|/ _ \\ '__|\n" +
            "   | | | | | |  __/ | |___ >  < (_) | | | | | | \\__ \\  __/ |   \n" +
            "   |_| |_| |_|\\___| |_____/_/\\_\\___/|_| |_| |_|_|___/\\___|_|   \n" +
            "                                                               \n" + 
            " A Tool to Annotate and Prioritize Exome Variants     v"+ buildVersion +"\n";

        logger.info("{}", splash);
    }

    private static void setup() {
        Locale.setDefault(Locale.UK);
        logger.info("Locale set to {}", Locale.getDefault());
        
        applicationContext = setUpApplicationContext();
        options = applicationContext.getBean(Options.class);
        buildVersion = (String) applicationContext.getBean("buildVersion");
        buildTimestamp = (String) applicationContext.getBean("buildTimestamp");
    }

     private static AnnotationConfigApplicationContext setUpApplicationContext() {
        //Get Spring started - this contains the configuration of the application
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        Path jarFilePath = null;
        try {
            jarFilePath = Paths.get(codeSource.getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            logger.error("Unable to find jar file", ex);
        }
        //this is set here so that Spring can load
        System.setProperty("jarFilePath", jarFilePath.toString());
        applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
        Path defaultOutputDir = jarFilePath.resolve(ExomiserSettings.DEFAULT_OUTPUT_DIR);
        try {
            if (!Files.exists(defaultOutputDir)) {
                Files.createDirectory(defaultOutputDir);
            }
        } catch (IOException ex) {
            logger.error("Unable to create default output directory for results {}", defaultOutputDir, ex);
        }
        return applicationContext;
    }

    private static void runAnalysis(ExomiserSettings exomiserSettings) {
        //3) Get the VCF file path (this creates a List of Variants)
        Path vcfFile = exomiserSettings.getVcfPath();
        logger.info("Running analysis for {}", vcfFile);
        //4) Get the PED file path if the VCF file has multiple samples
        //this can be null for single sample VCF files or refer to an actual file
        Path pedigreeFile = exomiserSettings.getPedPath();

        logger.info("Creating and annotating sample data");
        SampleDataFactory sampleDataFactory = applicationContext.getBean(SampleDataFactory.class);
        //now we have the sample data read in we can create a SampleData object to hold on to all the relvant information
        SampleData sampleData = sampleDataFactory.createSampleData(vcfFile, pedigreeFile);

        //run the analysis....
        Exomiser exomiser = applicationContext.getBean(Exomiser.class);
        exomiser.analyse(sampleData, exomiserSettings);

        logger.info("Writing results");

        for (OutputFormat outFormat : exomiserSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = ResultsWriterFactory.getResultsWriter(outFormat);
            //TODO: remove priorityList - this should become another report
            List<Priority> priorityList = new ArrayList<>();
            resultsWriter.writeFile(sampleData, exomiserSettings, priorityList);
        }

        logger.info("Finished analysis");
    }

    private static List<ExomiserSettings> parseArgs(String[] args) {

        List<SettingsBuilder> settingsBuilders = new ArrayList<>();
        CommandLineOptionsParser commandLineOptionsParser = applicationContext.getBean(CommandLineOptionsParser.class);
        try {
            Parser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                printHelp();
            }
            //check the args for a batch file first as this option is otherwise ignored 
            if (commandLine.hasOption("batch-file")) {
                Path batchFilePath = Paths.get(commandLine.getOptionValue("batch-file"));
                settingsBuilders.addAll(commandLineOptionsParser.parseBatchFile(batchFilePath));
            } else {
                //make a single SettingsBuilder
                settingsBuilders.add(commandLineOptionsParser.parseCommandLine(commandLine));
            }
        } catch (ParseException ex) {
            printHelp();
            logger.error("Unable to parse command line arguments. Please check you have typed the parameters correctly.", ex);    
        }
       
        List<ExomiserSettings> sampleSettings = new ArrayList<>();

        for (SettingsBuilder settingsBuilder : settingsBuilders) {
            settingsBuilder.buildVersion(buildVersion);
            settingsBuilder.buildTimestamp(buildTimestamp);

            ExomiserSettings exomiserSettings = settingsBuilder.build();

            if (exomiserSettings.isValid()) {
                sampleSettings.add(exomiserSettings);
            }
        }
        
        return sampleSettings;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String launchCommand = String.format("java -jar exomizer-cli-%s.jar [...]", buildVersion);
        formatter.printHelp(launchCommand, options);
    }
}
