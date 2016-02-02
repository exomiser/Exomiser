/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

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
    private Set<ModeOfInheritance> modesOfInheritance = EnumSet.of(ModeOfInheritance.UNINITIALIZED);
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

    @JsonIgnore
    public PriorityType getMainPrioritiserType() {
        for (AnalysisStep analysisStep : analysisSteps) {
            if (Prioritiser.class.isInstance(analysisStep)) {
                Prioritiser prioritiser = (Prioritiser) analysisStep;
                //OMIM, if combined with other prioritisers isn't the main one.
                if (prioritiser.getPriorityType() != PriorityType.OMIM_PRIORITY) {
                    return prioritiser.getPriorityType();
                }
            }
        }
        return PriorityType.NONE;
    }

    @JsonIgnore
    public List<List<AnalysisStep>> getAnalysisStepsGroupedByFunction() {
        List<List<AnalysisStep>> groups = new ArrayList<>();
        if (analysisSteps.isEmpty()) {
            logger.debug("No AnalysisSteps to group.");
            return groups;
        }

        AnalysisStep currentGroupStep = analysisSteps.get(0);
        List<AnalysisStep> currentGroup = new ArrayList<>();
        currentGroup.add(currentGroupStep);
        logger.debug("First group is for {} steps", currentGroupStep.getType());
        for (int i = 1; i < analysisSteps.size(); i++) {
            AnalysisStep step = analysisSteps.get(i);

            if (currentGroupStep.getType() != step.getType()) {
                logger.debug("Making new group for {} steps", step.getType());
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentGroupStep = step;
            }

            currentGroup.add(step);
        }
        //make sure the last group is added too
        groups.add(currentGroup);

        return groups;
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

    public Set<ModeOfInheritance> getModesOfInheritance() {
        return modesOfInheritance;
    }

    public void setModeOfInheritance(ModeOfInheritance modeOfInheritance) {
        this.modesOfInheritance = EnumSet.of(modeOfInheritance);
    }
    
    public void setModesOfInheritance(Set<ModeOfInheritance> modesOfInheritance) {
        this.modesOfInheritance = modesOfInheritance;
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
        hash = 73 * hash + Objects.hashCode(this.modesOfInheritance);
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
        if (!Objects.equals(this.modesOfInheritance,other.modesOfInheritance)) {
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
        return "Analysis{" + "vcfPath=" + vcfPath + ", pedPath=" + pedPath + ", hpoIds=" + hpoIds + ", modeOfInheritance=" + modesOfInheritance + ", scoringMode=" + scoringMode + ", analysisMode=" + analysisMode + ", frequencySources=" + frequencySources + ", pathogenicitySources=" + pathogenicitySources + ", analysisSteps=" + analysisSteps + '}';
    }

}
