/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleAnalysisRunner extends AbstractAnalysisRunner {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleAnalysisRunner.class);

    public SimpleAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, new SimpleVariantFilterRunner(), new SimpleGeneFilterRunner());
    }

}
