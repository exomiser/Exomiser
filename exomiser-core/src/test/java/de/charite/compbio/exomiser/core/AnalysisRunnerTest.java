/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilterRunner;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisRunnerTest {
    
    private AnalysisRunner instance;
        
    @Before
    public void setUp() {
        VariantDataService stubDataService = new VariantDataServiceStub();
        VariantFilterRunner variantFilterRunner = new SimpleVariantFilterRunner(stubDataService);
        instance = new AnalysisRunner(variantFilterRunner, new SimpleGeneFilterRunner());
    }

    @Test
    public void canRunAnalysis() {
        instance.runAnalysis(new Analysis());
    }
    
}
