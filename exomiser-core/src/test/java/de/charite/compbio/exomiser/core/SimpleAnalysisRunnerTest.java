/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilterRunner;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleAnalysisRunnerTest {
    
    private SimpleAnalysisRunner instance;
        
    @Before
    public void setUp() {
        JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
        VariantFactory variantFactory = new VariantFactory(new VariantAnnotationsFactory(testJannovarData));

        VariantDataService variantDataService = new VariantDataServiceStub();
        instance = new SimpleAnalysisRunner(variantFactory, variantDataService);
    }

    @Test
    public void canRunAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(Paths.get("src/test/resources/smallTest.vcf"));
        instance.runAnalysis(analysis);
    }
    
}
