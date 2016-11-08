package org.monarchinitiative.exomiser.core.prioritisers;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {
   
    //Prioritises against PPI-RandomWalk-proximity and dynamic human, mouse and fish phenotypes
    HIPHIVE_PRIORITY,
    //Prioritises against PPI-RandomWalk-proximity A.K.A "GeneWanderer"
    EXOMEWALKER_PRIORITY,
    //Prioritises against human phenotypes A.K.A. "HPO Phenomizer prioritizer"
    PHENIX_PRIORITY,
    //Prioritises against human-mouse phenotype similarities
    PHIVE_PRIORITY,
    //Prioritises against OMIM data
    OMIM_PRIORITY,
    //Prioritises  against phenotype data (Uberpheno) A.K.A. "Uberpheno semantic similarity filter"
    UBERPHENO_PRIORITY,
    //None - for when you don't want to run any prioritisation
    NONE

}
