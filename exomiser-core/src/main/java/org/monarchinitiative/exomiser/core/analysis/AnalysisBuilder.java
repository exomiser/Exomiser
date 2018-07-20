/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * Class for correctly building an {@link Analysis} object ready to be run by an {@link AnalysisRunner}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisBuilder.class);

    private final PriorityFactory priorityFactory;
    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;

    private final Analysis.Builder builder;

    //Sample-related variables
    private List<String> hpoIds = new ArrayList<>();
    private InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.empty();
    //Source-data-related variables
    private GenomeAnalysisService genomeAnalysisService;
    private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
    private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);

    private List<AnalysisStep> analysisSteps = new ArrayList<>();

    AnalysisBuilder(PriorityFactory priorityFactory, GenomeAnalysisServiceProvider genomeAnalysisServiceProvider) {
        this.priorityFactory = priorityFactory;
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.builder = Analysis.builder();
    }

    public Analysis build() {
        new AnalysisStepChecker().check(analysisSteps);
        builder.steps(analysisSteps);
        return builder.build();
    }

    public AnalysisBuilder vcfPath(Path vcfPath) {
        builder.vcfPath(vcfPath);
        return this;
    }

    public AnalysisBuilder genomeAssembly(GenomeAssembly genomeAssembly) {
        this.genomeAnalysisService = genomeAnalysisServiceProvider.get(genomeAssembly);
        builder.genomeAssembly(genomeAssembly);
        return this;
    }

    public AnalysisBuilder pedigree(Pedigree pedigree) {
        builder.pedigree(pedigree);
        return this;
    }

    public AnalysisBuilder probandSampleName(String sampleName) {
        builder.probandSampleName(sampleName);
        return this;
    }

    public AnalysisBuilder hpoIds(List<String> hpoIds) {
        this.hpoIds = ImmutableList.copyOf(hpoIds);
        builder.hpoIds(this.hpoIds);
        return this;
    }

    public AnalysisBuilder inheritanceModes(InheritanceModeOptions inheritanceModeOptions) {
        this.inheritanceModeOptions = inheritanceModeOptions;
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
        analysisSteps.add(makeFrequencyDependentStep(new KnownVariantFilter()));
        return this;
    }

    private FrequencyDataProvider makeFrequencyDependentStep(VariantFilter filter) {
        if (frequencySources.isEmpty()) {
            throw new IllegalArgumentException("Frequency sources have not yet been defined. Add some frequency sources before defining the analysis steps.");
        }
        GenomeAnalysisService genomeAnalysisService = getGenomeAnalysisService();
        return new FrequencyDataProvider(genomeAnalysisService, frequencySources, filter);
    }

    private GenomeAnalysisService getGenomeAnalysisService() {
        if (genomeAnalysisService != null) {
            return genomeAnalysisService;
        }
        throw new UndefinedGenomeAssemblyException("Genome assembly has not yet been defined. This is required before adding an assembly-dependant step.");
    }

    public AnalysisBuilder addFrequencyFilter(float cutOff) {
        analysisSteps.add(makeFrequencyDependentStep(new FrequencyFilter(cutOff)));
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
        analysisSteps.add(makeFrequencyDependentStep(new FrequencyFilter(cutOff)));
        return this;
    }

    public AnalysisBuilder addPathogenicityFilter(boolean keepNonPathogenic) {
        analysisSteps.add(makePathogenicityDependentStep(new PathogenicityFilter(keepNonPathogenic)));
        return this;
    }

    private PathogenicityDataProvider makePathogenicityDependentStep(PathogenicityFilter pathogenicityFilter) {
        if (pathogenicitySources.isEmpty()) {
            throw new IllegalArgumentException("Pathogenicity sources have not yet been defined. Add some pathogenicity sources before defining the analysis steps.");
        }
        GenomeAnalysisService genomeAnalysisService = getGenomeAnalysisService();
        return new PathogenicityDataProvider(genomeAnalysisService, pathogenicitySources, pathogenicityFilter);
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
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makePhivePrioritiser());
        return this;
    }

    private void addPrioritiserStepIfHpoIdsNotEmpty(Prioritiser prioritiser) {
        if (hpoIds == null || hpoIds.isEmpty()) {
            throw new IllegalArgumentException("HPO IDs not yet defined. Define some sample phenotypes before adding Prioritiser of type " + prioritiser
                    .getPriorityType());
        }
        analysisSteps.add(prioritiser);
    }

    public AnalysisBuilder addHiPhivePrioritiser() {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.defaults()));
        return this;
    }

    public AnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions));
        return this;
    }

    public AnalysisBuilder addPhenixPrioritiser() {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makePhenixPrioritiser());
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
