/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

/**
 * Enum to maintain coherence between the creation and parsing of command-line
 * options. 
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum CommandLineOption {

    SETTINGS_FILE_OPTION("settings-file"),
    VCF_OPTION("vcf"),
    PRIORITISER_OPTION("prioritiser"), //values for this are handled by PriorityType
    PED_OPTION("ped"),
    //FILTER OPTIONS
    REMOVE_DBSNP_OPTION("remove-dbsnp"),
    //other values for this are handled by FilterType

    //PRIORITISER OPTIONS
    CANDIDATE_GENE_OPTION("candidate-gene"),
    HPO_IDS_OPTION("hpo-ids"),
    SEED_GENES_OPTION("seed-genes"),
    DISEASE_ID_OPTION("disease-id"),
    INHERITANCE_MODE_OPTION("inheritance-mode"),
    //OUTPUT OPTIONS
    NUM_GENES_OPTION("num-genes"),
    OUT_FILE_OPTION("out-file"),
    OUT_FORMAT_OPTION("out-format"),
    HELP_OPTION("help");
    /**
     * The string representation of the FilterType as used when specifying the
     * type on the command-line.
     */
    private final String longOption;

    private CommandLineOption(String longOption) {
        this.longOption = longOption;
    }

    public String getLongOption() {
        return longOption;
    }

    /**
     *
     * @param value
     * @return
     */
    public static CommandLineOption valueOfLongOption(String value) {
        for (CommandLineOption type : values()) {
            if (type.longOption.equals(value)) {
                return type;
            }
        }
        return CommandLineOption.HELP_OPTION;
    }
}
