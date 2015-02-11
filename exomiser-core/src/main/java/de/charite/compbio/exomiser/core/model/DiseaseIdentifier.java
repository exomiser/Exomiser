/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseIdentifier extends ExternalIdentifier implements Comparable<DiseaseIdentifier> {
  
    public DiseaseIdentifier(String compoundIdentifier) {
        super(compoundIdentifier);
    }

    public DiseaseIdentifier(String databaseCode, String databaseAcc) {
        super(databaseCode, databaseAcc);
    }  
    
    /**
     * Sorts the ExternalIdentifier by databaseCode and then by databaseIdentifier
     * @param other
     * @return 
     */
    @Override
    public int compareTo(DiseaseIdentifier other) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
    
        if (this == other) {
            return EQUAL;
        }
        
        if (this.getDatabaseCode().equals(other.getDatabaseCode())) {
            return (this.getDatabaseAcc().compareTo(other.getDatabaseAcc()));
        }
        
        return this.getDatabaseCode().compareTo(other.getDatabaseCode());
    }

}
