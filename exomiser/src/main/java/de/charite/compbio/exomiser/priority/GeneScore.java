package de.charite.compbio.exomiser.priority;

import java.util.List;

/**
 * Prioritization of Genes results in a relevance score for each tested
 * {@link exomizer.exome.Gene Gene} object. The methods may also annotate the
 * genes with data (e.g., a link to OMIM or a link to Phenodigm or uberpheno
 * data. Each prioritization is expected to result on an object of a class that
 * implements GeneScore
 *
 * @author Peter N Robinson
 * @version 0.04 (6 January, 2014)
 * @see exomizer.filter.Triage
 */
public interface GeneScore {

    /**
     * @return a numerical value representing the relevance of the gene. Should
     * be between zero (no relevance) and an arbitrary real number (not
     * necessarily 1.0f).
     */
    public float getScore();

    /**
     * Some of the prioritizers need to renormalize the score after they have
     * gotten a score for all genes, and can use this method to do so,
     *
     * @param newscore The renormalized score
     */
    public void resetScore(float newscore);

    /**
     * @return A string with a summary of the filtering results .
     */
    public String getFilterResultSummary();

    /**
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired.
     */
    public List<String> getFilterResultList();

    /**
     * @return HTML code representing this prioritization/relevance score
     * @deprecated this should be handled by the writers
     */
    @Deprecated
    public String getHTMLCode();

}
