/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
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
@JsonPropertyOrder({"vcf", "genomeAssembly", "pedigree", "proband", "hpoIds", "inheritanceModes", "analysisMode", "frequencySources", "pathogenicitySources", "analysisSteps"})
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class);

    //Store the path of the file used to create this data.
    @JsonProperty("vcf")
    private final Path vcfPath;
    //there is often no pedigree.
    private final Pedigree pedigree;

    @JsonProperty("proband")
    private final String probandSampleName;

    private final GenomeAssembly genomeAssembly;

    //these are more optional variables
    private final List<String> hpoIds;

    @JsonProperty("inheritanceModes")
    private final InheritanceModeOptions inheritanceModeOptions;

    private final AnalysisMode analysisMode;
    private final Set<FrequencySource> frequencySources;
    private final Set<PathogenicitySource> pathogenicitySources;
    private final List<AnalysisStep> analysisSteps;

    private Analysis(Builder builder) {
        this.vcfPath = builder.vcfPath;
        this.genomeAssembly = builder.genomeAssembly;
        this.pedigree = builder.pedigree;
        this.probandSampleName = builder.probandSampleName;
        this.hpoIds = ImmutableList.copyOf(builder.hpoIds);
        this.inheritanceModeOptions = builder.inheritanceModeOptions;

        this.analysisMode = builder.analysisMode;
        this.frequencySources = Sets.immutableEnumSet(builder.frequencySources);
        this.pathogenicitySources = Sets.immutableEnumSet(builder.pathogenicitySources);
        this.analysisSteps = ImmutableList.copyOf(builder.analysisSteps);
    }

    public Path getVcfPath() {
        return vcfPath;
    }

    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    /**
     * Returns the {@link Pedigree} used in this {@link Analysis}.
     *
     * @return a pedigree object used for this analysis
     * @since 11.0.0
     */
    public Pedigree getPedigree() {
        return pedigree;
    }

    public String getProbandSampleName() {
        return probandSampleName;
    }

    public List<String> getHpoIds() {
        return hpoIds;
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
            if (analysisStep instanceof Prioritiser) {
                Prioritiser<? extends PriorityResult> prioritiser = (Prioritiser<? extends PriorityResult>) analysisStep;
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
        if (analysisSteps.isEmpty()) {
            logger.debug("No AnalysisSteps to group.");
            return Collections.emptyList();
        }
        List<List<AnalysisStep>> groups = new ArrayList<>();
        AnalysisStep.AnalysisStepType currentGroupType = analysisSteps.get(0).getType();
        logger.debug("First group is for {} steps", currentGroupType);
        List<AnalysisStep> currentGroup = new ArrayList<>();
        for (AnalysisStep step : analysisSteps) {
            if (step.getType() != currentGroupType) {
                logger.debug("Making new group for {} steps", step.getType());
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentGroupType = step.getType();
            }
            currentGroup.add(step);
        }
        //make sure the last group is added too
        groups.add(currentGroup);
        return groups;
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
                .vcfPath(vcfPath)
                .genomeAssembly(genomeAssembly)
                .pedigree(pedigree)
                .probandSampleName(probandSampleName)
                .hpoIds(hpoIds)

                .inheritanceModeOptions(inheritanceModeOptions)
                .analysisMode(analysisMode)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .steps(analysisSteps);
    }

    public static class Builder {

        private Path vcfPath = null;
        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();
        //there is often no pedigree.
        private Pedigree pedigree = Pedigree.empty();
        private String probandSampleName = "";
        //these are more optional variables
        private List<String> hpoIds = new ArrayList<>();

        private AnalysisMode analysisMode = AnalysisMode.PASS_ONLY;
        private InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.empty();
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

        /**
         * Specifies the genome assembly of the sample.
         *
         * @param genomeAssembly
         */
        public Builder genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = genomeAssembly;
            return this;
        }

        /**
         * Sets the {@link Pedigree} for use in this analysis. The object supplied cannot be null.
         * @param pedigree pedigree of the individual(s) listed in the VCF file for this analysis.
         * @return the current Builder object
         * @since 11.0.0
         * @throws NullPointerException when supplied with a null input value
         */
        public Builder pedigree(Pedigree pedigree) {
            Objects.requireNonNull(pedigree, "pedigree cannot be null");
            this.pedigree = pedigree;
            return this;
        }

        public Builder probandSampleName(String probandSampleName) {
            this.probandSampleName = probandSampleName;
            return this;
        }

        public Builder hpoIds(List<String> hpoIds) {
            this.hpoIds = hpoIds;
            return this;
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
            this.analysisSteps = new ArrayList<>(analysisSteps);
            return this;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Analysis analysis = (Analysis) o;
        return Objects.equals(vcfPath, analysis.vcfPath) &&
                Objects.equals(genomeAssembly, analysis.genomeAssembly) &&
                Objects.equals(pedigree, analysis.pedigree) &&
                Objects.equals(probandSampleName, analysis.probandSampleName) &&
                Objects.equals(hpoIds, analysis.hpoIds) &&
                inheritanceModeOptions == analysis.inheritanceModeOptions &&
                analysisMode == analysis.analysisMode &&
                Objects.equals(frequencySources, analysis.frequencySources) &&
                Objects.equals(pathogenicitySources, analysis.pathogenicitySources) &&
                Objects.equals(analysisSteps, analysis.analysisSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vcfPath, genomeAssembly, pedigree, probandSampleName, hpoIds, inheritanceModeOptions, analysisMode, frequencySources, pathogenicitySources, analysisSteps);
    }

    @Override
    public String toString() {
        return "Analysis{" + "vcfPath=" + vcfPath + ", genomeAssembly=" + genomeAssembly + ", pedigree=" + pedigree + ", probandSampleName=" + probandSampleName + ", hpoIds=" + hpoIds + ", inheritanceModeOptions=" + inheritanceModeOptions + ", analysisMode=" + analysisMode + ", frequencySources=" + frequencySources + ", pathogenicitySources=" + pathogenicitySources + ", analysisSteps=" + analysisSteps + '}';
    }
}
