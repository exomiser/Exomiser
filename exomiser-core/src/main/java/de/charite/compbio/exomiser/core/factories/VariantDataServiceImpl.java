/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.CADDDao;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.NCDSDao;
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
    private CADDDao caddDao;
    @Lazy
    @Autowired
    private NCDSDao ncdsDao;
    @Autowired
    private RegulatoryFeatureDao regulatoryFeatureDao;
    
    @Override
    public void setVariantFrequencyAndPathogenicityData(VariantEvaluation variantEvaluation) {
        logger.info("Calling setVariantFrequencyAndPathogenicityData");
        setVariantFrequencyData(variantEvaluation);
        setVariantPathogenicityData(variantEvaluation);
        setVariantCADDData(variantEvaluation);// TODO - this method is called by the simpleVariantFilterRunner so what will happen if no Tabix files - needs to check FilterType ? NOW NEVER CALLED EXCEPT TEST
        setVariantNCDSData(variantEvaluation);// TODO - this method is called by the simpleVariantFilterRunner so what will happen if no Tabix files - needs to check FilterType
        setVariantRegulatoryFeatureData(variantEvaluation);
    }
        
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
    public void setVariantCADDData(VariantEvaluation variantEvaluation) {
        // TODO - if pathogenicty filter is also set then we need to merge data - new method needed
        PathogenicityData pathData = getVariantCADDData(variantEvaluation);
        variantEvaluation.setPathogenicityData(pathData);
    }
    
    @Override
    public void setVariantNCDSData(VariantEvaluation variantEvaluation) {
        // TODO - if pathogenicty filter is also set then we need to merge data - new method needed
        PathogenicityData pathData = getVariantNCDSData(variantEvaluation);
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
    public PathogenicityData getVariantCADDData(Variant variant) {
        PathogenicityData pathData = caddDao.getPathogenicityData(variant);
        return pathData;
    }
    
    @Override
    public PathogenicityData getVariantNCDSData(Variant variant) {
        PathogenicityData pathData = ncdsDao.getPathogenicityData(variant);
        return pathData;
    }
    
    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        VariantEffect variantEffect = regulatoryFeatureDao.getRegulatoryFeatureData(variant);
        return variantEffect;
    }
}
