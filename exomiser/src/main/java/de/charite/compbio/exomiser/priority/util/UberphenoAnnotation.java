package de.charite.compbio.exomiser.priority.util;

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
	private HashSet<Integer> evidenceOmimIds = new HashSet<Integer>();
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
	 * @param symb
	 * @param t
	 * @param ev
	 */
	public UberphenoAnnotation(int entrez, String symb, Term t, HashSet<Integer> ev) {
		entrezGeneId 	= entrez;
		geneSymbol 		= symb;
		term 			= t;
		evidenceOmimIds = ev;
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
	public HashSet<Integer> getEvidenceOmimIds() {
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
		return geneSymbol+ "("+entrezGeneId+"): "+term+" (evidence: "+evidenceOmimIds+")";
	}
}
