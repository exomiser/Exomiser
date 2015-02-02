package de.charite.compbio.exomiser.core.prioritisers;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {
   
    //Prioritises against PPI-RandomWalk-proximity and dynamic human, mouse and fish phenotypes
    EXOMISER_ALLSPECIES_PRIORITY("hiphive", ScoringMode.RAW_SCORE),

    //Prioritises against PPI-RandomWalk-proximity
    EXOMEWALKER_PRIORITY("exomewalker", ScoringMode.RAW_SCORE),
    
    //Prioritises against human phenotypes
    PHENIX_PRIORITY("phenix", ScoringMode.RAW_SCORE),

    //Prioritises against human-mouse phenotype similarities
    EXOMISER_MOUSE_PRIORITY("phive", ScoringMode.RAW_SCORE),

    //Prioritises against OMIM data
    OMIM_PRIORITY("omim", ScoringMode.RAW_SCORE),
    
    //Prioritises  against phenotype data (Uberpheno)
    UBERPHENO_PRIORITY("uber-pheno", ScoringMode.RAW_SCORE),

    //Not set type - default for when things go wrong.
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
