/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import static de.charite.compbio.exomiser.cli.CommandLineOption.*;
import de.charite.compbio.exomiser.cli.CommandLineParser;
import static de.charite.compbio.exomiser.filter.FilterType.*;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.util.OutputFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for setting-up the command-line options. If you want a
 * new option on the command line, add it here.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class CommandLineOptionsConfig {

    @Bean
    public CommandLineParser commandLineParser() {
        return new CommandLineParser(options());
    }

    @Bean
    protected Options options() {
        Options options = new Options();

        addHelpOptions(options);
        addSettingsFileOptions(options);
        addSampleDataOptions(options);
        addFilterOptions(options);
        addPrioritiserOptions(options);
        addOutputOptions(options);

        return options;
    }

    private void addHelpOptions(Options options) {
        options.addOption(new Option("h", HELP_OPTION.getLongOption(), false, "Shows this help"));
        options.addOption(new Option("H", HELP_OPTION.getLongOption(), false, "Shows this help"));
    }

    private void addSettingsFileOptions(Options options) {
        options.addOption(OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription("Path to settings file. Any settings specified in the file will be overidden by parameters added on the command-line.")
                .withLongOpt(SETTINGS_FILE_OPTION.getLongOption())
                .create()
        );
    }

    private void addSampleDataOptions(Options options) {
        //input files - at least the VCF_OPTION file is required!
        Option inputVcf = OptionBuilder
                .withArgName("file")
                //                .isRequired()
                .hasArg()
                .withDescription("Path to VCF file with mutations to be analyzed. Can be either for an individual or a family.")
                .withLongOpt(VCF_OPTION.getLongOption())
                .create("v");
        options.addOption(inputVcf);

        options.addOption(OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription("Path to pedigree (ped) file. Required if the vcf file is for a family.")
                .withLongOpt(PED_OPTION.getLongOption())
                .create("p")
        );
    }

    private void addFilterOptions(Options options) {
        // Filtering options
        //Do filters filter-out or retain the options specified below? Would be good to spell this out in all cases.
        options.addOption(new Option("F", MAX_FREQ_OPTION.getLongOption(), true, "Maximum frequency threshold for variants to be retained. e.g. 100.00 will retain all variants. Default: 100.00")); // FrequencyFilter filter above or below threshold?
        options.addOption(new Option("R", INTERVAL_OPTION.getLongOption(), true, "Restrict to region/interval (e.g., chr2:12345-67890)")); //IntervalFilter
        options.addOption(new Option("Q", MIN_QUAL_OPTION.getLongOption(), true, "Mimimum quality threshold for variants as specifed in VCF 'QUAL' column.  Default: 0")); //QualityFilter
        //no extra args required - these are Booleans 
        options.addOption(new Option("P", INCLUDE_PATHOGENIC_OPTION.getLongOption(), false, "Filter variants to include those with predicted pathogenicity. Default: false"));//PathogenicityFilter 
        options.addOption(new Option(null, REMOVE_DBSNP_OPTION.getLongOption(), false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency).  Default: false"));
        //TODO: WTF is going on with PathogenicityFilter? It actualy needs boolean filterOutNonpathogenic, boolean removeSynonomousVariants
        //but these set (or don't set) things in the PathogenicityTriage - maybe we could have a MissensePathogenicityFilter too? 
        options.addOption(new Option("O", "exclude-pathogenic-missense", false, "Filter variants to include those with predicted pathogenicity - MISSENSE MUTATIONS ONLY"));//PathogenicityFilter 
        options.addOption(new Option("T", REMOVE_OFF_TARGET_OPTION.getLongOption(), false, "Keep off-target variants. These are defined as intergenic, intronic, upstream, downstream, synonymous or intronic ncRNA variants. Default: true")); //TargetFilter 
    }

    private void addPrioritiserOptions(Options options) {
        // Prioritiser options - may or may not be required depending on the priotitiser chosen.
        options.addOption(new Option(null, CANDIDATE_GENE_OPTION.getLongOption(), true, "Known or suspected gene association e.g. FGFR2"));
        options.addOption(new Option(null, HPO_IDS_OPTION.getLongOption(), true, "Comma separated list of HPO IDs for the sample being sequenced e.g. HP:0000407,HP:0009830,HP:0002858"));
        options.addOption(new Option("S", SEED_GENES_OPTION.getLongOption(), true, "Comma separated list of seed genes (Entrez gene IDs) for random walk"));
        //Prioritisers - Apart from the disease and inheritance prioritisers are all mutually exclusive.
        options.addOption(new Option("D", DISEASE_ID_OPTION.getLongOption(), true, "OMIM ID for disease being sequenced. e.g. OMIM:101600")); //OMIMPriority
        options.addOption(new Option("I", INHERITANCE_MODE_OPTION.getLongOption(), true, "Filter variants for inheritance pattern (AR, AD, X)")); //InheritancePriority change to DOMINANT / RECESSIVE / X ? Inclusive or exclusive?
        //The desired PRIORITISER_OPTION e.g. --PRIORITISER_OPTION=pheno-wanderer or --PRIORITISER_OPTION=zfin-phenodigm
        //this is less ambiguous to the user and makes for easier parsing. Can then check that all the required fields are present before proceeding.
        String prioritiserLongOpt = PRIORITISER_OPTION.getLongOption();
        //now we have the description, build the option.
        Option priorityOption = OptionBuilder
                //                .isRequired()
                .hasArg()
                .withArgName("name")
                .withValueSeparator()
                .withDescription(buildPrioritiserDescription(prioritiserLongOpt))
                .withLongOpt(prioritiserLongOpt)
                .create();
        options.addOption(priorityOption);
    }

      private void addOutputOptions(Options options) {
        //output options
        options.addOption(new Option(null, NUM_GENES_OPTION.getLongOption(), true, "Number of genes to show in output"));
        options.addOption(new Option("o", OUT_FILE_OPTION.getLongOption(), true, "name of out file. Will default to vcf-filename-exomiser-results.html"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("type")
                .withType(OutputFormat.class)
                .withValueSeparator()
                .withDescription("Specify format option HTML, VCF or TAB. Defaults to HTML if not specified. e.g. --out-format=TAB")
                .withLongOpt(OUT_FORMAT_OPTION.getLongOption())
                .create("f"));

        //TODO: check what this actually does (I think this is for Peter's CRE server, in which case it's not wanted here )
        options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));
    }

        /**
     * There is a lot of messing about needed to get the Prioritiser option
     * description sorted, but this will now automatically change to reflect
     * changes in any names or types which are added to the
     * {@link de.charite.compbio.exomiser.priority.PriorityType}
     *
     * @param prioritiserLongOpt
     * @return
     */
    private String buildPrioritiserDescription(String prioritiserLongOpt) {
        List<PriorityType> inValidPriorityTypes = new ArrayList<>();
        inValidPriorityTypes.add(PriorityType.NOT_SET);
        inValidPriorityTypes.add(PriorityType.INHERITANCE_MODE_PRIORITY);
        inValidPriorityTypes.add(PriorityType.OMIM_PRIORITY);

        List<PriorityType> validPriorityTypes = new ArrayList<>();
        //The last PriorityType is PriorityType.NOT_SET which has no command-line option so we ned to create a list of PriorityTypes without this one in.
        for (PriorityType priorityType : PriorityType.values()) {
            if (inValidPriorityTypes.contains(priorityType)) {
                //we're not interested in this option
            } else if (priorityType.getCommandLineValue().isEmpty()) {
                //we're not interested in this option either
            } else {
                //This is the option we're looking for!
                validPriorityTypes.add(priorityType);
            }
        }
        //now we've got the valid list of types, build up the description
        //this should look like this:
        //"Name of the PRIORITISER_OPTION used to score the genes.
        // Can be one of: inheritance-mode, phenomizer or dynamic-phenodigm. 
        // e.g. --PRIORITISER_OPTION=dynamic-phenodigm"

        StringBuilder priorityOptionDescriptionBuilder = new StringBuilder("Name of the prioritiser used to score the genes. Can be one of: ");

        int numPriorityTypes = validPriorityTypes.size();
        int lastType = numPriorityTypes - 1;
        int secondLastType = numPriorityTypes - 2;
        for (int i = 0; i < numPriorityTypes; i++) {
            PriorityType priorityType = validPriorityTypes.get(i);
            if (i == lastType) {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue())
                        .append(". e.g. --").append(prioritiserLongOpt)
                        .append("=")
                        .append(priorityType.getCommandLineValue());
            } else if (i == secondLastType) {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue()).append(" or ");
            } else {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue()).append(", ");
            }
        }

        return priorityOptionDescriptionBuilder.toString();
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
