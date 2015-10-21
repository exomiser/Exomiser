/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.analysis.AnalysisFactory;
import de.charite.compbio.exomiser.core.analysis.AnalysisMode;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.TestJannovarDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantAnnotator;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactoryImpl;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserTest {
 
    private Exomiser instance;
        
    private final JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
    private final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(testJannovarData.getRefDict(), testJannovarData.getChromosomes());
    private final VariantFactory variantFactory = new VariantFactory(new VariantAnnotator(variantContextAnnotator));

    private final SampleDataFactory sampleDataFactory = new SampleDataFactory(variantFactory, testJannovarData);
    private final VariantDataService stubDataService = new VariantDataServiceStub();
    
    private final AnalysisFactory analysisFactory = new AnalysisFactory(sampleDataFactory, new PriorityFactoryImpl(), stubDataService);
    
    @Before
    public void setUp() {
        instance = new Exomiser(analysisFactory);
    }
    
    private Analysis makeAnalysisWithMode(AnalysisMode analysisMode) {
        Analysis analysis = new Analysis(Paths.get("src/test/resources/smallTest.vcf"));
        analysis.setAnalysisMode(analysisMode);
        return analysis;  
    }
    
    @Test
    public void canRunAnalysis_Full() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.FULL);
        instance.run(analysis);  
    }
    
    @Test
    public void canRunAnalysis_Sparse() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.SPARSE);
        instance.run(analysis);  
    }
    
    @Test
    public void canRunAnalysis_PassOnly() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.PASS_ONLY);
        instance.run(analysis);  
    }
    
 }
