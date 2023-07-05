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

import org.monarchinitiative.exomiser.core.analysis.sample.PedigreeSampleValidator;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.*;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    private final GenomeAnalysisService genomeAnalysisService;

    protected final VariantFilterRunner variantFilterRunner;
    private final GeneFilterRunner geneFilterRunner;

    protected AbstractAnalysisRunner(GenomeAnalysisService genomeAnalysisService, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.genomeAnalysisService = genomeAnalysisService;

        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
    }

    @Override
    public AnalysisResults run(Sample sample, Analysis analysis) {
        // This is a critical step. It will validate that all the relevant information is present for the specified steps.
        AnalysisSampleValidator.validate(sample, analysis);

        logger.info("Validating sample input data");
        // all the sample-related bits, might be worth encapsulating
        Path vcfPath = sample.getVcfPath();
        VcfReader vcfReader = vcfPath == null ? new NoOpVcfReader() : new VcfFileReader(vcfPath);
        // n.b. this next block will safely handle a null VCF file
        VariantFactory variantFactory = new VariantFactoryImpl(genomeAnalysisService.getVariantAnnotator(), vcfReader);

        List<String> sampleNames = vcfReader.readSampleIdentifiers();
        String probandIdentifier = SampleIdentifiers.checkProbandIdentifier(sample.getProbandSampleName(), sampleNames);
        Pedigree validatedPedigree = PedigreeSampleValidator.validate(sample.getPedigree(), probandIdentifier, sampleNames);
        InheritanceModeOptions inheritanceModeOptions = analysis.getInheritanceModeOptions();

        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(validatedPedigree, inheritanceModeOptions);

        // now run the analysis on the sample
        if (sample.hasVcf()) {
            int vcfGenotypePosition = SampleIdentifiers.samplePosition(probandIdentifier, sampleNames);
            logger.info("Running analysis for proband {} (sample {} in VCF) from samples: {}. Using coordinates for genome assembly {}.", probandIdentifier, vcfGenotypePosition, sampleNames, sample.getGenomeAssembly());
        } else {
            logger.info("Running analysis for proband {} without VCF", probandIdentifier);
        }
        Instant timeStart = Instant.now();
        //soo many comments - this is a bad sign that this is too complicated.
        Map<String, Gene> allGenes = makeKnownGenes();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        FilterStats filterStats = new FilterStats();

        // How Exomiser uses the input sample data will depend on the analysis steps provided. These are grouped by
        // function (variant filter, gene filter, prioritiser) as an AnalysisGroup. Only a variant filter step/group
        // will trigger the VCF to be loaded and analysed.
        boolean variantsLoaded = false;
        List<AnalysisGroup> analysisStepGroups = AnalysisGroup.groupAnalysisSteps(analysis.getAnalysisSteps());
        logWarningIfSubOptimalAnalysisSumbitted(analysisStepGroups);
        for (AnalysisGroup analysisGroup : analysisStepGroups) {
            // This is admittedly pretty confusing code and I'm sorry. It's easiest to follow if you turn on debugging.
            // The analysis steps are run in groups of VARIANT_FILTER, GENE_ONLY_DEPENDENT or INHERITANCE_MODE_DEPENDENT
            logger.debug("Running group: {}", analysisGroup);
            if (analysisGroup.isVariantFilterGroup() && !variantsLoaded) {
                // Variants take up 99% of all the memory in an analysis - this scales approximately linearly with the
                //  sample size so for whole genomes this is best run as a stream to filter out the unwanted variants
                //  with as many filters as possible in one go
                variantEvaluations = loadAndFilterVariants(variantFactory, probandIdentifier, allGenes, analysisGroup, analysis, filterStats);
                // This is done here as there are GeneFilter steps which may require Variants in the genes, or the
                //  InheritanceModeDependent steps which definitely need them...
                assignVariantsToGenes(variantEvaluations, allGenes);
                variantsLoaded = true;
            } else {
                runSteps(analysisGroup, sample, new ArrayList<>(allGenes.values()), inheritanceModeAnnotator, filterStats);
            }
        }

        if (!filterStats.isEmpty()) {
            logger.info("Variant filter stats are:");
            filterStats.getFilterCounts().forEach(filterStat -> logger.info("{}: pass={} fail={}",
                    filterStat.getFilterType(), filterStat.getPassCount(), filterStat.getFailCount()));
        }

        // If no variant steps have been run and there is a VCF present, don't load it here - See issues #129, #478
        List<Gene> genesToScore = variantsLoaded ? getGenesWithVariants(allGenes) : allGenes.values().stream().filter(genesToScore()).collect(Collectors.toUnmodifiableList());
        // Temporarily add a new PValueGeneScorer so as not to break semver will revert to RawScoreGeneScorer in 14.0.0
        CombinedScorePvalueCalculator combinedScorePvalueCalculator = buildCombinedScorePvalueCalculator(sample, analysis, genesToScore.size());
        GeneScorer geneScorer = new PvalueGeneScorer(probandIdentifier, sample.getSex(), inheritanceModeAnnotator, combinedScorePvalueCalculator);

        logger.info("Scoring genes");
        List<Gene> genes = geneScorer.scoreGenes(genesToScore);
        List<VariantEvaluation> variants = variantsLoaded ? getFinalVariantList(variantEvaluations) : List.of();

        logger.info("Analysed sample {} with {} genes containing {} filtered variants", probandIdentifier, genes.size(), variants.size());
        AnalysisResults analysisResults = AnalysisResults.builder()
                // TODO: add FilterStats? - would make HTML output more meaningful
                .sample(sample)
                .analysis(analysis)
                .sampleNames(sampleNames)
                .genes(genes)
                .variantEvaluations(variants)
                .build();

        Duration duration = Duration.between(timeStart, Instant.now());
        long ms = duration.toMillis();
        logger.info("Finished analysis in {}m {}s {}ms ({} ms)", (ms / 1000) / 60 % 60, ms / 1000 % 60, ms % 1000, ms);
        return analysisResults;
    }

    private void logWarningIfSubOptimalAnalysisSumbitted(List<AnalysisGroup> analysisStepGroups) {
        boolean hasPrioritiserStep = false;
        boolean hasVariantFilterStep = false;
        for (AnalysisGroup analysisGroup : analysisStepGroups) {
            if (analysisGroup.isVariantFilterGroup()) {
                hasVariantFilterStep = true;
            }
            if (analysisGroup.hasPrioritiserStep()) {
                hasPrioritiserStep = true;
            }
        }
        if (!hasPrioritiserStep) {
            logger.warn("RUNNING AN ANALYSIS WITHOUT ANY PHENOTYPE PRIORITISATION WILL LEAD TO SUB-OPTIMAL RESULTS!");
        }
        if (!hasVariantFilterStep) {
            logger.warn("RUNNING AN ANALYSIS WITHOUT ANY VARIANT FILTERING WILL LEAD TO SUB-OPTIMAL RESULTS!");
        }
    }

    private CombinedScorePvalueCalculator buildCombinedScorePvalueCalculator(Sample sample, Analysis analysis, int numFilteredGenes) {
        var prioritiser = analysis.getMainPrioritiser();
        List<Gene> knownGenes = genomeAnalysisService.getKnownGenes();
        return prioritiser == null ? CombinedScorePvalueCalculator.withRandomScores(0, knownGenes.size(), numFilteredGenes) : CombinedScorePvalueCalculator.of(0, prioritiser, sample, knownGenes, numFilteredGenes);
    }

    /**
     * @return a map of genes indexed by gene symbol.
     */
    private Map<String, Gene> makeKnownGenes() {
        return genomeAnalysisService.getKnownGenes()
                .parallelStream()
                .collect(toConcurrentMap(Gene::getGeneSymbol, Function.identity()));
    }

    private List<VariantEvaluation> loadAndFilterVariants(VariantFactory variantFactory, String probandIdentifier, Map<String, Gene> allGenes, AnalysisGroup analysisGroup, Analysis analysis, FilterStats filterStats) {
        GeneReassigner geneReassigner = createNonCodingVariantGeneReassigner(analysis, allGenes);
        List<VariantFilter> variantFilters = prepareVariantFilterSteps(analysis, analysisGroup);

        List<VariantEvaluation> filteredVariants;
        VariantLogger variantLogger = new VariantLogger();

        // this can be done using parallel which dramatically reduces runtime at the expense of RAM and
        //  inability to scale past one job running on one machine
        try (Stream<VariantEvaluation> variantStream = variantFactory.createVariantEvaluations()) {
                    filteredVariants = variantStream
//                        .parallel()
                        .peek(variantLogger.logLoadedAndPassedVariants())
                        .filter(isObservedInProband(probandIdentifier))
                        .map(geneReassigner::reassignRegulatoryAndNonCodingVariantAnnotations)
                        .map(flagWhiteListedVariants())
                        .filter(isAssociatedWithKnownGene(allGenes))
                        .filter(runVariantFilters(variantFilters, filterStats))
                        .peek(variantLogger.countPassedVariant())
                        .collect(Collectors.toUnmodifiableList());
        }
        variantLogger.logResults();
        return filteredVariants;
    }

    // TODO: might be worth pulling out into an AnalysisSupport class or adding to the GenomeAnalysisService?
    private GeneReassigner createNonCodingVariantGeneReassigner(Analysis analysis, Map<String, Gene> allGenes) {
        ChromosomalRegionIndex<TopologicalDomain> tadIndex = genomeAnalysisService.getTopologicallyAssociatedDomainIndex();
        PriorityType mainPriorityType = analysis.getMainPrioritiserType();
        return new GeneReassigner(mainPriorityType, allGenes, tadIndex);
    }

    // TODO: might be worth pulling out into an AnalysisSupport class
    private List<VariantFilter> prepareVariantFilterSteps(Analysis analysis, AnalysisGroup analysisGroup) {
        logger.info("Filtering variants with:");
        List<VariantFilter> list = new ArrayList<>();
        for (AnalysisStep analysisStep : analysisGroup.getAnalysisSteps()) {
            if (analysisStep instanceof VariantFilter) {
                logger.info("{}", analysisStep);
                VariantFilter variantFilter = wrapWithFilterDataProvider((VariantFilter) analysisStep, analysis);
                list.add(variantFilter);
            }
        }
        return list;
    }

    private VariantFilter wrapWithFilterDataProvider(VariantFilter variantFilter, Analysis analysis) {
        if (variantFilter instanceof FrequencyFilter || variantFilter instanceof KnownVariantFilter) {
            logger.info("Wrapping {} with VariantDataProvider for sources {}", variantFilter, analysis.getFrequencySources());
            return new FrequencyDataProvider(genomeAnalysisService, analysis.getFrequencySources(), variantFilter);
        }
        if (variantFilter instanceof PathogenicityFilter) {
            logger.info("Wrapping {} with VariantDataProvider for sources {}", variantFilter, analysis.getPathogenicitySources());
            return new PathogenicityDataProvider(genomeAnalysisService, analysis.getPathogenicitySources(), variantFilter);
        }
        return variantFilter;
    }

    private Predicate<VariantEvaluation> isObservedInProband(String probandId) {
        // gnomAD high quality criteria: (GQ >= 20, DP >= 10, and have now added: allele balance > 0.2 for heterozygote genotypes)
        return variantEvaluation -> {
            SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
            // Getting a SampleGenotype.empty() really shouldn't happen, as the samples and pedigree should have been checked previously
            // only add VariantEvaluation where the proband has an ALT allele (OTHER_ALT should be present as an ALT in another VariantEvaluation)
            return probandGenotype.getCalls().contains(AlleleCall.ALT);
        };
    }

    private UnaryOperator<VariantEvaluation> flagWhiteListedVariants() {
        return variantEvaluation -> {
            if (genomeAnalysisService.variantIsWhiteListed(variantEvaluation)) {
                variantEvaluation.setWhiteListed(true);
            }
            return variantEvaluation;
        };
    }

    /**
     * Defines the filtering behaviour of the runner when performing the initial load and filter of variants. Allows the
     * concrete runner to define whether a variant should pass or fail depending on the gene or status of the gene it is
     * assigned to.
     *
     * @param genes
     * @return
     */
    abstract Predicate<VariantEvaluation> isAssociatedWithKnownGene(Map<String, Gene> genes);

    /**
     * Defines the filtering behaviour of the runner when performing the initial load and filter of variants. Allows the
     * concrete runner to define whether a variant should pass or fail when running the variant through the variant
     * filters defined in the variant filter group, or the initial group if there are more than one.
     *
     * @param variantFilters
     * @return
     */
    abstract Predicate<VariantEvaluation> runVariantFilters(List<VariantFilter> variantFilters, FilterStats filterStats);

    protected abstract Predicate<Gene> genesToScore();

    private void assignVariantsToGenes(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            Gene gene = allGenes.get(variantEvaluation.getGeneSymbol());
            if (gene != null) {
                // It is possible that the gene could be null if no filters have been run and the variant isn't assigned
                // to a known gene (gene symbol  is '.')
                gene.addVariant(variantEvaluation);
            }
        }
    }

    /**
     * @param allGenes
     * @return
     */
    protected List<Gene> getGenesWithVariants(Map<String, Gene> allGenes) {
        return allGenes.values().stream().filter(Gene::hasVariants).collect(Collectors.toUnmodifiableList());
    }

    abstract List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants);

    //might this be a nascent class waiting to get out here?
    private void runSteps(AnalysisGroup analysisGroup, Sample sample, List<Gene> genes, InheritanceModeAnnotator inheritanceModeAnnotator, FilterStats filterStats) {
        boolean inheritanceModesCalculated = false;
        for (AnalysisStep analysisStep : analysisGroup.getAnalysisSteps()) {
            if (!inheritanceModesCalculated && analysisStep.isInheritanceModeDependent()) {
                analyseGeneCompatibilityWithInheritanceMode(genes, inheritanceModeAnnotator);
                inheritanceModesCalculated = true;
            }

            runStep(analysisStep, sample, genes);

            if (analysisStep instanceof Filter<?>) {
                collectFilterStatsForFilter((Filter<?>) analysisStep, genes, filterStats);
            }
        }
    }

    private void analyseGeneCompatibilityWithInheritanceMode(List<Gene> genes, InheritanceModeAnnotator inheritanceModeAnnotator) {
        logger.info("Checking inheritance mode compatibility with {} for genes which passed filters", inheritanceModeAnnotator
                .getDefinedModes());
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser(inheritanceModeAnnotator);
        inheritanceModeAnalyser.analyseInheritanceModes(genes);
    }

    private void runStep(AnalysisStep analysisStep, Sample sample, List<Gene> genes) {

        if (analysisStep instanceof VariantFilter) {
            VariantFilter filter = (VariantFilter) analysisStep;
            logger.info("Running VariantFilter: {}", filter);
            for (Gene gene : genes) {
                variantFilterRunner.run(filter, gene.getVariantEvaluations());
            }
            return;
        }

        if (analysisStep instanceof GeneFilter) {
            GeneFilter filter = (GeneFilter) analysisStep;
            logger.info("Running GeneFilter: {}", filter);
            geneFilterRunner.run(filter, genes);
            return;
        }

        if (analysisStep instanceof Prioritiser) {
            Prioritiser<?> prioritiser = (Prioritiser<?>) analysisStep;
            logger.info("Running Prioritiser: {}", prioritiser);
            prioritiser.prioritizeGenes(sample, genes);
        }
    }

    private void collectFilterStatsForFilter(Filter<?> filter, List<Gene> genes, FilterStats filterStats) {
        FilterType filterType = filter.getFilterType();
        FilterResult passFilterResult = FilterResult.pass(filterType);
        FilterResult failFilterResult = FilterResult.fail(filterType);
        if (filter.isOnlyGeneDependent()) {
            // Cater for the case where the PriorityScoreFilter is run before any variants are loaded
            // don't add variant filter counts here as they can get mixed with genes which did have variants
            // so the numbers don't add up correctly. The alternative is to implement FilterStats::addGeneResult
            // but this also gets messy
            genes.stream()
                    .map(gene -> gene.passedFilter(filterType) ? passFilterResult : failFilterResult)
                    .forEach(filterStats::addResult);
        } else {
            genes.stream()
                    .flatMap(gene -> gene.getVariantEvaluations().stream())
                    .map(variantEvaluation -> variantEvaluation.passedFilter(filterType) ? passFilterResult : failFilterResult)
                    .forEach(filterStats::addResult);
        }
    }

    /**
     * Utility class for logging numbers of processed and passed variants.
     */
    private static class VariantLogger {
        private final AtomicInteger loaded = new AtomicInteger();
        private final AtomicInteger passed = new AtomicInteger();

        private Consumer<VariantEvaluation> logLoadedAndPassedVariants() {
            return variantEvaluation -> {
                loaded.incrementAndGet();
                if (loaded.get() % 100000 == 0) {
                    logger.info("Loaded {} variants - {} passed variant filters...", loaded.get(), passed.get());
                }
            };
        }

        private Consumer<VariantEvaluation> countPassedVariant() {
            return variantEvaluation -> {
                if (variantEvaluation.passedFilters()) {
                    passed.incrementAndGet();
                }
            };
        }

        void logResults() {
            logger.info("Loaded {} variants - {} passed variant filters", loaded.get(), passed.get());
        }
    }
}
