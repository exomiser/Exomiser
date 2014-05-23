/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.util;

import jannovar.common.ModeOfInheritance;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles parsing of the commandline input to provide properly typed data to
 * the rest of the application.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CommandLineOptionsParser {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptionsParser.class);

    private float frequency_threshold;
    /**
     * Quality threshold for variants. Corresponds to QUAL column in VCF file.
     */
    private float quality_threshold;
    /**
     * This String can be set to AD, AR, or X to initiate filtering according to
     * inheritance pattern.
     */
    private ModeOfInheritance inheritance_filter_type; //TODO: change name to modeOfInheritance
    private boolean filterOutAlldbSNP;
    private String disease;
    /**
     * Name of the disease gene family (an OMIM phenotypic series) that is being
     * used for prioritization with ExomeWalker.
     */
    private String diseaseGeneFamilyName;
    private List<String> hpo_ids;
        
        
    public CommandLineOptionsParser(String[] args) {

    }

    /**
     * Parse the command line.
     *
     * @param args Copy of the command line parameters.
     */
    public void parseCommandLineArguments(String[] args) {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Shows this help"));
        options.addOption(new Option("H", "help", false, "Shows this help"));

        //input files
        options.addOption(new Option("v", "vcf", true, "Path to VCF file with mutations to be analyzed. Can be either for an individual or a family. (mandatory)"));
        options.addOption(new Option(null, "ped", true, "pedigree (ped) file. (optional/mandatory - dependent on whether vcf file is for a family or individual)"));

        // Filtering options
        //Do filters filter-out or retain the options specified below? Would be good to spell this out in all cases.
        options.addOption(new Option("F", "freq_threshold", true, "Frequency threshold for variants. e.g. ")); // FrequencyFilter filter above or below threshold?
        options.addOption(new Option(null, "interval", true, "Restrict to interval (e.g., chr2:12345-67890)")); //IntervalFilter
        options.addOption(new Option("P", "path", false, "Filter variants for predicted pathogenicity"));//PathogenicityFilter 
        options.addOption(new Option("Q", "qual_threshold", true, "Quality threshold for variants as specifed in VCF 'QUAL' column")); //QualityFilter
        options.addOption(new Option("T", "keep_off_target_syn", false, "Leave in off-target, intronic and synonymous variants")); //TargetFilter

        // Prioritizer options 
        options.addOption(new Option("A", "omim_disease", true, "OMIM ID for disease being sequenced. e.g. OMIM:101600")); //OMIMPriority
        options.addOption(new Option("I", "inheritance", true, "Filter variants for inheritance pattern (AR, AD, X)")); //InheritancePriority change to DOMINANT / RECESSIVE / X ? Inclusive or exclusive?
        options.addOption(new Option("Z", "zfin_phenotypes", false, "Filter variants for ZFIN phenodigm score"));
        options.addOption(new Option(null, "candidate_gene", true, "Known or suspected gene association"));
        options.addOption(new Option(null, "dbsnp", false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency)"));
        options.addOption(new Option("M", "mgi_phenotypes", false, "Filter variants for MGI phenodigm score"));
        options.addOption(new Option(null, "hpo_ids", true, "HPO IDs for the sample being sequenced"));
        options.addOption(new Option("S", "SeedGenes", true, "Comma separated list of seed genes for random walk"));

        //output options
        options.addOption(new Option(null, "ngenes", true, "Number of genes to show in output"));
        options.addOption(new Option(null, "tsv", false, "Output tab-separated value (TSV) file instead of HTML"));
        options.addOption(new Option(null, "vcf_output", false, "Output VCF file instead of HTML"));
        options.addOption(new Option("o", "outfile", true, "name of out file (default: \"exomizer.html\")"));
        options.addOption(new Option("l", "log", true, "Configuration file for logger"));

        //TODO: check what this actually does
        options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));

        //resource path options no longer required
        options.addOption(new Option("D", "file_for_deserialising", true, "De-serialise"));
        options.addOption(new Option("W", "RWmatrix", true, "Random walk matrix file"));
        options.addOption(new Option("X", "RWindex", true, "Random walk index file"));
        options.addOption(new Option(null, "phenomizerData", true, "Phenomizer data directory"));
        options.addOption(new Option(null, "hpo", true, "HPO Ontology (obo) file"));
        options.addOption(new Option(null, "hpoannot", true, "HPO Annotations file"));

        Parser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h") || cmd.hasOption("H") || args.length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar Exomizer [...]", options);
                System.exit(0);
            }
        } catch (ParseException pe) {
            logger.error("Error parsing command line options: {}", pe);
            System.exit(1);
        }
       
