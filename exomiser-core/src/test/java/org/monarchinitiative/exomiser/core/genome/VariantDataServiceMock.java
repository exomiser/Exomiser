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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome;

import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mock of VariantDataService to provide canned responses for variants. Enables
 * testing of the service without requiring any database back-end. This is
 * backed by maps mapping the variants to their respective
 * frequency/pathogenicity/whatever data.
 *
 * @author Jules Jacobsen<jules.jacobsen@sanger.ac.uk>
 */
public class VariantDataServiceMock extends VariantDataServiceImpl {

    private final Map<Variant, FrequencyData> expectedFrequencyData;
    private final Map<Variant, PathogenicityData> expectedPathogenicityData;

    public VariantDataServiceMock() {
        this.expectedFrequencyData = new HashMap<>();
        this.expectedPathogenicityData = new HashMap<>();
    }

    public VariantDataServiceMock(Map<Variant, FrequencyData> expectedFrequencyData, Map<Variant, PathogenicityData> expectedPathogenicityData) {
        this.expectedFrequencyData = expectedFrequencyData;
        this.expectedPathogenicityData = expectedPathogenicityData;
    }

    /**
     * Adds the expected FrequencyData for the given Variant to the
     * VariantDataService.
     *
     * @param variant
     * @param frequencyData
     */
    public void put(Variant variant, FrequencyData frequencyData) {
        expectedFrequencyData.put(variant, frequencyData);
    }

    /**
     * Adds the expected PathogenicityData for the given Variant to the
     * VariantDataService.
     *
     * @param variant
     * @param pathogenicityData
     */
    public void put(Variant variant, PathogenicityData pathogenicityData) {
        expectedPathogenicityData.put(variant, pathogenicityData);
    }

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        FrequencyData allFrequencyData = expectedFrequencyData.getOrDefault(variant, FrequencyData.empty());
        return frequencyDataFromSpecifiedSources(allFrequencyData.getRsId(), allFrequencyData.getKnownFrequencies(), frequencySources);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        PathogenicityData pathData = expectedPathogenicityData.getOrDefault(variant, PathogenicityData.empty());
        return pathDataFromSpecifiedDataSources(pathData.getPredictedPathogenicityScores(), pathogenicitySources);
    }

}
