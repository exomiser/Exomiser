/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.config.MainConfig;
import de.charite.compbio.exomiser.core.model.Exomiser;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import de.charite.compbio.exomiser.core.writer.ResultsWriter;
import de.charite.compbio.exomiser.core.writer.ResultsWriterFactory;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
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

    public static void main(String[] args) {
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
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
//        applicationContext.register();
//        applicationContext.register(MainConfig.class);
        
        logger.info("Running Exomiser build version {}", applicationContext.getBean("buildVersion"));
        
        Options options = applicationContext.getBean(Options.class);
        CommandLineParser commandLineOptionsParser = applicationContext.getBean(CommandLineParser.class);
        //There is no other input other than this settings object so most of what comes next could be wrapped back up into an exomiser class 
        SettingsBuilder settingsBuilder = commandLineOptionsParser.parseCommandLineArguments(args);
        settingsBuilder.buildVersion((String) applicationContext.getBean("buildVersion"));
        settingsBuilder.buildTimestamp((String) applicationContext.getBean("buildTimestamp"));
        
        ExomiserSettings exomiserSettings = settingsBuilder.build();
        //
        if (!exomiserSettings.isValid()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar exomizer-cli [...]", options);
            System.exit(1);
        }

        //3) Get the VCF file path (this creates a List of Variants)
        Path vcfFile = exomiserSettings.getVcfPath();
        //4) Get the PED file path if the VCF file has multiple samples
        //this can be null for single sample VCF files or refer to an actual file
        Path pedigreeFile = exomiserSettings.getPedPath();
        
        logger.info("CREATING AND ANNOTATING SAMPLE DATA");
        SampleDataFactory sampleDataFactory = (SampleDataFactory) applicationContext.getBean("sampleDataFactory");
        //now we have the sample data read in we can create a SampleData object to hold on to all the relvant information
        SampleData sampleData = sampleDataFactory.createSampleData(vcfFile, pedigreeFile);
        
        //run the analysis....
        Exomiser exomiser = (Exomiser) applicationContext.getBean("exomiser");
        exomiser.analyse(sampleData, exomiserSettings);
        
        logger.info("OUTPUTTING RESULTS");
        
        for (OutputFormat outFormat : exomiserSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = ResultsWriterFactory.getResultsWriter(outFormat);
            //TODO: remove priorityList - this should become another report
            List<Priority> priorityList = new ArrayList<>();
            resultsWriter.writeFile(sampleData, exomiserSettings, priorityList);
        }
        
        logger.info("FINISHED EXOMISER");

    }
}