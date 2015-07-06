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

    QUALITY_FILTER,
    INTERVAL_FILTER,
    ENTREZ_GENE_ID_FILTER,
    PATHOGENICITY_FILTER,
    CADD_FILTER,
    FREQUENCY_FILTER,
    REGULATORY_FEATURE_FILTER,
    TARGET_FILTER,
    INHERITANCE_FILTER,
    BED_FILTER, 
    FREQUENCY_FILTER,
    KNOWN_VARIANT_FILTER,
    VARIANT_EFFECT_FILTER,
    INHERITANCE_FILTER,
    BED_FILTER, 
    PRIORITY_SCORE_FILTER;

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
            case CADD_FILTER:
                return "CADD";    
            case KNOWN_VARIANT_FILTER:
                return "Known variant";
            case FREQUENCY_FILTER:
                return "Frequency";
            case VARIANT_EFFECT_FILTER:
                return "Target"; //Exome target region
            case REGULATORY_FEATURE_FILTER:
                return "Regulatory Feature";    
            case INHERITANCE_FILTER:
                return "Inheritance";
            case BED_FILTER:
                return "Gene panel target region (Bed filter)";
            case PRIORITY_SCORE_FILTER:
                return "Gene priority score";
            default:
                return "Unidentified Filter";
        }
    }
}
