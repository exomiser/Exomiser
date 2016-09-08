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

package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.analysis.util.*;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilterRunner;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    private final SampleDataFactory sampleDataFactory;
    private final VariantDataService variantDataService;
    protected final VariantFilterRunner variantFilterRunner;
    private final GeneFilterRunner geneFilterRunner;
    private ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex;

    public AbstractAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.sampleDataFactory = sampleDataFactory;
        this.variantDataService = variantDataService;
        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
    }

    @Override
    public SampleData run(Analysis analysis) {

        final SampleData sampleData = makeSampleDataWithoutGenesOrVariants(analysis);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        Instant timeStart = Instant.now();

        final Pedigree pedigree = sampleData.getPedigree();
        final Path vcfPath = analysis.getVcfPath();

        //soo many comments - this is a bad sign that this is too complicated.
        Map<String, Gene> allGenes = makeKnownGenes();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
//        some kind of multi-map with ordered duplicate keys would allow for easy grouping of steps for running the groups together.
        List<List<AnalysisStep>> analysisStepGroups = analysis.getAnalysisStepsGroupedByFunction();
        boolean variantsLoaded = false;
        for (List<AnalysisStep> analysisGroup : analysisStepGroups) {
            //this is admittedly pretty confusing code and I'm sorry. It's easiest to follow if you turn on debugging.
            //The analysis steps are run in groups of VARIANT_FILTER, GENE_ONLY_DEPENDENT or INHERITANCE_MODE_DEPENDENT
            AnalysisStep firstStep = analysisGroup.get(0);
            logger.debug("Running {} group: {}", firstStep.getType(), analysisGroup);
            if (firstStep.isVariantFilter() && !variantsLoaded) {
                //variants take up 99% of all the memory in an analysis - this scales approximately linearly with the sample size
                //so for whole genomes this is best run as a stream to filter out the unwanted variants with as many filters as possible in one go
                variantEvaluations = loadAndFilterVariants(vcfPath, allGenes, analysisGroup, analysis);
                //this is done here as there are GeneFilter steps which may require Variants in the genes, or the InheritanceModeDependent steps which definitely need them...
                assignVariantsToGenes(variantEvaluations, allGenes);
                variantsLoaded = true;
            } else {
                runSteps(analysisGroup, new ArrayList<>(allGenes.values()), pedigree, analysis.getModeOfInheritance());
            }
        }
        //maybe only the non-variant dependent steps have been run in which case we need to load the variants although
        //the results might be a bit meaningless.
        //See issue #129 This is an excellent place to put the output of a gene phenotype score only run.
        //i.e. stream in the variants, annotate them (assign a gene symbol) then write out that variant with the calculated GENE_PHENO_SCORE (prioritiser scores).
        //this would fit well with a lot of people's pipelines where they only want the phenotype score as they are using VEP or ANNOVAR for variant analysis.
        if (!variantsLoaded) {
            try(Stream<VariantEvaluation> variantStream = loadVariants(vcfPath)) {
                variantEvaluations = variantStream.collect(toList());
            }
            assignVariantsToGenes(variantEvaluations, allGenes);
        }

        final List<Gene> genes = getFinalGeneList(allGenes);
        sampleData.setGenes(genes);
        final List<VariantEvaluation> variants = getFinalVariantList(variantEvaluations);
        sampleData.setVariantEvaluations(variants);

        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());
        logger.info("Analysed {} genes containing {} filtered variants", genes.size(), variants.size());
