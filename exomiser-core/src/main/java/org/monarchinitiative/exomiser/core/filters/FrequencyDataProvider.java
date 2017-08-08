/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;

import java.util.EnumSet;
import java.util.Set;

/**
 * Decorator implementation to provide variant frequency data to to the variant
 * just before it is needed by the decorated VariantFilter.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProvider extends AbstractFilterDataProvider {

    private final Set<FrequencySource> frequencySources;

    public FrequencyDataProvider(VariantDataService variantDataService, Set<FrequencySource> frequencySources, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);

        if (frequencySources.isEmpty()) {
            this.frequencySources = EnumSet.noneOf(FrequencySource.class);
        } else {
            this.frequencySources = EnumSet.copyOf(frequencySources);
        }
    }

    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        //check there are no frequencies first - this may be genuine, or possibly the variant hasn't yet had the data added
        //this will cut down on trips to the database if multiple filters require frequency data.
        if (variantEvaluation.getFrequencyData().getKnownFrequencies().isEmpty()) {
            FrequencyData frequencyData = variantDataService.getVariantFrequencyData(variantEvaluation, frequencySources);
            variantEvaluation.setFrequencyData(frequencyData);
        }
    }

}
