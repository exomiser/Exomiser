/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mock of VariantDataService to provide canned responses for variants. Enables
 * testing of the service without requiring any database back-end. This is
 * backed by a pair of maps mapping the variants to their respective
 * Frequency/PathogenicityData
 *
 * @author Jules Jacobsen<jules.jacobsen@sanger.ac.uk>
 */
public class VariantDataServiceMock extends VariantDataServiceImpl {

    private final Map<Variant, FrequencyData> expectedFrequencyData;
    private final Map<Variant, PathogenicityData> expectedPathogenicityData;
    private final Map<Variant, VariantEffect> expectedVariantEffects;

    public VariantDataServiceMock() {
        this.expectedFrequencyData = new HashMap<>();
        this.expectedPathogenicityData = new HashMap<>();
        this.expectedVariantEffects = new HashMap<>();
    }

    public VariantDataServiceMock(Map<Variant, FrequencyData> expectedFrequencyData, Map<Variant, PathogenicityData> expectedPathogenicityData, Map<Variant, VariantEffect> expectedVariantEffects) {
        this.expectedFrequencyData = expectedFrequencyData;
        this.expectedPathogenicityData = expectedPathogenicityData;
        this.expectedVariantEffects = expectedVariantEffects;
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

    /**
     * Adds the expected VariantEffect for the given Variant to the
     * VariantDataService.
     *
     * @param variant
     * @param variantEffect
     */
    public void put(Variant variant, VariantEffect variantEffect) {
        expectedVariantEffects.put(variant, variantEffect);
    }
    
    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        FrequencyData allFrequencyData = expectedFrequencyData.getOrDefault(variant, new FrequencyData());
        return frequencyDataFromSpecifiedSources(allFrequencyData, frequencySources);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        PathogenicityData pathData = expectedPathogenicityData.getOrDefault(variant, new PathogenicityData());
        return pathDataFromSpecifiedDataSources(pathData.getPredictedPathogenicityScores(), pathogenicitySources);
    }

    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        return expectedVariantEffects.getOrDefault(variant, variant.getVariantEffect());
    }

}
