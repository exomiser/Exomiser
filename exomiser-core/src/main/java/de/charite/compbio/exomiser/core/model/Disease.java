/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.List;

/**
 * Disease bean representing a genetic disease.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Disease implements Comparable<Disease>{
    
    private DiseaseIdentifier diseaseIdentifier;
    private String term;
    private List<PhenotypeTerm> phenotypeTerms;
    
    public Disease() {
    }
    
    /**
     * Convenience constructor - will create a new Disease with a new DiseaseIdentifier
     * being made from the provided diseaseId.
     * 
     * @param diseaseId 
     */
    public Disease(String diseaseId) {
        this.diseaseIdentifier = new DiseaseIdentifier(diseaseId);
    }
    
    public Disease(DiseaseIdentifier diseaseIdentifier) {
        this.diseaseIdentifier = diseaseIdentifier;
    }
    
    public String getDiseaseId() {
        return diseaseIdentifier.getCompoundIdentifier();
    }

    public DiseaseIdentifier getDiseaseIdentifier() {
        return diseaseIdentifier;
    }
    
    public void setDiseaseIdentifier(DiseaseIdentifier diseaseIdentifier) {
        this.diseaseIdentifier = diseaseIdentifier;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<PhenotypeTerm> getPhenotypeTerms() {
        return phenotypeTerms;
    }

    public void setPhenotypeTerms(List<PhenotypeTerm> phenotypeTerms) {
        this.phenotypeTerms = phenotypeTerms;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.diseaseIdentifier != null ? this.diseaseIdentifier.hashCode() : 0);
        hash = 53 * hash + (this.term != null ? this.term.hashCode() : 0);
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
        final Disease other = (Disease) obj;
        if (this.diseaseIdentifier != other.diseaseIdentifier && (this.diseaseIdentifier == null || !this.diseaseIdentifier.equals(other.diseaseIdentifier))) {
            return false;
        }
        if ((this.term == null) ? (other.term != null) : !this.term.equals(other.term)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int compareTo(Disease t) {
        return this.diseaseIdentifier.compareTo(t.diseaseIdentifier);
    }
    
    @Override
    public String toString() {
        return "Disease{" + diseaseIdentifier + " - " + term + "}";
    }    
    
}
