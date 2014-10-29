/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import jannovar.exome.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class VariantEvaluationDataService {
    
    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;
    
    public void setVariantFrequencyAndPathogenicityData(VariantEvaluation variantEvaluation) {
        setVariantFrequencyData(variantEvaluation);
        setVariantPathogenicityData(variantEvaluation);
    }
        
    public void setVariantFrequencyData(VariantEvaluation variantEvaluation) {
        FrequencyData freqData = getVariantFrequencyData(variantEvaluation);
        variantEvaluation.setFrequencyData(freqData);
    }

    public void setVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = getVariantPathogenicityData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }

    public FrequencyData getVariantFrequencyData(VariantEvaluation variantEvaluation) {
        Variant variant = variantEvaluation.getVariant();
        FrequencyData freqData = frequencyDao.getFrequencyData(variant);
        return freqData;
    }
    
    public PathogenicityData getVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        Variant variant = variantEvaluation.getVariant();
        PathogenicityData pathData = pathogenicityDao.getPathogenicityData(variant);
        return pathData;
    }
    
}
