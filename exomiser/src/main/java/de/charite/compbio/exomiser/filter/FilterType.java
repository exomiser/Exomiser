package de.charite.compbio.exomiser.filter;

/**
 * This is a simple class of enumerated constants that describe the type of
 * filtering that was applied to a Gene/Variant. This class is placed in the
 * jannovar hierarchy for now because it is intertwined with Variant.
 *
 * @author Peter Robinson
 * @version 0.05 (10 January, 2014)
 */
public enum FilterType {
    
    /**
     * Flag for output of field representing the Quality filter for the VCF
     * entry
     */
    QUALITY_FILTER("min-qual"),
    /**
     * Flag for filter type "interval"
     */
    INTERVAL_FILTER("restrict-interval"),
    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    PATHOGENICITY_FILTER("include-pathogenic"),
    /**
     * Flag to output results of filtering against frequency with Thousand
     * Genomes and ESP data.
     */
    FREQUENCY_FILTER("max-freq"),
    /**
     * Flag to represent results of filtering against phenotype data (HPO)
     */
    HPO_FILTER("hpo"),
    /**
     * Flag to represent target filter
     */
    TARGET_FILTER("remove-off-target-syn"),
    /**
     * Filter for target regions in a BED file
     */
    BED_FILTER("bed");

    /**
     * The string representation of the FilterType as used when specifying the type on the command-line.
     */
    private final String commandLineValue;
    
    private FilterType(String commandLineValue) {
        this.commandLineValue = commandLineValue;
    }
    
    public String getCommandLineValue() {
        return commandLineValue;
    }
    /**
     * Returns the type of Filter/Priority for the 
     * @param value
     * @return 
     */
    public static FilterType valueOfCommandLine(String value) {
        for (FilterType filterType : values()) {
            if (filterType.commandLineValue.equals(value)) {
                return filterType;
            }
        }
        return FilterType.FREQUENCY_FILTER;
    }
}