//        logTopNumScoringGenes(5, genes, analysis);

        Duration duration = Duration.between(timeStart, Instant.now());
        long ms = duration.toMillis();
        logger.info("Finished analysis in {}m {}s {}ms ({} ms)", (ms / 1000) / 60 % 60, ms / 1000 % 60, ms % 1000, ms);
        return sampleData;
    }

    private List<VariantEvaluation> loadAndFilterVariants(Path vcfPath, Map<String, Gene> allGenes, List<AnalysisStep> analysisGroup, Analysis analysis) {
        GeneReassigner geneReassigner = createNonCodingVariantGeneReassigner(analysis, allGenes);
        List<VariantFilter> variantFilters = getVariantFilterSteps(analysisGroup);

        List<VariantEvaluation> filteredVariants;
        AtomicInteger streamed = new AtomicInteger();
        AtomicInteger passed = new AtomicInteger();
        try (Stream<VariantEvaluation> variantStream = loadVariants(vcfPath)) {
            filteredVariants = variantStream
                    .map(logLoadedAndPassedVariants(streamed, passed))
                    .map(reassignNonCodingVariantToBestGeneInJannovarAnnotations(allGenes, geneReassigner))
                    .map(reassignNonCodingVariantToBestGeneInTad(allGenes, geneReassigner))
                    .filter(isAssociatedWithKnownGene(allGenes))
                    .filter(runVariantFilters(variantFilters))
                    .map(logPassedVariants(passed))
                    .collect(toList());
        }
        logger.info("Loaded {} variants - {} passed variant filters", streamed.get(), passed.get());
        return filteredVariants;
    }

    private GeneReassigner createNonCodingVariantGeneReassigner(Analysis analysis, Map<String, Gene> allGenes) {
        ChromosomalRegionIndex<TopologicalDomain> tadIndex = new ChromosomalRegionIndex<>(variantDataService.getTopologicallyAssociatedDomains());
        PriorityType mainPriorityType = analysis.getMainPrioritiserType();
        return new GeneReassigner(mainPriorityType, allGenes, tadIndex);
    }

    private List<VariantFilter> getVariantFilterSteps(List<AnalysisStep> analysisSteps) {
        logger.info("Filtering variants with:");
        return analysisSteps.stream()
                .filter(AnalysisStep::isVariantFilter)
                .map(analysisStep -> {
                    logger.info("{}", analysisStep);
                    return (VariantFilter) analysisStep;
                })
                .collect(toList());
    }

    //yep, logging logic
    private Function<VariantEvaluation, VariantEvaluation> logLoadedAndPassedVariants(AtomicInteger streamed, AtomicInteger passed) {
        return variantEvaluation -> {
            streamed.incrementAndGet();
            if (streamed.get() % 100000 == 0) {
                logger.info("Loaded {} variants - {} passed variant filters...", streamed.get(), passed.get());
            }
            return variantEvaluation;
        };
    }

    private Function<VariantEvaluation, VariantEvaluation> reassignNonCodingVariantToBestGeneInTad(Map<String, Gene> genes, GeneReassigner geneReassigner) {
        //todo: this won't function correctly if run before a prioritiser has been run
        return variantEvaluation -> {
            geneReassigner.reassignVariantToMostPhenotypicallySimilarGeneInTad(variantEvaluation);
            return variantEvaluation;
        };
    }
    
    private Function<VariantEvaluation, VariantEvaluation> reassignNonCodingVariantToBestGeneInJannovarAnnotations(Map<String, Gene> genes, GeneReassigner geneReassigner) {
        return variantEvaluation -> {
            if (variantEvaluation.isNonCodingVariant()){
                geneReassigner.reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(variantEvaluation);
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
    abstract Predicate<VariantEvaluation> runVariantFilters(List<VariantFilter> variantFilters);

    //more logging logic
    private Function<VariantEvaluation, VariantEvaluation> logPassedVariants(AtomicInteger passed) {
        return variantEvaluation -> {
            if (variantEvaluation.passedFilters()) {
                passed.incrementAndGet();
            }
            return variantEvaluation;
        };
    }

    private Stream<VariantEvaluation> loadVariants(Path vcfPath) {
        VariantFactory variantFactory = sampleDataFactory.getVariantFactory();
        List<RegulatoryFeature> regulatoryFeatures = variantDataService.getRegulatoryFeatures();
        this.regulatoryRegionIndex = new ChromosomalRegionIndex<>(regulatoryFeatures);
        logger.info("Loaded {} regulatory regions", regulatoryFeatures.size());
        //WARNING!!! THIS IS NOT THREADSAFE DO NOT USE PARALLEL STREAMS
        return variantFactory.streamVariantEvaluations(vcfPath).map(setRegulatoryRegionVariantEffect(regulatoryRegionIndex));
    }

    //Adds the missing REGULATORY_REGION_VARIANT effect to variants - this isn't in the Jannovar data set.
    private Function<VariantEvaluation, VariantEvaluation> setRegulatoryRegionVariantEffect(ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        return variantEvaluation -> {
            VariantEffect variantEffect = variantEvaluation.getVariantEffect();
            //n.b this check here is important as ENSEMBLE can have regulatory regions overlapping with missense variants.
            if (variantEffect == VariantEffect.INTERGENIC_VARIANT || variantEffect == VariantEffect.UPSTREAM_GENE_VARIANT) {
                List<RegulatoryFeature> overlappingFeatures = regulatoryRegionIndex.getRegionsContainingVariant(variantEvaluation);
                if (!overlappingFeatures.isEmpty()) {
                    //the effect is the same for all regulatory regions, so for the sake of speed, just assign it here rather than look it up form the list
                    variantEvaluation.setVariantEffect(VariantEffect.REGULATORY_REGION_VARIANT);
                }
            }
            return variantEvaluation;
        };
    }

    private SampleData makeSampleDataWithoutGenesOrVariants(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleDataWithoutVariantsOrGenes(analysis.getVcfPath(), analysis.getPedPath());
        return sampleData;
    }

    private void assignVariantsToGenes(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            Gene gene = allGenes.get(variantEvaluation.getGeneSymbol());
            gene.addVariant(variantEvaluation);
        }
    }

    /**
     * @param allGenes
     * @return
     */
    protected List<Gene> getFinalGeneList(Map<String, Gene> allGenes) {
        return allGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .collect(toList());
    }

    //TODO: make this abstract? we need the individual runners to define the behaviour - also check other protected methods.
    protected List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants) {
        return variants;
    }

    /**
     * @return a map of genes indexed by gene symbol.
     */
    private Map<String, Gene> makeKnownGenes() {
        return sampleDataFactory.createKnownGenes()
                .parallelStream()
                .collect(toConcurrentMap(Gene::getGeneSymbol, gene -> gene));
    }

    //might this be a nascent class waiting to get out here?
    private void runSteps(List<AnalysisStep> analysisSteps, List<Gene> genes, Pedigree pedigree, ModeOfInheritance modeOfInheritance) {
        boolean inheritanceModesCalculated = false;
        for (AnalysisStep analysisStep : analysisSteps) {
            if (!inheritanceModesCalculated && analysisStep.isInheritanceModeDependent()) {
                analyseGeneCompatibilityWithInheritanceMode(genes, pedigree, modeOfInheritance);
                inheritanceModesCalculated = true;
            }
            runStep(analysisStep, genes);
        }
    }

    private void runStep(AnalysisStep analysisStep, List<Gene> genes) {
        if (analysisStep.isVariantFilter()) {
            VariantFilter filter = (VariantFilter) analysisStep;
            logger.info("Running VariantFilter: {}", filter);
            for (Gene gene : genes) {
                variantFilterRunner.run(filter, gene.getVariantEvaluations());
            }
            return;

        }
        if (GeneFilter.class.isInstance(analysisStep)) {
            GeneFilter filter = (GeneFilter) analysisStep;
            logger.info("Running GeneFilter: {}", filter);
            geneFilterRunner.run(filter, genes);
            return;
        }

        if (Prioritiser.class.isInstance(analysisStep)) {
            Prioritiser prioritiser = (Prioritiser) analysisStep;
            logger.info("Running Prioritiser: {}", prioritiser);
            prioritiser.prioritizeGenes(genes);
        }
    }

    private void analyseGeneCompatibilityWithInheritanceMode(List<Gene> genes, Pedigree pedigree, ModeOfInheritance modeOfInheritance) {
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser(pedigree, modeOfInheritance);
        logger.info("Checking compatibility with {} inheritance mode for genes which passed filters", modeOfInheritance);
        inheritanceModeAnalyser.analyseInheritanceModes(genes);
    }

    private void scoreGenes(List<Gene> genes, ScoringMode scoreMode, ModeOfInheritance modeOfInheritance) {
        logger.info("Scoring genes");
        GeneScorer geneScorer = getGeneScorer(scoreMode);
        geneScorer.scoreGenes(genes, modeOfInheritance);
    }

    private GeneScorer getGeneScorer(ScoringMode scoreMode) {
        if (scoreMode == ScoringMode.RANK_BASED) {
            return new RankBasedGeneScorer();
        }
        return new RawScoreGeneScorer();
    }

    private void logTopNumScoringGenes(int numToLog, List<Gene> genes, Analysis analysis) {
        if (!genes.isEmpty()) {
            List<Gene> topScoringGenes = genes.stream().filter(Gene::passedFilters).limit(numToLog).collect(toList());
            if (topScoringGenes.isEmpty()) {
                logger.info("No genes passed analysis :(");
                return;
            }
            logger.info("Top {} scoring genes compatible with phenotypes {} were:", numToLog, analysis.getHpoIds());
            topScoringGenes.forEach(topScoringGene -> {
                logger.info("{}", topScoringGene);
                topScoringGene.getPassedVariantEvaluations().forEach(variant ->
                        logger.info("{} {}", variant.getGeneSymbol(), variant)
                );
            });

        }
    }

}
