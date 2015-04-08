package de.charite.compbio.exomiser.core.prioritisers.util;

import java.util.Set;
import java.util.HashSet;

import ontologizer.go.Term;

/**
 * Annotation relation between a gene and a term.
 *
 * @author sebastiankohler
 *
 */
public class UberphenoAnnotation {

    /**
     * The annotated term from uberpheno.
     */
    private Term term;

    /**
     * Which OMIMs are responsible for making this annotation
     */
    private Set<Integer> evidenceOmimIds = new HashSet<>();
    /**
     * The annotated gene's symbol.
     */
    private String geneSymbol;
    /**
     * The annotated gene's entrez id.
     */
    private int entrezGeneId;

    /**
     * Construct a new annotation relation bet
     *
     * @param entrez
     * @param geneSymbol
     * @param term
     * @param evidence
     */
    public UberphenoAnnotation(int entrez, String geneSymbol, Term term, Set<Integer> evidence) {
        this.entrezGeneId = entrez;
        this.geneSymbol = geneSymbol;
        this.term = term;
        this.evidenceOmimIds = evidence;
    }

    /**
     * @return The annotated term from the uberpheno
     */
    public Term getTerm() {
        return term;
    }

    /**
     * @return The entrez-id of the annotated gene.
     */
    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    /**
     * @return The OMIM-IDs responsible for this annotation.
     */
    public Set<Integer> getEvidenceOmimIds() {
        return evidenceOmimIds;
    }

    /**
     * @return The symbol of the annotated gene.
     */
    public String getGeneSymbol() {
        return geneSymbol;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return geneSymbol + "(" + entrezGeneId + "): " + term + " (evidence: " + evidenceOmimIds + ")";
    }
}
