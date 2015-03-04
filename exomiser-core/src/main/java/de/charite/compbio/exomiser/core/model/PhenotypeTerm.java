/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.Objects;

/**
 * Represents a phenotype term from a phenotype ontology - e.g. the HPO, MPO, ZPO... 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeTerm {
    
    private final String id;
    private final String term;
    private final double ic;

    public PhenotypeTerm(String id, String term, double ic) {
        this.id = id;
        this.term = term;
        this.ic = ic;
    }
    
    public String getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public double getIc() {
        return ic;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.term);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.ic) ^ (Double.doubleToLongBits(this.ic) >>> 32));
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.term, other.term)) {
            return false;
        }
        return Double.doubleToLongBits(this.ic) == Double.doubleToLongBits(other.ic);
    }

    @Override
    public String toString() {
        return "PhenotypeTerm{" + "id=" + id + ", term=" + term + ", ic=" + ic + '}';
    }
      
}
