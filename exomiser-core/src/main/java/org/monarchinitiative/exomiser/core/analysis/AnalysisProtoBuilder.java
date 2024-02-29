/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.FiltersProto;
import org.monarchinitiative.exomiser.api.v1.PrioritisersProto;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.FailedVariantFilter;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Fluent builder for an {@link AnalysisProto.Analysis} object. The API is identical to the {@link AnalysisBuilder}, other
 * than the return types.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class AnalysisProtoBuilder implements FluentAnalysisBuilder<AnalysisProto.Analysis> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisProtoBuilder.class);

    private final AnalysisProto.Analysis.Builder builder;

    private InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.empty();
    private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
    private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);

    AnalysisProtoBuilder() {
        this.builder = AnalysisProto.Analysis.newBuilder();
    }

    public static AnalysisProtoBuilder builder() {
        return new AnalysisProtoBuilder();
    }

    public AnalysisProto.Analysis build() {
        for (AnalysisProto.AnalysisStep analysisStep : builder.getStepsList()) {
            if (analysisStep.hasFrequencyFilter() && frequencySources.isEmpty()) {
                throw new IllegalStateException("Frequency sources have not been defined. Frequency sources required to run FrequencyFilter");
            }
            if (analysisStep.hasKnownVariantFilter() && frequencySources.isEmpty()) {
                throw new IllegalStateException("Frequency sources have not been defined. Frequency sources required to run KnownVariantFilter");
            }
            if (analysisStep.hasPathogenicityFilter() && pathogenicitySources.isEmpty()) {
                throw new IllegalStateException("Pathogenicity sources have not been defined. Pathogenicity sources required to run PathogenicityFilter");
            }
        }
        return builder.build();
    }

    public AnalysisProtoBuilder analysisMode(AnalysisMode analysisMode) {
        builder.setAnalysisMode(analysisMode == AnalysisMode.FULL ? AnalysisProto.AnalysisMode.FULL : AnalysisProto.AnalysisMode.PASS_ONLY);
        return this;
    }

    public AnalysisProtoBuilder inheritanceModes(InheritanceModeOptions inheritanceModeOptions) {
        this.inheritanceModeOptions = Objects.requireNonNull(inheritanceModeOptions);
        // maintain order as supplied
        inheritanceModeOptions.getMaxFreqs().forEach((moi, freq) -> builder.putInheritanceModes(moi.toString(), freq));
        return this;
    }

    public AnalysisProtoBuilder frequencySources(Set<FrequencySource> frequencySources) {
        this.frequencySources = Sets.immutableEnumSet(frequencySources);
        // maintain order as supplied
        frequencySources.forEach(frequencySource -> builder.addFrequencySources(frequencySource.toString()));
        return this;
    }

    public AnalysisProtoBuilder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
        this.pathogenicitySources = Sets.immutableEnumSet(pathogenicitySources);
        // maintain order as supplied
        pathogenicitySources.forEach(pathogenicitySource -> builder.addPathogenicitySources(pathogenicitySource.toString()));
        return this;
    }

    private AnalysisProto.AnalysisStep.Builder stepBuilder() {
        return AnalysisProto.AnalysisStep.newBuilder();
    }

    //Filters

    /**
     * Adds a {@link FailedVariantFilter} to the {@link Analysis}. This will remove variants without a 'PASS' or '.' in
     * the input VCF.
     *
     * @return An {@link AnalysisProtoBuilder} with a {@link FailedVariantFilter} added to the analysis steps.
     */
    public AnalysisProtoBuilder addFailedVariantFilter() {
        builder.addSteps(stepBuilder().setFailedVariantFilter(FiltersProto.FailedVariantFilter.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addIntervalFilter(GeneticInterval interval) {
        Objects.requireNonNull(interval);
        builder.addSteps(stepBuilder()
                .setIntervalFilter(
                        FiltersProto.IntervalFilter.newBuilder().addIntervals(interval.toString()))
        );
        return this;
    }

    /**
     * @param chromosomalRegions regions within which variants should be considered in an analysis
     * @return An {@link AnalysisProtoBuilder} with a {@link Collection<ChromosomalRegion>} added to the analysis steps.
     */
    public AnalysisProtoBuilder addIntervalFilter(Collection<ChromosomalRegion> chromosomalRegions) {
        Objects.requireNonNull(chromosomalRegions);
        FiltersProto.IntervalFilter.Builder intervalFilterBuilder = FiltersProto.IntervalFilter.newBuilder();
        chromosomalRegions.stream()
                .map(region -> new GeneticInterval(region.contigId(), region.start(), region.end()))
                .forEach(geneticInterval -> intervalFilterBuilder.addIntervals(geneticInterval.toString()));
        this.builder.addSteps(stepBuilder().setIntervalFilter(intervalFilterBuilder));
        return this;
    }

    public AnalysisProtoBuilder addGeneIdFilter(Set<String> entrezIds) {
        Objects.requireNonNull(entrezIds);
        builder.addSteps(stepBuilder().setGenePanelFilter(FiltersProto.GenePanelFilter.newBuilder()
                .addAllGeneSymbols(entrezIds)));
        return this;
    }

    public AnalysisProtoBuilder addVariantEffectFilter(Set<VariantEffect> variantEffects) {
        FiltersProto.VariantEffectFilter.Builder variantEffectFilterBuilder = FiltersProto.VariantEffectFilter.newBuilder();
        variantEffects.forEach(variantEffect -> variantEffectFilterBuilder.addRemove(variantEffect.toString()));
        builder.addSteps(stepBuilder().setVariantEffectFilter(variantEffectFilterBuilder));
        return this;
    }

    public AnalysisProtoBuilder addQualityFilter(double cutOff) {
        builder.addSteps(stepBuilder().setQualityFilter(FiltersProto.QualityFilter.newBuilder()
                .setMinQuality((float) cutOff)));
        return this;
    }

    public AnalysisProtoBuilder addKnownVariantFilter() {
        builder.addSteps(stepBuilder().setKnownVariantFilter(FiltersProto.KnownVariantFilter.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addFrequencyFilter(float cutOff) {
        builder.addSteps(stepBuilder().setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                .setMaxFrequency(cutOff)));
        return this;
    }

    @Override
    public AnalysisProtoBuilder addGeneBlacklistFilter() {
        builder.addSteps(stepBuilder().setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder()));
        return this;
    }

    /**
     * Add a frequency filter using the maximum frequency for any defined mode of inheritance as the cut-off. Calling this
     * method requires that the {@code inheritanceModes} method has already been called and supplied with a non-empty
     * {@link InheritanceModeOptions} instance.
     *
     * @return an {@link AnalysisProtoBuilder} with an added {@link FrequencyFilter} instantiated with the maximum
     * frequency taken from the {@link InheritanceModeOptions}.
     * @since 11.0.0
     */
    public AnalysisProtoBuilder addFrequencyFilter() {
        if (inheritanceModeOptions.isEmpty()) {
            throw new IllegalArgumentException("Unable to add frequency filter with undefined max frequency without inheritanceModeOptions being set.");
        }
        float cutOff = inheritanceModeOptions.getMaxFreq();
        return addFrequencyFilter(cutOff);
    }

    public AnalysisProtoBuilder addPathogenicityFilter(boolean keepNonPathogenic) {
        builder.addSteps(stepBuilder()
                .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder()
                        .setKeepNonPathogenic(keepNonPathogenic)));
        return this;
    }

    public AnalysisProtoBuilder addPriorityScoreFilter(PriorityType priorityType, float minPriorityScore) {
        builder.addSteps(stepBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setPriorityType(priorityType.toString())
                        .setMinPriorityScore(minPriorityScore)));
        return this;
    }

    public AnalysisProtoBuilder addRegulatoryFeatureFilter() {
        builder.addSteps(stepBuilder().setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addInheritanceFilter() {
        // this is just being added to the proto which we don't actually need as the inheritanceModeOptions is defined
        // as being empty and only needs to be created fo the real Analysis object.
        if (inheritanceModeOptions == null || inheritanceModeOptions.isEmpty()) {
            logger.info("Not adding an inheritance filter for undefined mode of inheritance");
            return this;
        }
//        FiltersProto.InheritanceFilter.Builder inheritanceFilterBuilder = FiltersProto.InheritanceFilter.newBuilder();
//        inheritanceModeOptions.getDefinedModes()
//                .forEach(modeOfInheritance -> inheritanceFilterBuilder.addInheritanceModes(modeOfInheritance.toString()));
//        builder.addSteps(stepBuilder().setInheritanceFilter(inheritanceFilterBuilder));

        builder.addSteps(stepBuilder().setInheritanceFilter(FiltersProto.InheritanceFilter.getDefaultInstance()));
        return this;
    }

    //Prioritisers
    public AnalysisProtoBuilder addOmimPrioritiser() {
        builder.addSteps(stepBuilder().setOmimPrioritiser(PrioritisersProto.OmimPrioritiser.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addPhivePrioritiser() {
        builder.addSteps(stepBuilder().setPhivePrioritiser(PrioritisersProto.PhivePrioritiser.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addHiPhivePrioritiser() {
        builder.addSteps(stepBuilder().setHiPhivePrioritiser(PrioritisersProto.HiPhivePrioritiser.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        builder.addSteps(stepBuilder()
                .setHiPhivePrioritiser(
                        PrioritisersProto.HiPhivePrioritiser.newBuilder()
                                .setRunParams(hiPhiveOptions.getRunParams())
                                .setCandidateGeneSymbol(hiPhiveOptions.getCandidateGeneSymbol())
                                .setDiseaseId(hiPhiveOptions.getDiseaseId())
                )
        );
        return this;
    }

    public AnalysisProtoBuilder addPhenixPrioritiser() {
        builder.addSteps(stepBuilder().setPhenixPrioritiser(PrioritisersProto.PhenixPrioritiser.getDefaultInstance()));
        return this;
    }

    public AnalysisProtoBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes) {
        builder.addSteps(stepBuilder()
                .setExomeWalkerPrioritiser(PrioritisersProto.ExomeWalkerPrioritiser.newBuilder().addAllSeedGeneIds(seedGenes)));
        return this;
    }

    public AnalysisProtoBuilder addAnalysisStep(AnalysisProto.AnalysisStep analysisStep) {
        builder.addSteps(analysisStep);
        return this;
    }

}
