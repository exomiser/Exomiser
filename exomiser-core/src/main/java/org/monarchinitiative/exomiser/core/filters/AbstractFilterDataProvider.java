/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

/**
 * Provides the base functionality for VariantFilterDataProviders.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractFilterDataProvider implements VariantFilterDataProvider {

    protected final VariantDataService variantDataService;
    protected final VariantFilter variantFilter;

    /**
     * Provides the base functionality for all VariantFilterDataProviders.
     * 
     * @param variantDataService
     * @param variantFilter 
     */
    AbstractFilterDataProvider(VariantDataService variantDataService, VariantFilter variantFilter) {
        this.variantDataService = variantDataService;
        this.variantFilter = variantFilter;
    }

    /**
     * Classes extending this class must provide the variantEvaluation with the
     * implementation-specific data.
     *
     * @param variantEvaluation
     */
    @Override
    public abstract void provideVariantData(VariantEvaluation variantEvaluation);

    @Override
    public VariantFilter getDecoratedFilter() {
        return variantFilter;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        provideVariantData(variantEvaluation);
        return variantFilter.runFilter(variantEvaluation);
    }

    @Override
    public FilterType getFilterType() {
        return variantFilter.getFilterType();
    }

    //TODO: is this a good idea to make this class 'invisible' like this?
    @Override
    public boolean equals(Object o) {
        return variantFilter.equals(o);
    }

    @Override
    public int hashCode() {
        return variantFilter.hashCode();
    }

    @Override
    public String toString() {
        return variantFilter.toString();
    }

}
