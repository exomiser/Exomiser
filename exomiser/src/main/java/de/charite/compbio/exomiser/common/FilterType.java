package de.charite.compbio.exomiser.common;

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
    QUALITY_FILTER(""),
    /**
     * Flag for filter type "interval"
     */
    INTERVAL_FILTER(""),
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
    HPO_FILTER(""),
    /**
     * Flag to represent exome-target filter
     */
    EXOME_TARGET_FILTER("target"),
    /**
     * Filter for target regions in a BED file
     */
    BED_FILTER("bed"),
    /**
     * Flag to represent results of filtering against an inheritance pattern.
     */
    INHERITANCE_MODE_PRIORITY("inheritance-mode"),
    /**
     * Flag to represent results of filtering against MGI phenotype data (Phenodigm)
     */
    PHENODIGM_MGI_PRIORITY("phenodigm-mgi"),
    /**
     * Flag to represent results of filtering against ZFIN phenotype data (Phenodigm)
     */
    PHENODIGM_ZFIN_PRIORITY("phenodigm-zfin"),
    /**
     * Flag to represent results of filtering against phenotype data (Uberpheno)
     */
    UBERPHENO_PRIORITY("uber-pheno"),
    /**
     * Flag to represent results of filtering against PPI-RandomWalk-proximity
     */
    GENEWANDERER_PRIORITY("gene-wanderer"),
    /**
     * Flag to represent results of filtering against PPI-RandomWalk-proximity
     * and human and mouse phenotypes
     */
    PHENOWANDERER_PRIORITY("pheno-wanderer"),
    /**
     * Flag to represent results of filtering against PPI-RandomWalk-proximity
     * and dynamic human and mouse phenotypes
     */
    DYNAMIC_PHENOWANDERER_PRIORITY("dynamic-pheno-wanderer"),
    /**
     * Flag to represent results of annotating against OMIM data
     */
    OMIM_PRIORITY("omim"),
    /**
     * Flag for BOQA prioritizer.
     */
    BOQA_PRIORITY("boqa"),
    /**
     * Flag for phenomizer prioritizer
     */
    PHENOMIZER_PRIORITY("phenomizer"),
    /**
     * Flag for dynamic phenodigm filter.
     */
    DYNAMIC_PHENODIGM_FILTER("");

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
        return FilterType.PHENODIGM_MGI_PRIORITY;
    }
}
