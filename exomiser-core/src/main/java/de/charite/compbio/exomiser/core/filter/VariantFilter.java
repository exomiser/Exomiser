package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Filterable;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.List;

/**
 * This interface is implemented by classes that perform filtering of the
 * <b>variants</b> in the VCF file according to various criteria. A Triage
 * object gets attached to each Variant object.
 * <P>
 * Note that classes that implement the interface
 * {@link de.charite.compbio.exomiser.priority.Priority Priority} are
 * responsible for gene-level filtering.
 *
 * @author Peter N Robinson
 * @version 0.07 (April 28, 2013).
 * @see de.charite.compbio.exomiser.priority.Priority
 */
public interface VariantFilter extends Filter<VariantEvaluation> {

    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter it is marked as having failed that
     * filter.
     *
     * @param variantEvaluations to be filtered
     */
    @Override
    public void filter(List<VariantEvaluation> variantEvaluations);

    /**
     * True or false depending on whether the {@code VariantEvaluation} passes
     * the filter or not.
     *
     * @param variantEvaluation to be filtered
     * @return true if the {@code VariantEvaluation} passes the filter.
     */
    @Override
    public boolean filter(VariantEvaluation variantEvaluation);

}
