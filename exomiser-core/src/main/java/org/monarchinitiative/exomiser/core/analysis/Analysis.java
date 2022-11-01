/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriority;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class allows an the excecution of an arbitrary number of {@link AnalysisStep} in almost any order.
 *
 * Creation of an Analysis is *strongly* recommended to be done via an {@link AnalysisBuilder} obtained from an
 * {@link AnalysisFactory} or the {@link org.monarchinitiative.exomiser.core.Exomiser} class.
 * Not doing so will likely result in incorrect/meaningless results.
 * 
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = Analysis.Builder.class)
@JsonPropertyOrder({"analysisMode", "inheritanceModes", "frequencySources", "pathogenicitySources", "steps"})
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class);

    private final AnalysisMode analysisMode;
    @JsonProperty("inheritanceModes")
    private final InheritanceModeOptions inheritanceModeOptions;
    private final Set<FrequencySource> frequencySources;
    private final Set<PathogenicitySource> pathogenicitySources;
    @JsonProperty("steps")
    private final List<AnalysisStep> analysisSteps;

    private Analysis(Builder builder) {
        this.analysisMode = builder.analysisMode;
        this.inheritanceModeOptions = builder.inheritanceModeOptions;
        this.frequencySources = Sets.immutableEnumSet(builder.frequencySources);
        this.pathogenicitySources = Sets.immutableEnumSet(builder.pathogenicitySources);
        this.analysisSteps = List.copyOf(builder.analysisSteps);
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public InheritanceModeOptions getInheritanceModeOptions() {
        return inheritanceModeOptions;
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
            if (analysisStep instanceof Prioritiser<? extends PriorityResult> prioritiser) {
                //OMIM, if combined with other prioritisers isn't the main one.
                if (prioritiser.getPriorityType() != PriorityType.OMIM_PRIORITY) {
                    return prioritiser.getPriorityType();
                }
            }
        }
        return PriorityType.NONE;
    }

    @JsonIgnore
    @Nullable
    public Prioritiser<PriorityResult> getMainPrioritiser() {
        for (AnalysisStep analysisStep : analysisSteps) {
            if (analysisStep instanceof Prioritiser && !(analysisStep instanceof OmimPriority)) {
                return (Prioritiser<PriorityResult>) analysisStep;
            }
        }
        return null;
    }

    /**
     * Returns a new builder instance for creating Analysis objects. *CAUTION* It is strongly advisable to create Analysis
     * objects using the {@link AnalysisBuilder} objects created with the {@link AnalysisFactory}. This will ensure the
     * analysis is in a fit state to run.
     *
     * This method should only be used for simple unit tests. More complete ones should use the {@link AnalysisBuilder}
     * as stated above.
     *
     * @return a new Builder instance.
     */
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
                .inheritanceModeOptions(inheritanceModeOptions)
                .analysisMode(analysisMode)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .steps(analysisSteps);
    }

    public static class Builder {

        private AnalysisMode analysisMode = AnalysisMode.PASS_ONLY;
        private InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.empty();
        private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
        private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);
        private List<AnalysisStep> analysisSteps = new ArrayList<>();

        public Analysis build() {
            return new Analysis(this);
        }

        public Builder inheritanceModeOptions(InheritanceModeOptions inheritanceModeOptions) {
            Objects.requireNonNull(inheritanceModeOptions, "inheritanceModeOptions cannot be null");
            this.inheritanceModeOptions = inheritanceModeOptions;
            return this;
        }

        public Builder inheritanceModeOptions(Map<SubModeOfInheritance, Float> inheritanceModes) {
            Objects.requireNonNull(inheritanceModes, "inheritanceModes cannot be null");
            this.inheritanceModeOptions = InheritanceModeOptions.of(inheritanceModes);
            return this;
        }

        public Builder analysisMode(AnalysisMode analysisMode) {
            this.analysisMode = Objects.requireNonNull(analysisMode);
            return this;
        }

        public Builder frequencySources(Set<FrequencySource> frequencySources) {
            this.frequencySources = Objects.requireNonNull(frequencySources);
            return this;
        }

        public Builder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
            this.pathogenicitySources = Objects.requireNonNull(pathogenicitySources);
            return this;
        }

        public Builder addStep(AnalysisStep step) {
            this.analysisSteps.add(step);
            return this;
        }

        public Builder steps(List<AnalysisStep> analysisSteps) {
            this.analysisSteps = new ArrayList<>(analysisSteps);
            return this;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Analysis analysis = (Analysis) o;
        return analysisMode == analysis.analysisMode &&
                inheritanceModeOptions == analysis.inheritanceModeOptions &&
                Objects.equals(frequencySources, analysis.frequencySources) &&
                Objects.equals(pathogenicitySources, analysis.pathogenicitySources) &&
                Objects.equals(analysisSteps, analysis.analysisSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inheritanceModeOptions, analysisMode, frequencySources, pathogenicitySources, analysisSteps);
    }

    @Override
    public String toString() {
        return "Analysis{analysisMode=" + analysisMode + ", inheritanceModeOptions=" + inheritanceModeOptions + ", frequencySources=" + frequencySources + ", pathogenicitySources=" + pathogenicitySources + ", analysisSteps=" + analysisSteps + '}';
    }
}
