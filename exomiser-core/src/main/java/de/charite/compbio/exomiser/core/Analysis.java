/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
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

    //Store the path of the file used to create this data.
    private Path vcfPath;
    private Path pedPath;
    //SampleData is not final as it requires building from the VCF/PED files. 
    //This could happen at a seperate time to the analysis initially being built.
    @JsonIgnore
    private SampleData sampleData;
    //these are more optional variables
    private List<String> hpoIds;
    private ModeOfInheritance modeOfInheritance;
    private ScoringMode scoringMode;
    private AnalysisMode analysisMode;
    private final List<AnalysisStep> analysisSteps;

    public Analysis() {
        vcfPath = null;
        //there is often no pedigree. 
        pedPath = null;
        sampleData = new SampleData();
        hpoIds = new ArrayList<>();
        modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        scoringMode = ScoringMode.RAW_SCORE;
        analysisMode = AnalysisMode.PASS_ONLY;
        analysisSteps = new ArrayList<>();
    }

    public Path getVcfPath() {
        return vcfPath;
    }

    public void setVcfPath(Path vcfPath) {
        this.vcfPath = vcfPath;
    }

    public Path getPedPath() {
        return pedPath;
    }

    public void setPedPath(Path pedPath) {
        this.pedPath = pedPath;
    }

    public SampleData getSampleData() {
        return sampleData;
    }

    public void setSampleData(SampleData sampleData) {
        this.sampleData = sampleData;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    public void setModeOfInheritance(ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
    }

    public ScoringMode getScoringMode() {
        return scoringMode;
    }

    public void setScoringMode(ScoringMode scoreMode) {
        this.scoringMode = scoreMode;
    }

    public List<String> getHpoIds() {
        return hpoIds;
    }

    public void setHpoIds(List<String> hpoIds) {
        this.hpoIds = hpoIds;
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(AnalysisMode analysisMode) {
        this.analysisMode = analysisMode;
    }

    public void addStep(AnalysisStep step) {
        this.analysisSteps.add(step);
    }

    public void addAllSteps(List<AnalysisStep> analysisSteps) {
        this.analysisSteps.addAll(analysisSteps);
    }
    
    public List<AnalysisStep> getAnalysisSteps() {
        return analysisSteps;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.vcfPath);
        hash = 89 * hash + Objects.hashCode(this.pedPath);
        hash = 89 * hash + Objects.hashCode(this.hpoIds);
        hash = 89 * hash + Objects.hashCode(this.modeOfInheritance);
        hash = 89 * hash + Objects.hashCode(this.scoringMode);
        hash = 89 * hash + Objects.hashCode(this.analysisMode);
        hash = 89 * hash + Objects.hashCode(this.analysisSteps);
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
        if (!Objects.equals(this.vcfPath, other.vcfPath)) {
            return false;
        }
        if (!Objects.equals(this.pedPath, other.pedPath)) {
            return false;
        }
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        if (this.modeOfInheritance != other.modeOfInheritance) {
            return false;
        }
        if (this.scoringMode != other.scoringMode) {
            return false;
        }
        if (this.analysisMode != other.analysisMode) {
            return false;
        }
        if (!Objects.equals(this.analysisSteps, other.analysisSteps)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Analysis{" + "vcf=" + vcfPath + ", ped=" + pedPath + ", modeOfInheritance=" + modeOfInheritance + ", scoringMode=" + scoringMode + ", analysisMode=" + analysisMode + ", analysisSteps=" + analysisSteps + '}';
    }

}
