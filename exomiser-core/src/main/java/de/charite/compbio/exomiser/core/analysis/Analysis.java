/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is analogous to the {@link Settings} class, although the key difference is that here the {@see #addStep}
 * 
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class);

    //Store the path of the file used to create this data.
    private Path vcfPath = null;
    //there is often no pedigree. 
    private Path pedPath = null;
    //SampleData is not final as it requires building from the VCF/PED files. 
    //This could happen at a separate time to the analysis initially being built.
    @JsonIgnore
    private SampleData sampleData = new SampleData();
    //these are more optional variables
    private List<String> hpoIds = new ArrayList<>();
    private ModeOfInheritance modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
    private ScoringMode scoringMode = ScoringMode.RAW_SCORE;
    private AnalysisMode analysisMode = AnalysisMode.PASS_ONLY;
    private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
    private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);
    private final List<AnalysisStep> analysisSteps = new ArrayList<>();

    public Analysis() {
    }

    public Analysis(Path vcfPath) {
        this.vcfPath = vcfPath;
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

    /**
     * The creation and setting of sample data should not be under the control
     * of an external user. In production this should only be set by the
     * AnalysisRunner as otherwise the entire process will become inconsistent.
     *
     * @param sampleData
     */
    protected void setSampleData(SampleData sampleData) {
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

    public Set<FrequencySource> getFrequencySources() {
        return frequencySources;
    }

    public void setFrequencySources(Set<FrequencySource> frequencySources) {
        this.frequencySources = frequencySources;
    }

    public Set<PathogenicitySource> getPathogenicitySources() {
        return pathogenicitySources;
    }

    public void setPathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
        this.pathogenicitySources = pathogenicitySources;
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
        hash = 73 * hash + Objects.hashCode(this.vcfPath);
        hash = 73 * hash + Objects.hashCode(this.pedPath);
        hash = 73 * hash + Objects.hashCode(this.hpoIds);
        hash = 73 * hash + Objects.hashCode(this.modeOfInheritance);
        hash = 73 * hash + Objects.hashCode(this.scoringMode);
        hash = 73 * hash + Objects.hashCode(this.analysisMode);
        hash = 73 * hash + Objects.hashCode(this.frequencySources);
        hash = 73 * hash + Objects.hashCode(this.pathogenicitySources);
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
        if (!Objects.equals(this.frequencySources, other.frequencySources)) {
            return false;
        }
        if (!Objects.equals(this.pathogenicitySources, other.pathogenicitySources)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Analysis{" + "vcfPath=" + vcfPath + ", pedPath=" + pedPath + ", hpoIds=" + hpoIds + ", modeOfInheritance=" + modeOfInheritance + ", scoringMode=" + scoringMode + ", analysisMode=" + analysisMode + ", frequencySources=" + frequencySources + ", pathogenicitySources=" + pathogenicitySources + ", analysisSteps=" + analysisSteps + '}';
    }

}
