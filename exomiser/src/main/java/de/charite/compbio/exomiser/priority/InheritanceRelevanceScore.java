package de.charite.compbio.exomiser.priority;



import java.util.List;



/**
 * This filter checks whether the variants are present in a fashion that is compatible with 
 * autosomal dominant, recessive or X chromosomal.
 * <P>
 *Note: This class was renamed from InheritanceTriage on 11. February 2013
 * @author Peter N Robinson
 * @version 0.04 (April 6, 2013).
 */
public class InheritanceRelevanceScore implements IRelevanceScore {
    /** This is set to true of the variant, possibly together with other variants of this gene, matches the
     * inheritance pattern (AR,AD,X). Note that because of the difficulty in calling hemizygous vs. homozygous
     * calls on the X chromosome, we do not distinguish between X recessive/dominant inheritance. */
    boolean matches_inheritance_pattern=false;

    /** @return true if the variant being analyzed passes the filter (e.g., is rare, pathogenic, or has high quality reads) */
    public boolean passesFilter() { return matches_inheritance_pattern; }
    /** @return return a float representation of the filter result [0..1]. If the result is boolean, return 0.0 for false and 1.0 for true */
    @Override public float getRelevanceScore() {if (matches_inheritance_pattern) return 1f; else return 0f; }
    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() { return null; }
    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public List<String> getFilterResultList()
	{ return null; }

     @Override
    public String getHTMLCode() {
	return "TODO";
    }

     @Override public void resetRelevanceScore(float newscore){ /* not implemented */ }
}