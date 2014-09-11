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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class VariantEvaluationDataFactory {
    
    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;
    
    //TODO: these need to use EHCache and have a user-definable size-limit set on them.
    private final Map<String, FrequencyData> frequencyCache;
    private final Map<String, PathogenicityData> pathogenicityCache;

    public VariantEvaluationDataFactory(Map<String, FrequencyData> frequencyCache, Map<String, PathogenicityData> pathogenicityCache) {
        this.frequencyCache = frequencyCache;
        this.pathogenicityCache = pathogenicityCache;
    }
    
    public void addPathogenicityData(VariantEvaluation variantEvaluation) {
        Variant variant = variantEvaluation.getVariant();
        PathogenicityData pathData = getPathogenicityData(variant);
        variantEvaluation.setPathogenicityData(pathData);
    }
    
    public void addFrequencyData(VariantEvaluation variantEvaluation) {
        Variant variant = variantEvaluation.getVariant();
        FrequencyData freqData = getFrequencyData(variant);
        variantEvaluation.setFrequencyData(freqData);
    }
    
    protected PathogenicityData getPathogenicityData(Variant variant) {
        String variantCoordinates = variant.getChromosomalVariant();
        
        if (pathogenicityCache.containsKey(variantCoordinates)) {
            return pathogenicityCache.get(variantCoordinates);
        }
        
        PathogenicityData pathData = pathogenicityDao.getPathogenicityData(variant);
        
        pathogenicityCache.put(variantCoordinates, pathData);
        
        return pathData;
    }

    
    protected FrequencyData getFrequencyData(Variant variant) {
        String variantCoordinates = variant.getChromosomalVariant();
        
        if (frequencyCache.containsKey(variantCoordinates)) {
            return frequencyCache.get(variantCoordinates);
        }
        
        FrequencyData frequencyData = frequencyDao.getFrequencyData(variant);
        
        frequencyCache.put(variantCoordinates, frequencyData);
              
        return frequencyData;
    }
}
