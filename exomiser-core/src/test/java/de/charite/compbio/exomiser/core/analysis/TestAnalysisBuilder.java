/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.model.SampleData;

/**
 * Class to help build Analysis objects for other test classes outside the analysis package.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestAnalysisBuilder {
    
    private final Analysis analysis = new Analysis();
           
    public Analysis build() {
        return analysis;
    }
    
    public TestAnalysisBuilder sampleData(SampleData sampleData) {
        analysis.setSampleData(sampleData);
        return this;
    }
    
    public TestAnalysisBuilder analysisMode(AnalysisMode analysisMode) {
        analysis.setAnalysisMode(analysisMode);
        return this;
    }
    
}
