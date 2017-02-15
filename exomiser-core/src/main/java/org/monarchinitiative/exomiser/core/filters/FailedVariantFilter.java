package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

/**
 * Fails variants which do not have 'PASS' or '.' in the VCF FILTER field.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FailedVariantFilter implements VariantFilter {

    private static final FilterType FILTER_TYPE = FilterType.FAILED_VARIANT_FILTER;

    private static final FilterResult PASS = FilterResult.pass(FILTER_TYPE);
    private static final FilterResult FAIL = FilterResult.fail(FILTER_TYPE);

    private enum VariantContextFilterState {
        UNFILTERED, PASSED, FAILED
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        VariantContextFilterState vcfFilterState = determineVariantContextState(variantEvaluation.getVariantContext());
        if (vcfFilterState == VariantContextFilterState.PASSED || vcfFilterState == VariantContextFilterState.UNFILTERED) {
            return PASS;
        }
        return FAIL;
    }

    private VariantContextFilterState determineVariantContextState(VariantContext variantContext) {
        if (variantContext.filtersWereApplied()) {
            if (variantContext.isNotFiltered()) {
                return VariantContextFilterState.PASSED;
            } else {
                return VariantContextFilterState.FAILED;
            }
        }
        return VariantContextFilterState.UNFILTERED;
    }

    @Override
    public FilterType getFilterType() {
        return FILTER_TYPE;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "FailedVariantFilter{}";
    }
}
