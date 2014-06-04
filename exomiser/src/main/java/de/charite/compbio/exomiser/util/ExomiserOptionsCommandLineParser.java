/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.util;

import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.util.ExomiserSettings.ExomiserOptionsBuilder;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles parsing of the commandline input to provide properly typed data to
 * the rest of the application in the form of an ExomiserSettings object.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class ExomiserOptionsCommandLineParser {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserOptionsCommandLineParser.class);

    @Autowired
    private final Options options;

    public ExomiserOptionsCommandLineParser(Options options) {
        this.options = options;
    }

    /**
     * Parse the command line.
     *
     * @param args the list of arguments from the command line
     * @return an ExomiserSettings object built from the command line.
     * @throws org.apache.commons.cli.ParseException
     */
    public ExomiserSettings parseCommandLineArguments(String[] args) throws ParseException {

        Parser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        return parseCommandLine(cmd);

    }

    private ExomiserSettings parseCommandLine(CommandLine commandLine) throws ParseException {

        logger.info("Parsing {} command line options:", commandLine.getOptions().length);

        ExomiserOptionsBuilder optionsBuilder = new ExomiserSettings.ExomiserOptionsBuilder();
        for (Option option : commandLine.getOptions()) {
            logger.info("-{} --{} : {}", option.getOpt(), option.getLongOpt(), option.getValues());
            switch (option.getLongOpt()) {
                //REQUIRED
                case "vcf":
                    optionsBuilder.vcfFilePath(Paths.get(option.getValue()));
                    break;
                case "ped":
                    optionsBuilder.pedFilePath(Paths.get(option.getValue()));
                    break;
                case "prioritiser":
                    optionsBuilder.usePrioritiser(PriorityType.valueOfCommandLine(option.getValue()));
                    break;

                //FILTER OPTIONS
                case "max-freq":
                    optionsBuilder.maximumFrequency(Float.parseFloat(option.getValue()));
                    break;
                case "restrict-interval":
                    optionsBuilder.geneticInterval(option.getValue());
                    break;
                case "min-qual":
                    optionsBuilder.minimumQuality(Float.parseFloat(option.getValue()));
                    break;
                case "include-pathogenic":
                    //default is false
                    optionsBuilder.includePathogenic(true);
                    break;
                case "remove-dbsnp":
                    //default is true
                    optionsBuilder.removeDbSnp(false);
                    break;
                case "remove-off-target-syn":
                    //default is true
                    optionsBuilder.removeOffTargetVariants(false);
                    break;

                //PRIORITISER OPTIONS
                case "candidate-gene":
                    optionsBuilder.candidateGene(option.getValue());
                    break;
                case "hpo-ids":
                    optionsBuilder.hpoIdList(makeList(option.getLongOpt(), option.getValue()));
                    break;
                case "seed-genes":
                    optionsBuilder.seedGeneList(makeList(option.getLongOpt(), option.getValue()));
                    break;
                case "disease-id":
                    optionsBuilder.diseaseId(option.getValue());
                    break;
                case "inheritance-mode":
                    optionsBuilder.modeOfInheritance(parseInheritanceMode(option.getValue()));
                    break;

                //OUTPUT OPTIONS
                case "num-genes":
                    optionsBuilder.numberOfGenesToShow(Integer.parseInt(option.getValue()));
                    break;
                case "out-file":
                    optionsBuilder.outFileName(option.getValue());
                    break;
                case "out-format":
                    optionsBuilder.outputFormat(parseOutputFormat(option.getValue()));
                    break;
            }
        }

        //TODO: 
        //Alternatively a ExomiserOptionsBuilder pattern on the ExomiserSettings which would validate them when build() is called?
        return optionsBuilder.build();

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

    @Override
    public String toString() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar exomizer-n.n.n.jar", options);
        return "ExomiserCommandLineOptionsParser{" + options + '}';
    }

    private List<String> makeList(String option, String value) {
        String delimiter = ",";
        
        if (!value.contains(delimiter)) {
            logger.error("Unable to parse {} value - please specify a list of comma-separated strings with no white space separator. e.g. OMIM:12345,OMIM:23456,OMIM:98765", option);
            return new ArrayList();
        }

        return Arrays.asList(value.split(delimiter));
    }

    private ModeOfInheritance parseInheritanceMode(String value) {
        switch (value) {
            case "AR":
                return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case "AD":
                return ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case "X":
                return ModeOfInheritance.X_RECESSIVE;
            default:
                logger.error("value {} is not one of AR, AD or X - inheritance mode has not been set", value);
                return ModeOfInheritance.UNINITIALIZED;
        }
    }

    private OutputFormat parseOutputFormat(String value) {
        switch (value) {
            case "HTML":
                return OutputFormat.HTML;
            case "TAB":
                return OutputFormat.TAB;
            case "TSV":
                return OutputFormat.TAB;
            case "VCF":
                return OutputFormat.VCF;
            default:
                return OutputFormat.HTML;
        }
    }

}
