/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.LinkedHashSet;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantDataServiceStub implements VariantDataService {

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant) {
        return new FrequencyData(new RsId(123456), new LinkedHashSet<Frequency>());
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant) {
        return new PathogenicityData(new LinkedHashSet<PathogenicityScore>());
    }

    @Override
    public void setVariantFrequencyAndPathogenicityData(VariantEvaluation variantEvaluation) {
        //deliberately empty
    }

    @Override
    public void setVariantFrequencyData(VariantEvaluation variantEvaluation) {
        //deliberately empty
    }

    @Override
    public void setVariantPathogenicityData(VariantEvaluation variantEvaluation) {
        //deliberately empty
    }

    @Override
    public PathogenicityData getVariantCADDData(Variant variant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVariantCADDData(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVariantRegulatoryFeatureData(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
