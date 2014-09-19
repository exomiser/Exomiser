package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;

/**
 * This interface is implemented by classes that perform filtering of the
 * <b>variants</b> in the VCF file according to various criteria. A
 * {@code FilterResult} gets returned for each {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Peter N Robinson
 * @version 0.07 (April 28, 2013).
 */
public interface VariantFilter extends Filter<VariantEvaluation> {

    /**
     * Returns a {@code FilterResult} indicating whether the
     * {@code VariantEvaluation} passed or failed the {@code Filter}.
     *
     * @param variantEvaluation to be filtered
     */
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation);

}
