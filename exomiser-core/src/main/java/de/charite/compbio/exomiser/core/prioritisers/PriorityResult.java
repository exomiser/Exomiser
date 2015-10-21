package de.charite.compbio.exomiser.core.prioritisers;

/**
 * Prioritization of Genes results in a relevance score for each tested
 * {@link exomizer.exome.Gene Gene} object. The methods may also annotate the
 genes with data (e.g., a link to OMIM or a link to Phenodigm or uberpheno
 data. Each prioritization is expected to result on an object of a class that
 implements PriorityResult
 *
 * @author Peter N Robinson
 */
public interface PriorityResult {
    
    /**
     * @return return a float representation of the filter result [0..1]. If the
     * result is boolean, return 0.0 for false and 1.0 for true
     */
    public float getScore();
    
    public PriorityType getPriorityType();

    /**
     * @return HTML code representing this prioritization/relevance score
     * @deprecated this should be handled by the writers
     */
    @Deprecated
    public String getHTMLCode();

}
