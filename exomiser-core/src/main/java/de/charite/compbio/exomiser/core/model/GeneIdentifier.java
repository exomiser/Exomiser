/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneIdentifier extends ExternalIdentifier implements Comparable<GeneIdentifier>{
    
    private final String geneSymbol;
    
    public GeneIdentifier(String geneSymbol, String databaseCode, String databaseAcc) {
        super(databaseCode, databaseAcc);
        if (geneSymbol == null || geneSymbol.isEmpty()) {
            geneSymbol = "UNKNOWN";
        }
        this.geneSymbol = geneSymbol;
    }

    public GeneIdentifier(String geneSymbol, String compoundIdentifier) {
        super(compoundIdentifier);
        if (geneSymbol == null || geneSymbol.isEmpty()) {
            geneSymbol = "UNKNOWN";
        }
        this.geneSymbol = geneSymbol;   

    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    /**
     *
     * @param other
     * @return
     */

    @Override
    public int compareTo(GeneIdentifier other) {
        return this.geneSymbol.compareTo(other.geneSymbol);
    }

    @Override
    public String toString() {
        return geneSymbol + "{" + super.getCompoundIdentifier() + '}';
    }
}
