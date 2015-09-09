/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock of VariantDataService to provide canned responses for variants. Enables
 * testing of the service without requiring any database back-end. This is
 * backed by a pair of maps mapping the variants to their respective
 * Frequency/PathogenicityData
 *
 * @author Jules Jacobsen<jules.jacobsen@sanger.ac.uk>
 */
public class VariantDataServiceMock implements VariantDataService {

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
    public FrequencyData getVariantFrequencyData(Variant variant) {
        return expectedFrequencyData.getOrDefault(variant, new FrequencyData());
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant) {
        return expectedPathogenicityData.getOrDefault(variant, new PathogenicityData());
    }

    @Override
    public void setVariantFrequencyData(VariantEvaluation variantEvaluation) {
        variantEvaluation.setFrequencyData(getVariantFrequencyData(variantEvaluation));
    }

    @Override
    public void setVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        variantEvaluation.setPathogenicityData(getVariantPathogenicityData(variantEvaluation));
    }

    @Override
    public PathogenicityData getVariantCaddData(Variant variant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PathogenicityData getVariantNcdsData(Variant variant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVariantCaddData(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVariantNcdsData(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVariantRegulatoryFeatureData(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
