package de.charite.compbio.exomiser.core.prioritisers;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {
   
    //Prioritises against PPI-RandomWalk-proximity and dynamic human, mouse and fish phenotypes
    HIPHIVE_PRIORITY(ScoringMode.RAW_SCORE),
    //Prioritises against PPI-RandomWalk-proximity A.K.A "GeneWanderer"
    EXOMEWALKER_PRIORITY(ScoringMode.RAW_SCORE),
    //Prioritises against human phenotypes A.K.A. "HPO Phenomizer prioritizer"
    PHENIX_PRIORITY(ScoringMode.RAW_SCORE),
    //Prioritises against human-mouse phenotype similarities
    PHIVE_PRIORITY(ScoringMode.RAW_SCORE),
    //Prioritises against OMIM data
    OMIM_PRIORITY(ScoringMode.RAW_SCORE),
    //Prioritises  against phenotype data (Uberpheno) A.K.A. "Uberpheno semantic similarity filter"
    UBERPHENO_PRIORITY(ScoringMode.RAW_SCORE),
    //None - for when you don't want to run any prioritisation
    NONE(ScoringMode.RAW_SCORE);
        
    /**
     * The string representation of the FilterType as used when specifying the type on the command-line.
     */
    private final ScoringMode scoringMode;
    
    private PriorityType(ScoringMode scoringMode) {
        this.scoringMode = scoringMode;
    }
    
    public ScoringMode getScoringMode() {
        return scoringMode;
    }
       
}
