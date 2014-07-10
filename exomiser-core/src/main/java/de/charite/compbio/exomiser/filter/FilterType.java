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
    QUALITY_FILTER,
    /**
     * Flag for filter type "interval"
     */
    INTERVAL_FILTER,
    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    PATHOGENICITY_FILTER,
    /**
     * Flag to output results of filtering against frequency with Thousand
     * Genomes and ESP data.
     */
    FREQUENCY_FILTER,
    /**
     * Flag to represent target filter
     */
    TARGET_FILTER,
    /**
     * Filter for target regions in a BED file
     */
    BED_FILTER;

}
