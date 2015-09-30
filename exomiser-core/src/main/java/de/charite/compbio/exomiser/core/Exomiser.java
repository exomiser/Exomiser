/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.analysis.AnalysisFactory;
import de.charite.compbio.exomiser.core.analysis.AnalysisMode;
import de.charite.compbio.exomiser.core.analysis.AnalysisRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class Exomiser {
    
    private static final Logger logger = LoggerFactory.getLogger(Exomiser.class);
    
    private final AnalysisFactory analysisFactory;

    @Autowired
    public Exomiser(AnalysisFactory analysisFactory) {
        this.analysisFactory = analysisFactory;
    }
    
    public void run(Analysis analysis) {
        AnalysisRunner runner = makeAnalysisRunner(analysis);
        runner.runAnalysis(analysis);
    }

    private AnalysisRunner makeAnalysisRunner(Analysis analysis) {
        AnalysisMode analysisMode = analysis.getAnalysisMode();
        logger.info("Running analysis with mode: {}", analysisMode);
        switch (analysisMode) {
            case FULL:
                return analysisFactory.getFullAnalysisRunner();
            case SPARSE:
                return analysisFactory.getSparseAnalysisRunner();
            case PASS_ONLY:
                return analysisFactory.getPassOnlyAnalysisRunner();
            default:
                //this guy takes up the least RAM
                return analysisFactory.getPassOnlyAnalysisRunner();
        }
    }
    
}
