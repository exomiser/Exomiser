/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.CommandLineOptionsParser;
import de.charite.compbio.exomiser.cli.options.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Spring configuration for setting-up the command-line options. If you want a
 * new option on the command line, add it here.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class CommandLineOptionsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptionsConfig.class);

    @Bean
    public CommandLineOptionsParser commandLineParser() {
        return new CommandLineOptionsParser();
    }

    /**
     * Add the options you want to be made available to the application here.
     *
     * @return the required OptionMarshallers for the system.
     */
    public Set<OptionMarshaller> desiredOptionMarshallers() {
        Set<OptionMarshaller> desiredOptionMarshallers = new LinkedHashSet<>();

        //commandline parser options
        desiredOptionMarshallers.add(new SettingsFileOptionMarshaller());
        desiredOptionMarshallers.add(new BatchFileOptionMarshaller());

        //analysis options
        desiredOptionMarshallers.add(new FullAnalysisOptionMarshaller());

        //sample data files 
        desiredOptionMarshallers.add(new VcfFileOptionMarshaller());
        desiredOptionMarshallers.add(new PedFileOptionMarshaller());

        //filter options
        desiredOptionMarshallers.add(new FrequencyThresholdOptionMarshaller());
        desiredOptionMarshallers.add(new FrequencyDbSnpOptionMarshaller());
        desiredOptionMarshallers.add(new GeneticIntervalOptionMarshaller());
        desiredOptionMarshallers.add(new QualityThresholdOptionMarshaller());
        desiredOptionMarshallers.add(new PathogenicityFilterCutOffOptionMarshaller());
        desiredOptionMarshallers.add(new TargetFilterOptionMarshaller());
        desiredOptionMarshallers.add(new GenesToKeepFilterOptionMarshaller());

        //prioritiser options
        desiredOptionMarshallers.add(new PrioritiserOptionMarshaller());
        desiredOptionMarshallers.add(new HpoIdsOptionMarshaller());
        desiredOptionMarshallers.add(new InheritanceModeOptionMarshaller());
        desiredOptionMarshallers.add(new SeedGenesOptionMarshaller());
        desiredOptionMarshallers.add(new DiseaseIdOptionMarshaller());
        desiredOptionMarshallers.add(new CandidateGeneOptionMarshaller());
        desiredOptionMarshallers.add(new HiPhiveOptionMarshaller());

        //output options
        desiredOptionMarshallers.add(new NumGenesOptionMarshaller());
        desiredOptionMarshallers.add(new OutFileOptionMarshaller());
        desiredOptionMarshallers.add(new OutFormatOptionMarshaller());

        return desiredOptionMarshallers;
    }

    @Bean
    public Map<String, OptionMarshaller> optionMarshallers() {
        Map<String, OptionMarshaller> optionMarshallers = new HashMap<>();

        for (OptionMarshaller optionMarshaller : desiredOptionMarshallers()) {
            String cliParameter = optionMarshaller.getCommandLineParameter();
            logger.debug("Adding {}", optionMarshaller);
            optionMarshallers.put(cliParameter, optionMarshaller);
        }
        return optionMarshallers;
    }

    @Bean
    public Options options() {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Shows this help"));
        options.addOption(new Option("H", "help", false, "Shows this help"));

        for (OptionMarshaller optionMarshaller : desiredOptionMarshallers()) {
            Option option = optionMarshaller.getOption();
            options.addOption(option);
        }

        return options;
    }

        //the original options:
//        options.addOption(new Option("h", "HELP_OPTION", false, "Shows this HELP_OPTION"));
//        options.addOption(new Option("H", "HELP_OPTION", false, "Shows this HELP_OPTION"));
//        options.addOption(new Option("v", "VCF_OPTION", true, "Path to VCF_OPTION file with mutations to be analyzed."));
//        options.addOption(new Option("o", "outfile", true, "name of out file (default: \"exomizer.html\")"));
//        options.addOption(new Option("l", "log", true, "Configuration file for logger"));
//        // / Filtering options
//        options.addOption(new Option("A", "omim_disease", true, "OMIM ID for disease being sequenced"));
//        options.addOption(new Option("B", "boqa", true, "comma-separated list of HPO terms for BOQA"));
//        options.addOption(new Option("D", "file_for_deserialising", true, "De-serialise"));
//        options.addOption(new Option("F", "freq_threshold", true, "Frequency threshold for variants"));
//        options.addOption(new Option("I", "inheritance", true, "Filter variants for inheritance pattern (AR,AD,X)"));
//        options.addOption(new Option("M", "mgi_phenotypes", false, "Filter variants for MGI phenodigm score"));
//
//        options.addOption(new Option("P", "path", false, "Filter variants for predicted pathogenicity"));
//        options.addOption(new Option("Q", "qual_threshold", true, "Quality threshold for variants"));
//        options.addOption(new Option("S", "SeedGenes", true, "Comma separated list of seed genes for random walk"));
//        options.addOption(new Option("W", "RWmatrix", true, "Random walk matrix file"));
//        options.addOption(new Option("X", "RWindex", true, "Random walk index file"));
//        options.addOption(new Option("Z", "zfin_phenotypes", false, "Filter variants for ZFIN phenodigm score"));
//
//        // Annotations that do not filter
//        options.addOption(new Option(null, "interval", true, "Restrict to interval (e.g., chr2:12345-67890)"));
//        options.addOption(new Option(null, "tsv", false, "Output tab-separated value (TSV) file instead of HTML"));
//        options.addOption(new Option(null, "vcf_output", false, "Output VCF_OPTION file instead of HTML"));
//        options.addOption(new Option(null, "CANDIDATE_GENE_OPTION", true, "Known or suspected gene association"));
//        options.addOption(new Option(null, "dbsnp", false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency)"));
//        options.addOption(new Option(null, "PED_OPTION", true, "pedigree (PED_OPTION) file"));
//        options.addOption(new Option(null, "hpo", true, "HPO Ontology (obo) file"));
//        options.addOption(new Option(null, "hpoannot", true, "HPO Annotations file"));
//        options.addOption(new Option(null, "HPO_IDS_OPTION", true, "HPO IDs for the sample being sequenced"));
//        options.addOption(new Option(null, "ngenes", true, "Number of genes to show in output"));
//        options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));
//        options.addOption(new Option(null, "phenomizerData", true, "Phenomizer data directory"));
}
