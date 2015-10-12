package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserRunner;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.exomiser.core.analysis.util.GeneScorer;
import de.charite.compbio.exomiser.core.analysis.util.RankBasedGeneScorer;
import de.charite.compbio.exomiser.core.analysis.util.RawScoreGeneScorer;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.InheritanceCompatibilityChecker;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.InheritanceCompatibilityCheckerException;
import htsjdk.variant.variantcontext.VariantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.charite.compbio.exomiser.core.analysis.util.GeneReassigner;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    private final SampleDataFactory sampleDataFactory;
    protected final VariantDataService variantDataService;
    protected final VariantFilterRunner variantFilterRunner;
    private final GeneFilterRunner geneFilterRunner;
    private final PrioritiserRunner prioritiserRunner;
    private GeneReassigner geneReassigner;
    protected PriorityType mainPriorityType;
    

    public AbstractAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.sampleDataFactory = sampleDataFactory;
        this.variantDataService = variantDataService;
        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
        this.prioritiserRunner = new PrioritiserRunner();
    }

    @Override
    public void runAnalysis(Analysis analysis) {

        final SampleData sampleData = makeSampleDataWithoutGenesOrVariants(analysis);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        final Pedigree pedigree = sampleData.getPedigree();
        final Path vcfPath = analysis.getVcfPath();
        final List<AnalysisStep> analysisSteps = analysis.getAnalysisSteps();
        new AnalysisStepChecker().check(analysisSteps);

        Map<String, Gene> allGenes = makeKnownGenes();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
//        some kind of multi-map with ordered duplicate keys would allow for easy grouping of steps for running the groups together.
        List<List<AnalysisStep>> analysisStepGroups = groupAnalysisStepsByFunction(analysisSteps);
        boolean variantsLoaded = false;
        for (List<AnalysisStep> analysisGroup : analysisStepGroups) {
            AnalysisStep firstStep = analysisGroup.get(0);
            logger.debug("Running {} group: {}", firstStep.getType(), analysisGroup);
            if (firstStep.isVariantFilter() & !variantsLoaded) {
                geneReassigner = new GeneReassigner(this.variantDataService, this.mainPriorityType);
                //variants take up 99% of all the memory in an analysis - this scales approximately linearly with the sample size
                //so for whole genomes this is best run as a stream to filter out the unwanted variants with as many filters as possible in one go
                variantEvaluations = loadAndFilterVariants(vcfPath, allGenes, analysisGroup);
                
                if (mainPriorityType != null){
                    //GeneReassigner geneReassigner = new GeneReassigner(variantDataService, mainPriorityType);
                    geneReassigner.reassignGeneToMostPhenotypicallySimilarGeneInTad(variantEvaluations, allGenes);
                    geneReassigner.reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(variantEvaluations, allGenes);
                }
                
                assignVariantsToGenes(variantEvaluations, allGenes);
                variantsLoaded = true;
            } else {
                runSteps(analysisGroup, new ArrayList<>(allGenes.values()), pedigree, analysis.getModeOfInheritance());
            }
        }
        //maybe only the non-variant dependent steps have been run in which case we need to load the variants although
        //the results might be a bit meaningless.
        if (!variantsLoaded) {
            variantEvaluations = loadVariants(vcfPath, allGenes, variantEvaluation -> true, variantEvaluation -> true);
            assignVariantsToGenes(variantEvaluations, allGenes);
        }

        final List<Gene> genes = getFinalGeneList(allGenes);
        sampleData.setGenes(genes);
        final List<VariantEvaluation> variants = getFinalVariantList(variantEvaluations);
        sampleData.setVariantEvaluations(variants);

        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());
        logger.info("Analysed {} genes containing {} filtered variants", genes.size(), variantEvaluations.size());

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
    }

    private void assignVariantsToGenes(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            Gene gene = allGenes.get(variantEvaluation.getGeneSymbol());
            gene.addVariant(variantEvaluation);
        }
    }

    private List<VariantEvaluation> loadAndFilterVariants(Path vcfPath, Map<String, Gene> allGenes, List<AnalysisStep> analysisGroup) {
        Predicate<VariantEvaluation> geneFilterPredicate = geneFilterPredicate(allGenes,geneReassigner);
        List<VariantFilter> variantFilters = getVariantFilterSteps(analysisGroup);
        Predicate<VariantEvaluation> variantFilterPredicate = variantFilterPredicate(variantFilters);

        return loadVariants(vcfPath, allGenes, geneFilterPredicate, variantFilterPredicate);
    }

    /**
     *
     */
    protected Predicate<VariantEvaluation> variantFilterPredicate(List<VariantFilter> variantFilters) {
        return variantEvaluation -> {
            //loop through the filters and run them over the variantEvaluation according to the variantFilterRunner behaviour
            variantFilters.stream()
                    .forEach(filter -> variantFilterRunner.run(filter, variantEvaluation));
            return true;
        };
    }

    protected Predicate<VariantEvaluation> geneFilterPredicate(Map<String, Gene> genes, GeneReassigner geneReassigner) {
        return variantEvaluation -> genes.containsKey(variantEvaluation.getGeneSymbol());
    }

    /**
     *
     * @param passedGenes
     * @return
     */
    protected List<Gene> getFinalGeneList(Map<String, Gene> passedGenes) {
        return passedGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .collect(toList());
    }

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

    private List<List<AnalysisStep>> groupAnalysisStepsByFunction(List<AnalysisStep> analysisSteps) {
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

    private List<VariantEvaluation> loadVariants(Path vcfPath, Map<String, Gene> genes, Predicate<VariantEvaluation> geneFilterPredicate, Predicate<VariantEvaluation> variantFilterPredicate) {

        final int[] streamed = {0};
        final int[] passed = {0};

        VariantFactory variantFactory = sampleDataFactory.getVariantFactory();
        List<VariantEvaluation> variantEvaluations;
        try (Stream<VariantEvaluation> variantEvaluationStream = variantFactory.streamVariantEvaluations(vcfPath)) {
            //WARNING!!! THIS IS NOT THREADSAFE DO NOT USE PARALLEL STREAMS
            variantEvaluations = variantEvaluationStream
                    .map(variantEvaluation -> {
                        //yep, logging logic
                        streamed[0]++;
                        if (streamed[0] % 100000 == 0) {
                            logger.info("Loaded {} variants - {} passed variant filters", streamed[0], passed[0]);
                        }
                        return variantEvaluation;
                    })
                    .filter(geneFilterPredicate)
                    .filter(variantFilterPredicate)
                    .map(variantEvaluation -> {
                        if (variantEvaluation.passedFilters()) {
                            //more logging logic
                            passed[0]++;
                        }
                        return variantEvaluation;
                    })
                    .collect(toList());
        }
        logger.info("Loaded {} variants - {} passed variant filters", streamed[0], passed[0]);
        return variantEvaluations;
    }

    private List<VariantFilter> getVariantFilterSteps(List<AnalysisStep> analysisSteps) {
        logger.info("Filtering variants with:");
        return analysisSteps
                .stream()
                .filter(analysisStep -> (analysisStep.isVariantFilter()))
                .map(analysisStep -> {
                    logger.info("{}", analysisStep);
                    return (VariantFilter) analysisStep;
                })
                .collect(toList());
    }

    private SampleData makeSampleDataWithoutGenesOrVariants(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleDataWithoutVariantsOrGenes(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
    }

    private void runSteps(List<AnalysisStep> analysisSteps, List<Gene> genes, Pedigree pedigree, ModeOfInheritance modeOfInheritance) {
        boolean inheritanceModesCalculated = false;
        for (AnalysisStep analysisStep : analysisSteps) {
            if (!inheritanceModesCalculated && analysisStep.isInheritanceModeDependent()) {
                analyseGeneInheritanceMode(genes, pedigree, modeOfInheritance);
                inheritanceModesCalculated = true;
            }
            runStep(analysisStep, genes);
        }
    }

    //TODO: would this be better using the Visitor pattern?
    private void runStep(AnalysisStep analysisStep, List<Gene> genes) {
        if (analysisStep.isVariantFilter()) {
            VariantFilter filter = (VariantFilter) analysisStep;
            logger.info("Running VariantFilter: {}", filter);
            for (Gene gene : genes) {
                variantFilterRunner.run(filter, gene.getVariantEvaluations());
            }
            return;

        }
        if (GeneFilter.class
                .isInstance(analysisStep)) {
            GeneFilter filter = (GeneFilter) analysisStep;

            logger.info(
                    "Running GeneFilter: {}", filter);
            geneFilterRunner.run(filter, genes);

            return;
        }

        if (Prioritiser.class
                .isInstance(analysisStep)) {
            Prioritiser prioritiser = (Prioritiser) analysisStep;
            if (prioritiser.getPriorityType() != PriorityType.OMIM_PRIORITY){
                mainPriorityType = prioritiser.getPriorityType();
            }
            logger.info(
                    "Running Prioritiser: {}", prioritiser);
            prioritiserRunner.run(prioritiser, genes);
        }
    }

    private void analyseGeneInheritanceMode(Collection<Gene> genes, Pedigree pedigree, ModeOfInheritance modeOfInheritance) {
        //TODO could this be a VariantDataProvider?
        logger.info("Checking compatibility with {} inheritance mode for genes which passed filters", modeOfInheritance);
        //check the inheritance mode for the genes
        InheritanceCompatibilityChecker inheritanceCompatibilityChecker = new InheritanceCompatibilityChecker.Builder().pedigree(pedigree).addMode(modeOfInheritance).build();

        genes.stream().filter(Gene::passedFilters).forEach(gene -> {
            checkInheritanceCompatibilityOfVariants(gene, modeOfInheritance, inheritanceCompatibilityChecker);
        });

    }

    private void checkInheritanceCompatibilityOfVariants(Gene gene, ModeOfInheritance modeOfInheritance, InheritanceCompatibilityChecker inheritanceCompatibilityChecker) {
        if (modeOfInheritance == ModeOfInheritance.UNINITIALIZED) {
            return;
        }
        Multimap<String, VariantEvaluation> geneVariants = ArrayListMultimap.create();
        for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
            geneVariants.put(variantEvaluation.getVariantContext().toStringWithoutGenotypes(), variantEvaluation);
        }
        List<VariantContext> compatibleVariants = getVariantsCompatibleWithInheritanceMode(inheritanceCompatibilityChecker, gene);

        if (!compatibleVariants.isEmpty()) {
            logger.debug("Gene {} has {} variants compatible with {}:", gene.getGeneSymbol(), compatibleVariants.size(), modeOfInheritance);
            gene.setInheritanceModes(inheritanceCompatibilityChecker.getInheritanceModes());
            for (VariantContext compatibleVariantContext : compatibleVariants) {
                //using toStringWithoutGenotypes as the genotype string gets changed 
                Collection<VariantEvaluation> variants = geneVariants.get(compatibleVariantContext.toStringWithoutGenotypes());
                for (VariantEvaluation variant : variants) {
                    variant.setInheritanceModes(EnumSet.of(modeOfInheritance));
                    logger.debug("{}: {}", variant.getInheritanceModes(), variant);
                }
            }
        }
    }

    private List<VariantContext> getVariantsCompatibleWithInheritanceMode(InheritanceCompatibilityChecker inheritanceCompatibilityChecker, Gene gene) {
        List<VariantContext> compatibleVariants = new ArrayList<>();
        //This needs to be done using all the variants in the gene in order to be able to check for compound heterozygous variations
        //otherwise it would be simpler to just call this on each variant in turn
        try {
            //Make sure only ONE variantContext is added if there are multiple alleles as there will be one VariantEvaluation per allele.
            //Having multiple copies of a VariantContext might cause problems with the comp het calculations 
            Set<VariantContext> geneVariants = gene.getVariantEvaluations().stream().map(VariantEvaluation::getVariantContext).collect(toSet());
            compatibleVariants = inheritanceCompatibilityChecker.getCompatibleWith(new ArrayList<>(geneVariants));
        } catch (InheritanceCompatibilityCheckerException ex) {
            logger.error(null, ex);
        }
        return compatibleVariants;
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

}
