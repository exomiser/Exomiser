/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class);
    
    private final SampleData sampleData;
    private final ModeOfInheritance modeOfInheritance;
    private final ScoringMode scoringMode;
    private final List<AnalysisStep> analysisSteps;

    public Analysis(SampleData sampleData) {
        this.sampleData = sampleData;
        this.modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        this.scoringMode = ScoringMode.RAW_SCORE;
        this.analysisSteps = new ArrayList<>();
    }
    
    public Analysis(SampleData sampleData, ModeOfInheritance modeOfInheritance) {
        this.sampleData = sampleData;
        this.modeOfInheritance = modeOfInheritance;
        this.scoringMode = ScoringMode.RAW_SCORE;
        this.analysisSteps = new ArrayList<>();
    }
    
    public Analysis(SampleData sampleData, ModeOfInheritance modeOfInheritance, ScoringMode geneScoringMode) {
        this.sampleData = sampleData;
        this.modeOfInheritance = modeOfInheritance;
        this.scoringMode = geneScoringMode;
        this.analysisSteps = new ArrayList<>();
    }

    public SampleData getSampleData() {
        return sampleData;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    public ScoringMode getScoringMode() {
        return scoringMode;
    }

    public void addStep(AnalysisStep step) {
        analysisSteps.add(step);
    }

    public List<AnalysisStep> getAnalysisSteps() {
        return analysisSteps;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.sampleData);
        hash = 83 * hash + Objects.hashCode(this.analysisSteps);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Analysis other = (Analysis) obj;
        if (!Objects.equals(this.sampleData, other.sampleData)) {
            return false;
        }
        if (!Objects.equals(this.analysisSteps, other.analysisSteps)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Analysis{" + "sampleData=" + sampleData + ", modeOfInheritance=" + modeOfInheritance + ", scoringMode=" + scoringMode + ", analysisSteps=" + analysisSteps + '}';
    }

}
