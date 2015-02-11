package de.charite.compbio.exomiser.core.filters;

/**
 * This is a simple class of enumerated constants that describe the type of
 * filtering that was applied to a Gene/Variant. This class is placed in the
 * jannovar hierarchy for now because it is intertwined with Variant.
 *
 * @author Peter Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.05 (10 January, 2014)
 */
public enum FilterType {

    /**
     * Flag for output of field representing the Quality filter for the VCF
     * entry
     *//**
     * Flag for output of field representing the Quality filter for the VCF
     * entry
     */
    QUALITY_FILTER,
    /**
     * Flag for filter type "interval"
     */
    INTERVAL_FILTER,
    /**
     * Flag to represent filtering by a user entered set of genes
     */
    ENTREZ_GENE_ID_FILTER,
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
    INHERITANCE_FILTER,
    /**
     * Filter for target regions in a BED file
     */
    BED_FILTER;

    @Override
    public String toString() {
        switch (this) {
            case ENTREZ_GENE_ID_FILTER:
                return "Genes to keep";
            case QUALITY_FILTER:
                return "Quality";
            case INTERVAL_FILTER:
                return "Interval";
            case PATHOGENICITY_FILTER:
                return "Pathogenicity";
            case FREQUENCY_FILTER:
                return "Frequency";
            case TARGET_FILTER:
                return "Target"; //Exome target region
            case INHERITANCE_FILTER:
                return "Inheritance";
            case BED_FILTER:
                return "Gene panel target region (Bed filter)";
        }
        return "Unidentified Filter";
    }
}
