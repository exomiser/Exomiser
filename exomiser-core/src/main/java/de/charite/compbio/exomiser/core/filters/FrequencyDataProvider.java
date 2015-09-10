package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;

import java.util.EnumSet;
import java.util.Set;

/**
 * Decorator implementation to provide variant frequency data to to the variant
 * just before it is needed by the decorated VariantFilter.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProvider implements VariantFilterDataProvider {

    private final VariantDataService variantDataService;
    private final VariantFilter variantFilter;
    private final Set<FrequencySource> frequencySources;

    public FrequencyDataProvider(VariantDataService variantDataService, Set<FrequencySource> frequencySources, VariantFilter frequencyFilter) {
        this.variantDataService = variantDataService;
        this.variantFilter = frequencyFilter;
        if (frequencySources.isEmpty()) {
            this.frequencySources = EnumSet.noneOf(FrequencySource.class);
        } else {
            this.frequencySources = EnumSet.copyOf(frequencySources);
        }
    }

    @Override
    public FilterType getFilterType() {
        return variantFilter.getFilterType();
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        addMissingFrequencyData(variantEvaluation);
        return variantFilter.runFilter(variantEvaluation);
    }

    private void addMissingFrequencyData(VariantEvaluation variantEvaluation) {
        //check there are no frequencies first - this may be genuine, or possibly the variant hasn't yet had the data added
        //this will cut down on trips to the database if multiple filters require frequency data.
        if (variantEvaluation.getFrequencyData().getKnownFrequencies().isEmpty()) {
            FrequencyData frequencyData = variantDataService.getVariantFrequencyData(variantEvaluation, frequencySources);
            variantEvaluation.setFrequencyData(frequencyData);
        }
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
