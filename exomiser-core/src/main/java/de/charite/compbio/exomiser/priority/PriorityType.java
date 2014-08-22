package de.charite.compbio.exomiser.priority;

/**
 *
 * @author Jules Jacobse <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {
   
    /**
     * Flag to represent results of filtering against an inheritance pattern.
     */
    INHERITANCE_MODE_PRIORITY("inheritance-mode", ScoringMode.RAW_SCORE),
    /**
     * Flag to represent results of filtering against phenotype data (Uberpheno)
     */
    UBERPHENO_PRIORITY("uber-pheno", ScoringMode.RAW_SCORE),
    /**
     * Flag to represent results of filtering against PPI-RandomWalk-proximity
     */
    EXOMEWALKER_PRIORITY("exomewalker", ScoringMode.RAW_SCORE),
    /**
     * Flag to represent results of filtering against PPI-RandomWalk-proximity
     * and dynamic human and mouse phenotypes
     */

    EXOMISER_ALLSPECIES_PRIORITY("exomiser-allspecies", ScoringMode.RAW_SCORE),

    /**
     * Flag to represent results of annotating against OMIM data
     */
    OMIM_PRIORITY("omim", ScoringMode.RAW_SCORE),
    /**
     * Flag for BOQA prioritizer.
     */
    BOQA_PRIORITY("boqa", ScoringMode.RAW_SCORE),
    /**
     * Flag for phenomizer prioritizer
     */
    PHENIX_PRIORITY("phenix", ScoringMode.RAW_SCORE),
    /**
     * Flag for dynamic phenodigm filter.
     */
    EXOMISER_MOUSE_PRIORITY("exomiser-mouse", ScoringMode.RAW_SCORE),
    
    /**
     * Not set type - default for when things go wrong.
     */
    NOT_SET("", ScoringMode.RAW_SCORE);
    
    /**
     * The string representation of the FilterType as used when specifying the type on the command-line.
     */
    private final String commandLineValue;
    private final ScoringMode scoringMode;
    
    private PriorityType(String commandLineValue, ScoringMode scoringMode) {
        this.commandLineValue = commandLineValue;
        this.scoringMode = scoringMode;
    }
    
    public String getCommandLineValue() {
        return commandLineValue;
    }

    public ScoringMode getScoringMode() {
        return scoringMode;
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

    @Override
    public String toString() {
        return commandLineValue;
    } 
    
}
