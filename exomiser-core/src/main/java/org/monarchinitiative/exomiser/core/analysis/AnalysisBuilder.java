package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
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
    private final VariantDataService variantDataService;

    private final Analysis.Builder builder;

    //Sample-related variables
    private List<String> hpoIds = new ArrayList<>();
    private ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;
    //Source-data-related variables
    private Set<FrequencySource> frequencySources = EnumSet.noneOf(FrequencySource.class);
    private Set<PathogenicitySource> pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);

    private List<AnalysisStep> analysisSteps = new ArrayList<>();

    AnalysisBuilder(PriorityFactory priorityFactory, VariantDataService variantDataService) {
        this.priorityFactory = priorityFactory;
        this.variantDataService = variantDataService;
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

    public AnalysisBuilder pedPath(Path pedPath) {
        builder.pedPath(pedPath);
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

    public AnalysisBuilder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
        builder.modeOfInheritance(modeOfInheritance);
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
    public AnalysisBuilder addIntervalFilter(GeneticInterval interval) {
        analysisSteps.add(new IntervalFilter(interval));
        return this;
    }

    public AnalysisBuilder addGeneIdFilter(Set<Integer> entrezIds) {
        analysisSteps.add(new EntrezGeneIdFilter(new LinkedHashSet<>(entrezIds)));
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

    public AnalysisBuilder addKnownVariantFilterFilter() {
        analysisSteps.add(makeFrequencyDependentStep(new KnownVariantFilter()));
        return this;
    }

    private FrequencyDataProvider makeFrequencyDependentStep(VariantFilter filter) {
        if (frequencySources.isEmpty()) {
            throw new IllegalStateException("Frequency sources have not yet been defined. Add some frequency sources before defining the analysis steps.");
        }
        return new FrequencyDataProvider(variantDataService, frequencySources, filter);
    }

    public AnalysisBuilder addFrequencyFilter(float cutOff) {
        analysisSteps.add(makeFrequencyDependentStep(new FrequencyFilter(cutOff)));
        return this;
    }

    public AnalysisBuilder addPathogenicityFilter(boolean keepNonPathogenic) {
        analysisSteps.add(makePathogenicityDependentStep(new PathogenicityFilter(keepNonPathogenic)));
        return this;
    }

    private PathogenicityDataProvider makePathogenicityDependentStep(PathogenicityFilter pathogenicityFilter) {
        if (pathogenicitySources.isEmpty()) {
            throw new IllegalStateException("Pathogenicity sources have not yet been defined. Add some pathogenicity sources before defining the analysis steps.");
        }
        return new PathogenicityDataProvider(variantDataService, pathogenicitySources, pathogenicityFilter);
    }

    public AnalysisBuilder addPriorityScoreFilter(PriorityType priorityType , float minPriorityScore) {
        analysisSteps.add(new PriorityScoreFilter(priorityType, minPriorityScore));
        return this;
    }

    public AnalysisBuilder addRegulatoryFeatureFilter() {
        analysisSteps.add(new RegulatoryFeatureFilter());
        return this;
    }

    public AnalysisBuilder addInheritanceFilter() {
        if (modeOfInheritance == ModeOfInheritance.ANY) {
            logger.info("Not adding an inheritance filter for {} mode of inheritance", modeOfInheritance);
            return this;
        }
        analysisSteps.add(new InheritanceFilter(modeOfInheritance));
        return this;
    }

    //Prioritisers
    public AnalysisBuilder addOmimPrioritiser() {
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        return this;
    }

    public AnalysisBuilder addPhivePrioritiser() {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makePhivePrioritiser(hpoIds));
        return this;
    }

    private void addPrioritiserStepIfHpoIdsNotEmpty(Prioritiser prioritiser) {
        if (hpoIds == null || hpoIds.isEmpty()) {
            throw new IllegalStateException("HPO IDs not yet defined. Define some sample phenotypes before adding Prioritiser of type " + prioritiser.getPriorityType());
        }
        analysisSteps.add(prioritiser);
    }

    public AnalysisBuilder addHiPhivePrioritiser() {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makeHiPhivePrioritiser(hpoIds, HiPhiveOptions.DEFAULT));
        return this;
    }

    public AnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makeHiPhivePrioritiser(hpoIds, hiPhiveOptions));
        return this;
    }

    public AnalysisBuilder addPhenixPrioritiser() {
        addPrioritiserStepIfHpoIdsNotEmpty(priorityFactory.makePhenixPrioritiser(hpoIds));
        return this;
    }

    public AnalysisBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes) {
        if (seedGenes == null || seedGenes.isEmpty()) {
            throw new IllegalStateException("seedGenes not defined. Define some ENTREZ gene identifiers before adding ExomeWalker prioritier");
        }
        analysisSteps.add(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));
        return this;
    }

    public AnalysisBuilder addAnalysisStep(AnalysisStep analysisStep) {
        analysisSteps.add(analysisStep);
        return this;
    }
}
