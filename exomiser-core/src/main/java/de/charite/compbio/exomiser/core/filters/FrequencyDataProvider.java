package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

/**
 * Decorator implementation to provide variant frequency data to to the variant just before it is needed by the decorated
 * VariantFilter.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProvider implements VariantFilterDataProvider {

    final VariantDataService variantDataService;
    final VariantFilter variantFilter;

    public FrequencyDataProvider(VariantDataService variantDataService, VariantFilter frequencyFilter) {
        this.variantDataService = variantDataService;
        this.variantFilter = frequencyFilter;
    }

    @Override
    public FilterType getFilterType() {
        return variantFilter.getFilterType();
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        variantDataService.setVariantFrequencyData(variantEvaluation);
        return variantFilter.runFilter(variantEvaluation);
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
