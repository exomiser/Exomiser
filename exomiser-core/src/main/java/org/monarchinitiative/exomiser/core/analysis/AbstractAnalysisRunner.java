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

import org.monarchinitiative.exomiser.core.analysis.sample.PedigreeSampleValidator;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.sample.SampleIdentifierUtil;
import org.monarchinitiative.exomiser.core.analysis.util.*;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.VcfFiles;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    private final GenomeAnalysisService genomeAnalysisService;

    protected final VariantFilterRunner variantFilterRunner;
    private final GeneFilterRunner geneFilterRunner;

    public AbstractAnalysisRunner(GenomeAnalysisService genomeAnalysisService, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.genomeAnalysisService = genomeAnalysisService;

        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
    }

    @Override
    public AnalysisResults run(Sample sample, Analysis analysis) {
        // This is a critical step. It will validate that all the relevant information is present for the specified steps.
        AnalysisSampleValidator.validate(sample, analysis);

        logger.info("Starting analysis");
        logger.info("Using genome assembly {}", sample.getGenomeAssembly());
        // all the sample-related bits, might be worth encapsulating
        Path vcfPath = sample.getVcfPath();
        // n.b. this next block will safely handle a null VCF file
        logger.info("Checking proband and pedigree for VCF {}", vcfPath);
        List<String> sampleNames = VcfFiles.readSampleIdentifiers(vcfPath);
        // TODO: given we now have a Sample object, shouldn't it be used in the analysis?
        SampleIdentifier sampleIdentifier = SampleIdentifierUtil.createProbandIdentifier(sample.getProbandSampleName(), sampleNames);
        Pedigree validatedPedigree = PedigreeSampleValidator.validate(sample.getPedigree(), sampleIdentifier, sampleNames);
        InheritanceModeOptions inheritanceModeOptions = analysis.getInheritanceModeOptions();

        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(validatedPedigree, inheritanceModeOptions);

        //now run the analysis on the sample
        logger.info("Running analysis for proband {} (sample {} in VCF) from samples: {}", sampleIdentifier.getId(), sampleIdentifier
                .getGenotypePosition() + 1, sampleNames);
        Instant timeStart = Instant.now();
        //soo many comments - this is a bad sign that this is too complicated.
        Map<String, Gene> allGenes = makeKnownGenes();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        FilterStats filterStats = new FilterStats();
//        some kind of multi-map with ordered duplicate keys would allow for easy grouping of steps for running the groups together.
        List<List<AnalysisStep>> analysisStepGroups = analysis.getAnalysisStepsGroupedByFunction();
        boolean variantsLoaded = false;
        List<AnalysisGroup> analysisStepGroups = AnalysisGroup.groupAnalysisSteps(analysis.getAnalysisSteps());
        for (AnalysisGroup analysisGroup : analysisStepGroups) {
            // This is admittedly pretty confusing code and I'm sorry. It's easiest to follow if you turn on debugging.
            // The analysis steps are run in groups of VARIANT_FILTER, GENE_ONLY_DEPENDENT or INHERITANCE_MODE_DEPENDENT
            logger.debug("Running group: {}", analysisGroup);
            if (analysisGroup.isVariantFilterGroup() && !variantsLoaded) {
                // Variants take up 99% of all the memory in an analysis - this scales approximately linearly with the
                //  sample size so for whole genomes this is best run as a stream to filter out the unwanted variants
                //  with as many filters as possible in one go
                variantEvaluations = loadAndFilterVariants(sample, sampleIdentifier, allGenes, analysisGroup, analysis, filterStats);
                // This is done here as there are GeneFilter steps which may require Variants in the genes, or the
                //  InheritanceModeDependent steps which definitely need them...
                assignVariantsToGenes(variantEvaluations, allGenes);
                variantsLoaded = true;
            } else {
                runSteps(analysisGroup, sample.getHpoIds(), new ArrayList<>(allGenes.values()), inheritanceModeAnnotator, filterStats);
            }
        }

        if (!filterStats.isEmpty()) {
            logger.info("Variant filter stats are:");
            filterStats.getFilterCounts().forEach(filterStat -> logger.info("{}: pass={} fail={}",
                    filterStat.getFilterType(), filterStat.getPassCount(), filterStat.getFailCount()));
        }

        //maybe only the non-variant dependent steps have been run in which case we need to load the variants although
        //the results might be a bit meaningless.
        //See issue #129 This is an excellent place to put the output of a gene phenotype score only run.
        //i.e. stream in the variants, annotate them (assign a gene symbol) then write out that variant with the calculated GENE_PHENO_SCORE (prioritiser scores).
        //this would fit well with a lot of people's pipelines where they only want the phenotype score as they are using VEP or ANNOVAR for variant analysis.

        if (!variantsLoaded && sample.hasVcf()) {
            try (Stream<VariantEvaluation> variantStream = loadVariants(vcfPath)) {
                variantEvaluations = variantStream.collect(toList());
            }
            assignVariantsToGenes(variantEvaluations, allGenes);
            variantsLoaded = true;
        }

        logger.info("Scoring genes");
        List<Gene> genes;
        List<VariantEvaluation> variants;
        GeneScorer geneScorer = new RawScoreGeneScorer(sampleIdentifier, sample.getSex(), inheritanceModeAnnotator);
        if (variantsLoaded) {
            genes = geneScorer.scoreGenes(getGenesWithVariants(allGenes));
            variants = getFinalVariantList(variantEvaluations);
        } else {
            genes = geneScorer.scoreGenes(new ArrayList<>(allGenes.values()));
            variants = Collections.emptyList();
        }

        logger.info("Analysed {} genes containing {} filtered variants", genes.size(), variants.size());

        logger.info("Creating analysis results from VCF {}", sample.getVcfPath());
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

    /**
     * @return a map of genes indexed by gene symbol.
     */
    private Map<String, Gene> makeKnownGenes() {
        return genomeAnalysisService.getKnownGenes()
                .parallelStream()
                .collect(toConcurrentMap(Gene::getGeneSymbol, Function.identity()));
    }

    private List<VariantEvaluation> loadAndFilterVariants(Sample sample, SampleIdentifier sampleIdentifier, Map<String, Gene> allGenes, AnalysisGroup analysisGroup, Analysis analysis, FilterStats filterStats) {
        GeneReassigner geneReassigner = createNonCodingVariantGeneReassigner(analysis, allGenes);
        List<VariantFilter> variantFilters = prepareVariantFilterSteps(analysis, analysisGroup);

        List<VariantEvaluation> filteredVariants;
        VariantLogger variantLogger = new VariantLogger();
        try (Stream<VariantEvaluation> variantStream = loadVariants(sample.getVcfPath())) {
            filteredVariants = variantStream
                    .peek(variantLogger.logLoadedAndPassedVariants())
                    .filter(isObservedInProband(sampleIdentifier))
                    .map(geneReassigner::reassignRegulatoryAndNonCodingVariantAnnotations)
                    .map(flagWhiteListedVariants())
                    .filter(isAssociatedWithKnownGene(allGenes))
                    .filter(runVariantFilters(variantFilters, filterStats))
                    .peek(variantLogger.countPassedVariant())
                    .collect(toList());
        }
        variantLogger.logResults();
        return filteredVariants;
    }

    private GeneReassigner createNonCodingVariantGeneReassigner(Analysis analysis, Map<String, Gene> allGenes) {
        ChromosomalRegionIndex<TopologicalDomain> tadIndex = genomeAnalysisService.getTopologicallyAssociatedDomainIndex();
        PriorityType mainPriorityType = analysis.getMainPrioritiserType();
        return new GeneReassigner(mainPriorityType, allGenes, tadIndex);
    }

    private List<VariantFilter> getVariantFilterSteps(List<AnalysisStep> analysisSteps) {
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

    private Stream<VariantEvaluation> loadVariants(Path vcfPath) {
        //WARNING!!! THIS IS NOT THREADSAFE DO NOT USE PARALLEL STREAMS
        return genomeAnalysisService.createVariantEvaluations(vcfPath);
    }

    private Predicate<VariantEvaluation> isObservedInProband(SampleIdentifier probandSample) {
        return variantEvaluation -> {
            SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandSample.getId());
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
        return allGenes.values().stream().filter(Gene::hasVariants).collect(toList());
    }

    abstract List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants);

    //might this be a nascent class waiting to get out here?
    private void runSteps(AnalysisGroup analysisGroup, List<String> hpoIds, List<Gene> genes, InheritanceModeAnnotator inheritanceModeAnnotator, FilterStats filterStats) {
        boolean inheritanceModesCalculated = false;
        for (AnalysisStep analysisStep : analysisGroup.getAnalysisSteps()) {
            if (!inheritanceModesCalculated && analysisStep.isInheritanceModeDependent()) {
                analyseGeneCompatibilityWithInheritanceMode(genes, inheritanceModeAnnotator);
                inheritanceModesCalculated = true;
            }

            runStep(analysisStep, hpoIds, genes);

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

    private void runStep(AnalysisStep analysisStep, List<String> hpoIds, List<Gene> genes) {

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
            prioritiser.prioritizeGenes(hpoIds, genes);
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
