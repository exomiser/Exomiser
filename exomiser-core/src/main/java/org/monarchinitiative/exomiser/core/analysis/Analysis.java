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
package org.monarchinitiative.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * This class is analogous to the {@link Settings} class, although the key difference is that here the {@see #addStep}
 * 
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = Analysis.Builder.class)
@JsonPropertyOrder({"vcfPath", "pedPath", "hpoIds", "modeOfInheritance", "analysisMode", "frequencySources", "pathogenicitySources", "analysisSteps"})
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class);

    //Store the path of the file used to create this data.
    private final Path vcfPath;
    //there is often no pedigree. 
    private final Path pedPath;
    //these are more optional variables
    private final List<String> hpoIds;
    private final ModeOfInheritance modeOfInheritance;

    private final AnalysisMode analysisMode;
    private final Set<FrequencySource> frequencySources;
    private final Set<PathogenicitySource> pathogenicitySources;
    private final List<AnalysisStep> analysisSteps;

    private Analysis(Builder builder) {
        this.vcfPath = builder.vcfPath;
        this.pedPath = builder.pedPath;
        this.hpoIds = ImmutableList.copyOf(builder.hpoIds);
        this.modeOfInheritance = builder.modeOfInheritance;

        this.analysisMode = builder.analysisMode;
        this.frequencySources = Sets.immutableEnumSet(builder.frequencySources);
        this.pathogenicitySources = Sets.immutableEnumSet(builder.pathogenicitySources);
        this.analysisSteps = ImmutableList.copyOf(builder.analysisSteps);
    }

    public Path getVcfPath() {
        return vcfPath;
    }

    public Path getPedPath() {
        return pedPath;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    public List<String> getHpoIds() {
        return hpoIds;
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public Set<FrequencySource> getFrequencySources() {
        return frequencySources;
    }

    public Set<PathogenicitySource> getPathogenicitySources() {
        return pathogenicitySources;
    }

    public List<AnalysisStep> getAnalysisSteps() {
        return analysisSteps;
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

    @JsonCreator
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a shallow copy of the current Analysis. This is only a potential issue for the AnalysisSteps as all other
     * compound classes are immutable.
     * @return an Analysis.Builder copy of the current Analysis.
     */
    public Builder copy() {
        return builder()
                .vcfPath(vcfPath)
                .pedPath(pedPath)
                .hpoIds(hpoIds)
                .modeOfInheritance(modeOfInheritance)

                .analysisMode(analysisMode)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .steps(analysisSteps);
    }

    public static class Builder {

        private Path vcfPath = null;
        //there is often no pedigree.
        private Path pedPath = null;
        //these are more optional variables
        private List<String> hpoIds = new ArrayList<>();
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;

        private AnalysisMode analysisMode = AnalysisMode.PASS_ONLY;
        private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
        private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);
        private List<AnalysisStep> analysisSteps = new ArrayList<>();

        public Analysis build() {
            return new Analysis(this);
        }

        public Builder vcfPath(Path vcfPath) {
            this.vcfPath = vcfPath;
            return this;
        }

        public Builder pedPath(Path pedPath) {
            this.pedPath = pedPath;
            return this;
        }

        public Builder hpoIds(List<String> hpoIds) {
            this.hpoIds = hpoIds;
            return this;
        }

        public Builder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            this.modeOfInheritance = modeOfInheritance;
            return this;
        }

        public Builder analysisMode(AnalysisMode analysisMode) {
            this.analysisMode = analysisMode;
            return this;
        }

        public Builder frequencySources(Set<FrequencySource> frequencySources) {
            this.frequencySources = frequencySources;
            return this;
        }

        public Builder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
            this.pathogenicitySources = pathogenicitySources;
            return this;
        }

        public Builder addStep(AnalysisStep step) {
            this.analysisSteps.add(step);
            return this;
        }

        public Builder steps(List<AnalysisStep> analysisSteps) {
            this.analysisSteps = analysisSteps;
            return this;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.vcfPath);
        hash = 73 * hash + Objects.hashCode(this.pedPath);
        hash = 73 * hash + Objects.hashCode(this.hpoIds);
        hash = 73 * hash + Objects.hashCode(this.modeOfInheritance);
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

        if (this.analysisMode != other.analysisMode) {
            return false;
        }
        if (!Objects.equals(this.frequencySources, other.frequencySources)) {
            return false;
        }
        return Objects.equals(this.pathogenicitySources, other.pathogenicitySources);
    }

    @Override
    public String toString() {
        return "Analysis{" + "vcfPath=" + vcfPath + ", pedPath=" + pedPath + ", hpoIds=" + hpoIds + ", modeOfInheritance=" + modeOfInheritance + ", analysisMode=" + analysisMode + ", frequencySources=" + frequencySources + ", pathogenicitySources=" + pathogenicitySources + ", analysisSteps=" + analysisSteps + '}';
    }
}
