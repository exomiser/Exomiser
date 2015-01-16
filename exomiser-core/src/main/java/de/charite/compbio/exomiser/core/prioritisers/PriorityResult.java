package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.filters.Score;
import java.util.List;

/**
 * Prioritization of Genes results in a relevance score for each tested
 * {@link exomizer.exome.Gene Gene} object. The methods may also annotate the
 genes with data (e.g., a link to OMIM or a link to Phenodigm or uberpheno
 data. Each prioritization is expected to result on an object of a class that
 implements PriorityResult
 *
 * @author Peter N Robinson
 */
public interface PriorityResult extends Score {
    
    public PriorityType getPriorityType();

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
