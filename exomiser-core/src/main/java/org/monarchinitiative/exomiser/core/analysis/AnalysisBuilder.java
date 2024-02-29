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

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class for correctly building an {@link Analysis} object ready to be run by an {@link AnalysisRunner}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisBuilder implements FluentAnalysisBuilder<Analysis> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisBuilder.class);

    // Perhaps combine these into an ueber AnalysisService?
    private final OntologyService ontologyService;
    private final PriorityFactory priorityFactory;
    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;

    private final Analysis.Builder builder;

    private InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.empty();

    //Source-data-related variables
    private GenomeAnalysisService genomeAnalysisService;
    private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
    private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);

    private final List<AnalysisStep> analysisSteps = new ArrayList<>();

    AnalysisBuilder(GenomeAnalysisServiceProvider genomeAnalysisServiceProvider, PriorityFactory priorityFactory, OntologyService ontologyService) {
        this.ontologyService = ontologyService;
        this.priorityFactory = priorityFactory;
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.builder = Analysis.builder();
    }

    // TODO: Should this be building an AnalysisProto (rename to Analysis), then the AnalysisProto is wrapped in an AnalysisRunnerSupport?
    //  alternatively the AnalysisParser could be used for creating a RunnableAnalysis which has all the decorated filters and so on ready to roll
    //  i.e. rename the current Analysis to Job and split into Sample and Analysis
    public Analysis build() {
        new AnalysisStepChecker().check(analysisSteps);
        builder.steps(analysisSteps);
        return builder.build();
    }

    public AnalysisBuilder inheritanceModes(InheritanceModeOptions inheritanceModeOptions) {
        this.inheritanceModeOptions = Objects.requireNonNull(inheritanceModeOptions);
        builder.inheritanceModeOptions(this.inheritanceModeOptions);
        return this;
    }

    public AnalysisBuilder analysisMode(AnalysisMode analysisMode) {
        builder.analysisMode(analysisMode);
        return this;
    }

    public AnalysisBuilder frequencySources(Set<FrequencySource> frequencySources) {
        this.frequencySources = Sets.immutableEnumSet(frequencySources);
        builder.frequencySources(frequencySources);
        return this;
    }

    public AnalysisBuilder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
        this.pathogenicitySources = Sets.immutableEnumSet(pathogenicitySources);
        builder.pathogenicitySources(pathogenicitySources);
        return this;
    }

    //Filters

    /**
     * Adds a {@link FailedVariantFilter} to the {@link Analysis}. This will remove variants without a 'PASS' or '.' in
     * the input VCF.
     *
     * @return An {@link AnalysisBuilder} with a {@link FailedVariantFilter} added to the analysis steps.
     */
    public AnalysisBuilder addFailedVariantFilter() {
        analysisSteps.add(new FailedVariantFilter());
        return this;
    }

    public AnalysisBuilder addIntervalFilter(GeneticInterval interval) {
        analysisSteps.add(new IntervalFilter(interval));
        return this;
    }

    /**
     * @since 12.0.0
     * @param chromosomalRegions regions within which variants should be considered in an analysis
     * @return An {@link AnalysisBuilder} with a {@link Collection<ChromosomalRegion>} added to the analysis steps.
     */
    public AnalysisBuilder addIntervalFilter(Collection<ChromosomalRegion> chromosomalRegions) {
        analysisSteps.add(new IntervalFilter(chromosomalRegions));
        return this;
    }

    public AnalysisBuilder addGeneIdFilter(Set<String> entrezIds) {
        analysisSteps.add(new GeneSymbolFilter(new LinkedHashSet<>(entrezIds)));
        return this;
    }

    public AnalysisBuilder addVariantEffectFilter(Set<VariantEffect> variantEffects) {
        analysisSteps.add(new VariantEffectFilter(Sets.immutableEnumSet(variantEffects)));
        return this;
    }

    public AnalysisBuilder addQualityFilter(double cutoff) {
        analysisSteps.add(new QualityFilter(cutoff));
        return this;
    }

    public AnalysisBuilder addKnownVariantFilter() {
        if (frequencySources.isEmpty()) {
            throw new IllegalArgumentException("Frequency sources have not yet been defined. Add some frequency sources before defining the analysis steps.");
        }
        analysisSteps.add(new KnownVariantFilter());
        return this;
    }

    public AnalysisBuilder addFrequencyFilter(float cutOff) {
        if (frequencySources.isEmpty()) {
            throw new IllegalArgumentException("Frequency sources have not yet been defined. Add some frequency sources before defining the analysis steps.");
        }
        analysisSteps.add(new FrequencyFilter(cutOff));
        return this;
    }
    @Override
    public AnalysisBuilder addGeneBlacklistFilter() {
        analysisSteps.add(new GeneBlacklistFilter());
        return this;
    }

    /**
     * Add a frequency filter using the maximum frequency for any defined mode of inheritance as the cut-off. Calling this
     * method requires that the {@code inheritanceModes} method has already been called and supplied with a non-empty
     * {@link InheritanceModeOptions} instance.
     *
     * @return an {@link AnalysisBuilder} with an added {@link FrequencyFilter} instantiated with the maximum
     * frequency taken from the {@link InheritanceModeOptions}.
     * @since 11.0.0
     */
    public AnalysisBuilder addFrequencyFilter() {
        if (inheritanceModeOptions.isEmpty()) {
            throw new IllegalArgumentException("Unable to add frequency filter with undefined max frequency without inheritanceModeOptions being set.");
        }
        float cutOff = inheritanceModeOptions.getMaxFreq();
        return addFrequencyFilter(cutOff);
    }

    public AnalysisBuilder addPathogenicityFilter(boolean keepNonPathogenic) {
        if (pathogenicitySources.isEmpty()) {
            throw new IllegalArgumentException("Pathogenicity sources have not yet been defined. Add some pathogenicity sources before defining the analysis steps.");
        }
        analysisSteps.add(new PathogenicityFilter(keepNonPathogenic));
        return this;
    }

    public AnalysisBuilder addPriorityScoreFilter(PriorityType priorityType, float minPriorityScore) {
        analysisSteps.add(new PriorityScoreFilter(priorityType, minPriorityScore));
        return this;
    }

    public AnalysisBuilder addRegulatoryFeatureFilter() {
        analysisSteps.add(new RegulatoryFeatureFilter());
        return this;
    }

    public AnalysisBuilder addInheritanceFilter() {
        if (inheritanceModeOptions == null || inheritanceModeOptions.isEmpty()) {
            logger.info("Not adding an inheritance filter for undefined mode of inheritance");
            return this;
        }
        analysisSteps.add(new InheritanceFilter(inheritanceModeOptions.getDefinedModes()));
        return this;
    }

    //Prioritisers
    public AnalysisBuilder addOmimPrioritiser() {
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        return this;
    }

    public AnalysisBuilder addPhivePrioritiser() {
        analysisSteps.add(priorityFactory.makePhivePrioritiser());
        return this;
    }

    public AnalysisBuilder addHiPhivePrioritiser() {
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.defaults()));
        return this;
    }

    public AnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions));
        return this;
    }

    public AnalysisBuilder addPhenixPrioritiser() {
        analysisSteps.add(priorityFactory.makePhenixPrioritiser());
        return this;
    }

    public AnalysisBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes) {
        if (seedGenes == null || seedGenes.isEmpty()) {
            throw new IllegalArgumentException("seedGenes not defined. Define some ENTREZ gene identifiers before adding ExomeWalker prioritier");
        }
        analysisSteps.add(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));
        return this;
    }

    public AnalysisBuilder addAnalysisStep(AnalysisStep analysisStep) {
        analysisSteps.add(analysisStep);
        return this;
    }

}
