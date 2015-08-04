package de.charite.compbio.exomiser.core.filters;

/**
 * A FilterResult object gets attached to each Variant object as a result of the
 * filtering of the variants in the VCF file according to various criteria.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface FilterResult {

    public FilterType getFilterType();

    public FilterResultStatus getResultStatus();
    
    public boolean passedFilter();

}
