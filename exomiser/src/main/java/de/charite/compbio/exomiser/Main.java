/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser;

import de.charite.compbio.exomiser.common.SampleData;
import de.charite.compbio.exomiser.common.SampleDataFactory;
import de.charite.compbio.exomiser.config.MainConfig;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.util.CommandLineOptionsParser;
import de.charite.compbio.exomiser.util.VariantAnnotator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
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

    public static void main(String argv[]) {
        //Get Spring started - this contains the configuration of the application
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
        DataSource dataSource = applicationContext.getBean(DataSource.class);
//        logger.info(dataSource.toString());
        // 1) Open the connection to database.
        //Exomiser shouldn't need a datasource directly - this is only needed by some filters and prioritisers
        //and is supplied to them by the PriorityFactory or FilterFactory
        Exomizer exomizer = new Exomizer();
        exomizer.setDataSource(dataSource);

        //This is going to be a pretty crucial bit for passing in the relevant 
        //settings to the exomiser class
//        Options options = OptionParser.parseCommandLine(argv);
//        exomizer.parseOptions(options);
        //alternative:
//         Exomizer exomizer = new Exomizer(options, dataSource);

//        CommandLineOptionsParser cliParser = new CommandLineOptionsParser(argv);

        exomizer.parseCommandLineArguments(argv);
//        inheritance_mode, disease, hpo_ids, candidateGene are actual input variables

//        try {
//              This is now only used by the HTMLWriters
//            exomizer.openNewDatabaseConnection();
//        } catch (ExomizerInitializationException ex) {
//            logger.error("Could not open SQL connection. Terminating program...", ex);
//            System.exit(1);
//        }
        //2) Input the transcript definition file from Jannovar that is used to annotate the variants.
        //superfluous as this is a default and always needed. This and the wandom walk matrix should be provided 
        //by the ApplicationContext
//        try {
//            exomizer.deserializeUCSCdata();
        
//        } catch (ExomizerException e) {
//            logger.error("Error with deserialization:", e);
//            System.exit(1);
//        }

        //3) Read a VCF file (this creates a List of Variants)
        //4) Read a PED file if the VCF file has multiple samples
        //get this from the command line or Settings object, this is just short-circuiting things
        Path vcfFile = Paths.get(exomizer.getVCFfile());
        //this can be null or refer to an actual file
        Path pedigreeFile = Paths.get(exomizer.getPedFile());
        SampleData sampleData = SampleDataFactory.createSampleData(vcfFile, pedigreeFile);
        if (sampleData.getPedigree() == null) {
            logger.error("CRITICAL! Sample data has no pedigree - unable to continue analysis.");
            logger.error("Check pedigree file was specified if you are using a family VCF file and the file is available.");
            logger.error("PROGRAM WILL NOW EXIT. BYE!");
            try {
                //give the application a second to finish the logging then die.
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.error(null, ex);
            } finally {
               System.exit(-1);
            }
        }
        List<VariantEvaluation> variantList = sampleData.getVariantEvaluations();
        
        logger.info("ANNOTATING VARIANTS");
        //annotation is independent of the filtering and exomising
//            exomizer.annotateVariants();
        VariantAnnotator variantAnnotator = applicationContext.getBean(VariantAnnotator.class);
        variantAnnotator.annotateVariants(variantList);

        //split out filtering and prioritising?
        //this is currently handled by Prioritiser but should probably be part of Main / Exomiser
        try {
            //5) This function takes care of most of the analysis.
            logger.info("INIT FILTERS AND PRIORITISERS");
            //get a list of Filters and Prioritisers from the Settings object?
            exomizer.initializeFiltersAndPrioritizers();
//            exomizer.setFilters(filterList);
//            exomizer.setPriorities(priorityList);
        } catch (ExomizerException e) {
            logger.error("Error while prioritizing VCF data: ", e);
            System.exit(1);
        }

        logger.info("FILTERING VARIANTS AND PRIORITISING GENES");
        //for Exomiser in ExomiserList, Exomiser.execute()
        List<Gene> prioritisedGenes = exomizer.executePrioritization(variantList);
        sampleData.setGeneList(prioritisedGenes);
        
//            exomizer.executePrioritization(filterList);
            
            //currently this lot is part of Prioritiser, but it might fit better in 
            //either Main as part of the overall application flow as it always runs
            //or maybe in Exomiser rather than Prioritiser
//            logger.info("FILTERING VARIANTS");
//            filterVariants(variantList);
//            /**************************************************************/
//            /* 2) Prioritize the variants according to phenotype, model */
//            /* organism data, protein protein interactions, whatever */
//            /**************************************************************/
//            geneList = makeGenesFromVariants(variantList);
//            logger.info("PRIORITISING GENES");
//            prioritizeGenes(geneList);
//            /**************************************************************/
//            /* 3) Rank all genes now according to combined score. */
//            /**************************************************************/
//            logger.info("RANKING GENES");
//            if (rankBasedScoring) {
//                scoreCandidateGenesByRank();
//            } else {
//                rankCandidateGenes();
//            }
//            return geneList;

        
        logger.info("OUTPUTTING RESULTS");

        //tempory setting of names and pedigree so that the HTMLWriters work
        exomizer.setSampleNames((ArrayList<String>) sampleData.getSampleNames());
        exomizer.setPedigree(sampleData.getPedigree());        
        //tempory setting of genes and variants so that the HTMLWriters work
        exomizer.setGeneList(sampleData.getGeneList());
        exomizer.setVariantList(sampleData.getVariantEvaluations());
            
        //6) Output to HTML (default) or TSV (needs to be set via the --tsv flag on the command line)
        if (exomizer.useTSVFile()) {
            exomizer.outputTSV();
        } else if (exomizer.useVCFFile()) {
            exomizer.outputVCF();
        } else {
            /*
             * The following function decides based on the flag useCRE, useBOQA,
             * or useRandomWalk what kind of HTML to produce.
             */
            try {
                exomizer.outputHTML();
            } catch (ExomizerException e) {
                logger.error("Error writing output: ", e);
                System.exit(1);
            }
        }
        logger.info("FINISHED EXOMISER");

    }

}
