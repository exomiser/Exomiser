/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class SimpleAnalysisRunner extends AbstractAnalysisRunner {

    SimpleAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, variantDataService, new SimpleVariantFilterRunner(), new SimpleGeneFilterRunner());
    }

}
