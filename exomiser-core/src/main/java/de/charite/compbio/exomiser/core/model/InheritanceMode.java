/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

/**
 * Enum representing the different modes on inheritance for a disease.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum InheritanceMode {
    
    RECESSIVE("HP:0000007", "R"),
    DOMINANT("HP:0000006", "D"),
    //HP:0000005 is the root inheritance term - 'Mode of inheritance'. So not 
    //really unknown, but vague enough. 
    UNKNOWN("HP:0000005", "U"),
    X_LINKED("HP:0001417", "X"),
    X_RECESSIVE("HP:0001419", "X"),
    X_DOMINANT("HP:0001423", "X"),
    DOMINANT_AND_RECESSIVE("-", "B"),
//    MULTIFACTORIAL("HP:0001426"),
//    SPORADIC("HP:0003745"),
    SOMATIC("HP:0001428", "S"),
    MITOCHONDRIAL("HP:0001427", "M"),
    POLYGENIC("HP:0010982", "P"),
    Y_LINKED("HP:0001450", "Y");
    
    
    private final String hpoTerm;
    //short form letter code for the inheritance mode
    private final String inheritanceCode;
    
    private InheritanceMode(String hpoTerm, String inheritanceCode) {
        this.hpoTerm = hpoTerm;
        this.inheritanceCode = inheritanceCode;
    }
        
    public String getHpoTerm() {
        return hpoTerm;
    }
    
    public String getInheritanceCode() {
        return inheritanceCode;
    }
    
    /**
     * Returns the InheritanceMode for a given inheritanceCode. Will return UNKNOWN
     * as a default. This is currently lossy as X could refer to either X-linked
     * dominant, recessive or uncharacterised.
     * @param inheritanceCode
     * @return 
     */
    public static InheritanceMode valueOfInheritanceCode(String inheritanceCode) {
        for (InheritanceMode inheritanceMode : InheritanceMode.values()) {
            if (inheritanceMode.inheritanceCode.equals(inheritanceCode)) {
                return inheritanceMode;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Returns the InheritanceMode for a given HPO term. Will return UNKNOWN
     * as a default.
     * @param hpoTerm
     * @return 
     */
    public static InheritanceMode valueOfHpoTerm(String hpoTerm) {
        for (InheritanceMode inheritanceMode : InheritanceMode.values()) {
            if (inheritanceMode.hpoTerm.equals(hpoTerm)) {
                return inheritanceMode;
            }
        }
        return UNKNOWN;
    }
}
