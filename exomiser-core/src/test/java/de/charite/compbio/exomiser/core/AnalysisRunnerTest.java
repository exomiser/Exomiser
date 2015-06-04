/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.model.SampleData;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisRunnerTest {
    
    private AnalysisRunner instance;
    
    private ExomiserSettings settings;
    
    @Before
    public void setUp() {
        VariantDataService stubDataService = new VariantDataServiceStub();
        settings = new ExomiserSettings.SettingsBuilder().vcfFilePath(Paths.get("testVcf")).build();
        
        instance = new AnalysisRunner(stubDataService, settings);
    }

    @Test
    public void canRunAnalysis() {
        Analysis analysis = new Analysis(new SampleData(), settings);
        instance.runAnalysis(analysis);
    }
    
}
