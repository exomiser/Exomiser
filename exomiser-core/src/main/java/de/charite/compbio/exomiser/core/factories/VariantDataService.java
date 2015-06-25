/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.dao.RegulatoryFeatureDao;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class VariantDataService {
    
    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;
    @Autowired
    private RegulatoryFeatureDao regulatoryFeatureDao;
    
    public void setVariantFrequencyRegulatoryFeatureAndPathogenicityData(VariantEvaluation variantEvaluation) {
        setVariantFrequencyData(variantEvaluation);
        setVariantPathogenicityData(variantEvaluation);
        setVariantRegulatoryFeatureData(variantEvaluation);
    }
        
    public void setVariantFrequencyData(VariantEvaluation variantEvaluation) {
        FrequencyData freqData = getVariantFrequencyData(variantEvaluation);
        variantEvaluation.setFrequencyData(freqData);
    }

    public void setVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = getVariantPathogenicityData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }
    
    public void setVariantRegulatoryFeatureData(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.INTERGENIC_VARIANT || variantEvaluation.getVariantEffect() == VariantEffect.UPSTREAM_GENE_VARIANT) {
            VariantEffect variantEffect = getVariantRegulatoryFeatureData(variantEvaluation);
            variantEvaluation.setVariantEffect(variantEffect);
        }
    }

    public FrequencyData getVariantFrequencyData(Variant variant) {
        FrequencyData freqData = frequencyDao.getFrequencyData(variant);
        return freqData;
    }
    
    public PathogenicityData getVariantPathogenicityData(Variant variant) {
        PathogenicityData pathData = pathogenicityDao.getPathogenicityData(variant);
        return pathData;
    }
    
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        VariantEffect variantEffect = regulatoryFeatureDao.getRegulatoryFeatureData(variant);
        return variantEffect;
    }
}
