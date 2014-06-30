/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.config.MainConfig;
import de.charite.compbio.exomiser.common.SampleData;
import de.charite.compbio.exomiser.common.SampleDataFactory;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.filter.FilterFactory;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.ScoringMode;
import de.charite.compbio.exomiser.util.ExomiserSettings;
import de.charite.compbio.exomiser.util.Prioritiser;
import de.charite.compbio.exomiser.util.VariantAnnotator;
import de.charite.compbio.exomiser.writer.ResultsWriter;
import de.charite.compbio.exomiser.writer.ResultsWriterFactory;
import java.nio.file.Path;
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
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);

        Options options = applicationContext.getBean(Options.class);
        ExomiserOptionsCommandLineParser commandLineOptionsParser = applicationContext.getBean(ExomiserOptionsCommandLineParser.class);
        //There is no other input other than this settings object so most of what comes next could be wrapped back up into an exomiser class 
        ExomiserSettings exomiserSettings = commandLineOptionsParser.parseCommandLineArguments(args);
        //
        if (exomiserSettings == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar Exomizer [...]", options);
            System.exit(1);
        }

        //3) Read a VCF file (this creates a List of Variants)
        Path vcfFile = exomiserSettings.getVcfPath();
        //4) Read a PED file if the VCF file has multiple samples
        //this can be null or refer to an actual file
        Path pedigreeFile = exomiserSettings.getPedPath();
        //now we have the sample data read in we can create a SampleData object to hold on to all the relvant information
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
               System.exit(1);
            }
        }
        FilterFactory filterFactory = applicationContext.getBean(FilterFactory.class);
        List<Filter> filterList = filterFactory.makeFilters(exomiserSettings);
        logger.info("Prepared filters: {}", filterList);
        //create the priority factory - this will deliberately fail if there are
        //incorrect input options for the specified prioritiser. 
        PriorityFactory priorityFactory = applicationContext.getBean(PriorityFactory.class);
        List<Priority> priorityList = priorityFactory.makePrioritisers(exomiserSettings);
        logger.info("Prepared prioritsers: {}", priorityList);

        List<VariantEvaluation> variantList = sampleData.getVariantEvaluations();
        
        logger.info("ANNOTATING VARIANTS");
        //annotation is independent of the filtering and exomising.
        VariantAnnotator variantAnnotator = applicationContext.getBean(VariantAnnotator.class);
        //Variants are annotated with KnownGene data from UCSC (see the application context)
        variantAnnotator.annotateVariants(variantList);

        //split out filtering and prioritising? e.g.
        //List<VariantEvaluation> filteredVariants = VariantFilterer.filter(variantList, filterList);
        //this is currently handled by Prioritiser but should probably be part of an Exomiser - 
        //e.g. RareDiseaseExomiser would OMIMPriority by default (the current situation)
        Prioritiser prioritiser = new Prioritiser(exomiserSettings.getModeOfInheritance(), filterList, priorityList);
        
        logger.info("FILTERING VARIANTS AND PRIORITISING GENES");
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        ScoringMode scoreMode = exomiserSettings.getPrioritiserType().getScoringMode();
        logger.info("Using {} scoring mode: {}", exomiserSettings.getPrioritiserType(), scoreMode);
        List<Gene> prioritisedGenes = prioritiser.executePrioritization(variantList, scoreMode);
//        List<Gene> prioritisedGenes = exomizer.executePrioritization(variantList);
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

        ResultsWriter resultsWriter = ResultsWriterFactory.getResultsWriter(exomiserSettings.getOutputFormat());
        resultsWriter.write(sampleData, exomiserSettings, filterList, priorityList);
        
        logger.info("FINISHED EXOMISER");

    }

}

//        try {
            //5) This function takes care of most of the analysis.
//            logger.info("INIT FILTERS AND PRIORITISERS");
            //get a list of Filters and Prioritisers from the Settings object?
//            exomizer.initializeFiltersAndPrioritizers();
//            exomizer.setFilters(filterList);
//            exomizer.setPriorities(priorityList);
//        } catch (ExomizerException e) {
//            logger.error("Error while prioritizing VCF data: ", e);
//            System.exit(1);
//        }
//        //6) Output to HTML (default) or TSV (needs to be set via the --tsv flag on the command line)
//        if (exomizer.useTSVFile()) {
//            exomizer.outputTSV();
//        } else if (exomizer.useVCFFile()) {
//            exomizer.outputVCF();
//        } else {
//            /*
//             * The following function decides based on the flag useCRE, useBOQA,
//             * or useRandomWalk what kind of HTML to produce.
//             */
//            try {
//                exomizer.outputHTML();
//            } catch (ExomizerException e) {
//                logger.error("Error writing output: ", e);
//                System.exit(1);
//            }
//        }