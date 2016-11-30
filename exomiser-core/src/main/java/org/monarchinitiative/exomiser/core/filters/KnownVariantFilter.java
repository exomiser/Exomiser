/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Objects;

/**
 * Filter for removing variants which have been characterised in a database.
 * This includes having an RSID assigned or having any frequency data.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class KnownVariantFilter implements VariantFilter {

    private static final FilterType KNOWN_VARIANT_FILTER_TYPE = FilterType.KNOWN_VARIANT_FILTER;

    private final FilterResult passesFilter = new PassFilterResult(KNOWN_VARIANT_FILTER_TYPE);
    private final FilterResult failsFilter = new FailFilterResult(KNOWN_VARIANT_FILTER_TYPE);

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (notRepresentedInDatabase(variantEvaluation)) {
            return passesFilter;
        }
        return failsFilter;
    }

    private boolean notRepresentedInDatabase(VariantEvaluation variantEvaluation) {
        return !variantEvaluation.getFrequencyData().isRepresentedInDatabase();
    }

    @Override
    public FilterType getFilterType() {
        return KNOWN_VARIANT_FILTER_TYPE;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(KNOWN_VARIANT_FILTER_TYPE);
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
        return "KnownVariantFilter{}";
    }
}
