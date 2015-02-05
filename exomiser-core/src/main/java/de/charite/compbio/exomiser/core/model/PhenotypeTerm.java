/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * Represents a phenotype term from a phenotype ontology - e.g. the HPO, MPO, ZPO... 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeTerm {
    
    private final String id;
    private final String term;

    public PhenotypeTerm(String id, String term) {
        this.id = id;
        this.term = term;
    }
    
    public String getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 37 * hash + (this.term != null ? this.term.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhenotypeTerm other = (PhenotypeTerm) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.term == null) ? (other.term != null) : !this.term.equals(other.term)) {
            return false;
        }
        return true;
    }
    

    @Override
    public String toString() {
        return id + " " + term;
    }
      
}
