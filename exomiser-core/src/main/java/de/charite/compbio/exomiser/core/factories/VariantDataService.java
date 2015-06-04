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

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantDataService {

    FrequencyData getVariantFrequencyData(Variant variant);

    PathogenicityData getVariantPathogenicityData(Variant variant);

    void setVariantFrequencyAndPathogenicityData(VariantEvaluation variantEvaluation);

    void setVariantFrequencyData(VariantEvaluation variantEvaluation);

    void setVariantPathogenicityData(VariantEvaluation variantEvaluation);
    
}
