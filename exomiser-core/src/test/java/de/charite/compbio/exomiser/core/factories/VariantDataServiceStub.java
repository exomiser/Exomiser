/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantDataServiceStub implements VariantDataService {

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        return new FrequencyData(null, Collections.emptySet());
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        return new PathogenicityData(Collections.emptySet());
    }

    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        return variant.getVariantEffect();
    }
    
    @Override
    public List<String> getGenesInTad(Variant variant) {
        return Collections.emptyList();
    }

}