//        TODO: figure this lot out
//        if (cmd.hasOption("B")) {
//            setBOQA_TermList(cmd.getOptionValue("B"));
//            setUseBoqa();
//        }
////            if (cmd.hasOption("D")) {
////                setUCSCserializedFile(cmd.getOptionValue("D"));
////            }
//        if (cmd.hasOption("F")) {
//            setFrequencyThreshold(cmd.getOptionValue("F"));
//        }
//        if (cmd.hasOption("I")) {
//            setInheritanceFilter(cmd.getOptionValue("I"));
//        }
//        if (cmd.hasOption("P")) {
//            setUsePathogenicityFilter(true);
//        } else {
//            setUsePathogenicityFilter(false);
//        }
//        if (cmd.hasOption("T")) {
//            this.use_target_filter = false;
//        }
//        if (cmd.hasOption("Q")) {
//            setQualityThreshold(cmd.getOptionValue("Q"));
//        }
//
//        if (cmd.hasOption("o")) {
//            setOutfile(cmd.getOptionValue("o"));
//        }
//        if (cmd.hasOption("v")) {
//            setVCFfile(cmd.getOptionValue('v'));
//        } else if (cmd.hasOption("V")) {
//            setVCFfile(cmd.getOptionValue('V'));
//        }
//
//        if (cmd.hasOption("interval")) {
//            setInterval(cmd.getOptionValue("interval"));
//        }
//        if (cmd.hasOption("tsv")) {
//            setUseTSV(true);
//        }
//        if (cmd.hasOption("vcf_output")) {
//            setUseVCF(true);
//        }
//        if (cmd.hasOption("ngenes")) {
//            String n = cmd.getOptionValue("ngenes");
//            setNumberOfGenesToShow(n);
//        }
//
//        if (cmd.hasOption("candidate_gene")) {
//            setCandidateGene(cmd.getOptionValue("candidate_gene"));
//        }
//        if (cmd.hasOption("dbsnp")) {
//            setFilterOutAlldbSNP(true);
//        }
//
//        if (cmd.hasOption("withinFirewall")) {
//            setWithinFirewall();
//        }
//        if (cmd.hasOption("ped")) {
//            setPedFile(cmd.getOptionValue("ped"));
//        }
//        if (cmd.hasOption("hpo_ids")) {
//            setHPOids(cmd.getOptionValue("hpo_ids"));
//        }
//        /**
//         * *
//         * The following commands are the entry points into particular types of
//         * HPO analysis: Generic, clinically relevant Exome server, Exome Walker
//         * analysis. Combinations of arguments set flags that will control the
//         * behaviour of the program. At least one condition must be met to start
//         * the analysis. Otherwise, an error message is written to STDOUT.
//         */
//        // / --hpo ${HPO} --hpoannot ${HPANNOT} --phenomizerData ${PHMDATA}
//        // --hpo_ids ${HPTERMS}
//	    /*
//         * 1) Clinically relevant exome server
//         */
//        if (cmd.hasOption("phenomizerData") && cmd.hasOption("hpo_ids")) {
//            setPhenomizerDataDirectory(cmd.getOptionValue("phenomizerData"));
//            setHPOids(cmd.getOptionValue("hpo_ids"));
//            setDoClinicallyRelevantExomeServer();
//        } /*
//         * 2) Phenotype based Random walk (PhenoWanderer) analysis
//         */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("A")) {
//            setRandomWalkFilePath(cmd.getOptionValue("W"));
//            setRandomWalkIndexPath(cmd.getOptionValue("X"));
//            setTargetDisease(cmd.getOptionValue("A"));
//            //setDoPhenoRandomWalk();
//        } /*
//         * 2) Phenotype based Random walk (PhenoWanderer) analysis using HPO
//         * IDs
//         */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("hpo_ids")) {
//            setRandomWalkFilePath(cmd.getOptionValue("W"));
//            setRandomWalkIndexPath(cmd.getOptionValue("X"));
//            setHPOids(cmd.getOptionValue("hpo_ids"));
//            //setDoDynamicPhenoRandomWalk();
//        } /*
//         * 2) Random walk (GeneWanderer) analysis
//         */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("S")) {
//            setRandomWalkFilePath(cmd.getOptionValue("W"));
//            setRandomWalkIndexPath(cmd.getOptionValue("X"));
//            setEntrezSeedGenes(cmd.getOptionValue("S"));
//            setDoRandomWalk();
//        } /*
//         * 3) ZFIN Phenodigm prioritization
//         */ else if (cmd.hasOption("Z") && cmd.hasOption("A")) {
//            setTargetDisease(cmd.getOptionValue("A"));
//            setUseZFINphenodigmFilter(true);
//            setUseMGIphenodigmFilter(false);
//        } /*
//         * 3) MGI Phenodigm prioritization
//         */ else if (cmd.hasOption("M") && cmd.hasOption("A")) {
//            setTargetDisease(cmd.getOptionValue("A"));
//            setUseZFINphenodigmFilter(false);
//            setUseMGIphenodigmFilter(true);
//        } else {
//            logger.warn("Non-standard combination of arguments passed to perform analysis.");
//        }

    }

    /**
     * This function is used to ensure that certain options are passed to the
     * program before we start execution.
     *
     * @param cmd An apache CommandLine object that stores the command line
     * arguments
     * @param name Name of the argument that must be present
     * @return Value of the required option as a String.
     */
    private static String getRequiredOptionValue(CommandLine cmd, char name) {
        String val = cmd.getOptionValue(name);
        if (val == null) {
            logger.error("Aborting because the required argument -{} wasn't specified! Use the -h for more help.", name);
            System.exit(-1);
        }
        return val;
    }

}
