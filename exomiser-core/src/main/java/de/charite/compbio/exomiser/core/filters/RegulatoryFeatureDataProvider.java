/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RegulatoryFeatureDataProvider extends AbstractFilterDataProvider {
    
    private Map<String, Gene> allGenes;
    
    public RegulatoryFeatureDataProvider(VariantDataService variantDataService, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);
    }
    
    public void setAllGenes(Map<String, Gene> allGenes) {
        this.allGenes = allGenes;
    }
    
    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        VariantEffect variantEffect = variantDataService.getVariantRegulatoryFeatureData(variantEvaluation, allGenes);
        variantEvaluation.setVariantEffect(variantEffect);
    }

}
