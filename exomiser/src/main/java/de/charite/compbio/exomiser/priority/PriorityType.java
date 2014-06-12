package de.charite.compbio.exomiser.priority;

/**
 *
 * @author Jules Jacobse <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {
   
    /**
     * Flag to represent results of filtering against an inheritance pattern.
     *//**
     * Flag to represent results of filtering against an inheritance pattern.
     */
    INHERITANCE_MODE_PRIORITY("inheritance-mode"),
    /**
     * Flag to represent results of filtering against MGI phenotype data (Phenodigm)
     */
    PHENODIGM_MGI_PRIORITY("phenodigm-mgi"),
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
    DYNAMIC_PHENODIGM_PRIORITY("dynamic-phenodigm"),

    /**
     * Not set type - default for when things go wrong.
     */
    NOT_SET("");
    
    /**
     * The string representation of the FilterType as used when specifying the type on the command-line.
     */
    private final String commandLineValue;
    
    private PriorityType(String commandLineValue) {
        this.commandLineValue = commandLineValue;
    }
    
    public String getCommandLineValue() {
        return commandLineValue;
    }
    /**
     * Returns the type of Priority for the 
     * @param value
     * @return 
     */
    public static PriorityType valueOfCommandLine(String value) {
        for (PriorityType priorityType : values()) {
            if (priorityType.commandLineValue.equals(value)) {
                return priorityType;
            }
        }
        return PriorityType.NOT_SET;
    }
}
