/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.CaddDao;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.NcdsDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.dao.RegulatoryFeatureDao;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class VariantDataServiceImpl implements VariantDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImpl.class);

    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;
    @Lazy
    @Autowired
    private CaddDao caddDao;
    @Lazy
    @Autowired
    private NcdsDao ncdsDao;
    @Autowired
    private RegulatoryFeatureDao regulatoryFeatureDao;
            
    @Override
    public void setVariantFrequencyData(VariantEvaluation variantEvaluation) {
        FrequencyData freqData = getVariantFrequencyData(variantEvaluation);
        variantEvaluation.setFrequencyData(freqData);
    }

    @Override
    public void setVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = getVariantPathogenicityData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }

    @Override
    public void setVariantCaddData(VariantEvaluation variantEvaluation) {
        // TODO - if pathogenicty filter is also set then we need to merge data - new method needed
        PathogenicityData pathData = getVariantCaddData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }
    
    @Override
    public void setVariantNcdsData(VariantEvaluation variantEvaluation) {
        // TODO - if pathogenicty filter is also set then we need to merge data - new method needed
        PathogenicityData pathData = getVariantNcdsData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }
    
    @Override
    public void setVariantRegulatoryFeatureData(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.INTERGENIC_VARIANT || variantEvaluation.getVariantEffect() == VariantEffect.UPSTREAM_GENE_VARIANT) {
            VariantEffect variantEffect = getVariantRegulatoryFeatureData(variantEvaluation);
            variantEvaluation.setVariantEffect(variantEffect);
        }
    }
    
    @Override
    public FrequencyData getVariantFrequencyData(Variant variant) {
        FrequencyData freqData = frequencyDao.getFrequencyData(variant);
        return freqData;
    }
    
    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant) {
        PathogenicityData pathData = pathogenicityDao.getPathogenicityData(variant);
        return pathData;
    }
    
    @Override
    public PathogenicityData getVariantCaddData(Variant variant) {
        PathogenicityData pathData = caddDao.getPathogenicityData(variant);
        return pathData;
    }
    
    @Override
    public PathogenicityData getVariantNcdsData(Variant variant) {
        PathogenicityData pathData = ncdsDao.getPathogenicityData(variant);
        return pathData;
    }
    
    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        VariantEffect variantEffect = regulatoryFeatureDao.getRegulatoryFeatureData(variant);
        return variantEffect;
    }
}
