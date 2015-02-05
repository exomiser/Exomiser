/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * Contains information about how well a pair of <code>PhenotypeTerm</code> 
 * match each other.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeMatch {
    //information content
    private double ic;
    //Jaccard similarity score
    private double simJ;
    //lowest common subsumer
    private String lcs;
    
    private PhenotypeTerm mousePhenotype;
    private PhenotypeTerm humanPhenotype;

    public PhenotypeMatch() {
    }

    public double getIc() {
        return ic;
    }

    public void setIc(double ic) {
        this.ic = ic;
    }

    public double getSimJ() {
        return simJ;
    }

    public void setSimJ(double simJ) {
        this.simJ = simJ;
    }

    public String getLcs() {
        return lcs;
    }

    public void setLcs(String lcs) {
        this.lcs = lcs;
    }

    public PhenotypeTerm getMousePhenotype() {
        return mousePhenotype;
    }

    public void setMousePhenotype(PhenotypeTerm mousePhenotype) {
        this.mousePhenotype = mousePhenotype;
    }

    public PhenotypeTerm getHumanPhenotype() {
        return humanPhenotype;
    }

    public void setHumanPhenotype(PhenotypeTerm humanPhenotype) {
        this.humanPhenotype = humanPhenotype;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.ic) ^ (Double.doubleToLongBits(this.ic) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.simJ) ^ (Double.doubleToLongBits(this.simJ) >>> 32));
        hash = 59 * hash + (this.mousePhenotype != null ? this.mousePhenotype.hashCode() : 0);
        hash = 59 * hash + (this.humanPhenotype != null ? this.humanPhenotype.hashCode() : 0);
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
        final PhenotypeMatch other = (PhenotypeMatch) obj;
        if (Double.doubleToLongBits(this.ic) != Double.doubleToLongBits(other.ic)) {
            return false;
        }
        if (Double.doubleToLongBits(this.simJ) != Double.doubleToLongBits(other.simJ)) {
            return false;
        }
        if (this.mousePhenotype != other.mousePhenotype && (this.mousePhenotype == null || !this.mousePhenotype.equals(other.mousePhenotype))) {
            return false;
        }
        if (this.humanPhenotype != other.humanPhenotype && (this.humanPhenotype == null || !this.humanPhenotype.equals(other.humanPhenotype))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhenotypeMatch{" + "ic=" + ic + ", simJ=" + simJ + ", mousePhenotype=" + mousePhenotype.getId() + ", humanPhenotype=" + humanPhenotype.getId() + '}';
    }
    
}
