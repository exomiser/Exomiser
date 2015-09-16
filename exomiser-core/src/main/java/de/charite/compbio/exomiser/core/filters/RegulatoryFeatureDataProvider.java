/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RegulatoryFeatureDataProvider extends AbstractFilterDataProvider {
    

    public RegulatoryFeatureDataProvider(VariantDataService variantDataService, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);
    }
    
    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        VariantEffect variantEffect = variantDataService.getVariantRegulatoryFeatureData(variantEvaluation);
        variantEvaluation.setVariantEffect(variantEffect);
    }

}
