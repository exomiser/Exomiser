package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;

/**
 * Decorator interface to provide data for variants just in time for the filter 
 * which requires it.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantFilterDataProvider extends VariantFilter {

    /**
     * Provides the variantEvaluation with the implementation-specific data.
     * @param variantEvaluation 
     */
    public void provideVariantData(VariantEvaluation variantEvaluation);
    
    /**
     * @return the decorated filter which the DataProvider is providing data for.
     */
    public VariantFilter getDecoratedFilter();
}
