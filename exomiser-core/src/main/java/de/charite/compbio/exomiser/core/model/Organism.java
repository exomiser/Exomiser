/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * Enum representing the model organism present in the Exomiser database for which
 * there are phenotype mappings. These are few and should not be added to
 * frequently.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum Organism {

    //Not sure I really like this as it's not exactly flexible and there are alot of different species out there.
    HUMAN("9606", "Homo sapiens"),
    MOUSE("10090", "Mus musculus"),
    FISH("7955", "Danio rerio");
    
    private final String ncbiId;
    private final String speciesName;
    
    private Organism(String ncbiId, String speciesName) {
        this.ncbiId = ncbiId;
        this.speciesName = speciesName;
    }

    public String getNcbiId() {
        return ncbiId;
    }

    public String getSpeciesName() {
        return speciesName;
    }
    
    
}
