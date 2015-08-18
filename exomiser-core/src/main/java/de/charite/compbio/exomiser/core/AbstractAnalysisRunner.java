package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserRunner;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.util.RankBasedGeneScorer;
import de.charite.compbio.exomiser.core.util.RawScoreGeneScorer;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    protected final SampleDataFactory sampleDataFactory;
    protected final VariantFilterRunner variantFilterRunner;
    protected final GeneFilterRunner geneFilterRunner;
    protected final PrioritiserRunner prioritiserRunner;

    public AbstractAnalysisRunner(SampleDataFactory sampleDataFactory, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.sampleDataFactory = sampleDataFactory;
        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
        this.prioritiserRunner = new PrioritiserRunner();
    }

    @Override
    public void runAnalysis(Analysis analysis) {
        long startSampleDataTimeMillis = System.currentTimeMillis();

        final SampleData sampleData = makeSampleData(analysis);
        Map<String, Gene> knownGenes = makeKnownGenes();

        long endSampleDataTimeMillis = System.currentTimeMillis();
        double sampleDataTimeSecs = (double) (endSampleDataTimeMillis - startSampleDataTimeMillis) / 1000;
        //TODO this should become irrelvant as ideally we should be able to stream and filter all variants in one go 
        logger.info("Finished creating sample data in {} secs", sampleDataTimeSecs);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        final Path vcfPath = analysis.getVcfPath();
        final List<Gene> genes = sampleData.getGenes();
        final Pedigree pedigree = sampleData.getPedigree();
        //TODO: check AnalysisStep order, warn if wrong, re-order, then runSteps
        //TODO: can runSteps be merged such that it copes with all analysis scenarios?
        runSteps(vcfPath, analysis.getAnalysisSteps(), genes, pedigree);

        logger.info(logPassed(genes));
        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
    }

    /**
     * @return a map of genes indexed by gene symbol.
     */
    protected Map<String, Gene> makeKnownGenes() {
        return sampleDataFactory.createKnownGenes()
                .parallelStream()
                .collect(toConcurrentMap(Gene::getGeneSymbol, gene -> gene));
    }

    private String logPassed(List<Gene> genes) {
        int filteredGenes = 0;
        int filteredVariants = 0;
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                filteredGenes++;
                filteredVariants += gene.getPassedVariantEvaluations().size();
            }
        }
        return String.format("Filtered %d genes containing %d filtered variants", filteredGenes, filteredVariants);
    }

    private SampleData makeSampleData(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleData(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
    }

    //TODO: or run as groups of steps?
    //List<AnalysisStep> -> AnalysisSteps Iterable
    protected void runSteps(Path vcfPath, List<AnalysisStep> analysisSteps, List<Gene> genes, Pedigree pedigree) {
        boolean inheritanceModesCalculated = false;
        boolean variantsLoaded = false;
        for (AnalysisStep analysisStep : analysisSteps) {
            if (!inheritanceModesCalculated && analysisStep.isInheritanceModeDependent()) {
                analyseGeneInheritanceModes(genes, pedigree);
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
        if (GeneFilter.class.isInstance(analysisStep)) {
            GeneFilter filter = (GeneFilter) analysisStep;
            logger.info("Running GeneFilter: {}", filter);
            geneFilterRunner.run(filter, genes);
            return;
        }
        if (Prioritiser.class.isInstance(analysisStep)) {
            Prioritiser prioritiser = (Prioritiser) analysisStep;
            logger.info("Running Prioritiser: {}", prioritiser);
            prioritiserRunner.run(prioritiser, genes);
        }
    }

    private void analyseGeneInheritanceModes(Collection<Gene> genes, Pedigree pedigree) {
        logger.info("Calculating inheritance modes for genes which passed filters");
        //check the inheritance mode for the genes
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser();
        genes.stream().filter(Gene::passedFilters).forEach(gene -> {
            Set<ModeOfInheritance> inheritanceModes = inheritanceModeAnalyser.analyseInheritanceModes(gene, pedigree);
            gene.setInheritanceModes(inheritanceModes);
        });
    }

    protected void scoreGenes(List<Gene> genes, ScoringMode scoreMode, ModeOfInheritance modeOfInheritance) {
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

    protected Map<String, Gene> getPassedGenes(Map<String, Gene> genes) {
        Map<String, Gene> passedGenes = genes.values()
                .parallelStream()
                .filter(Gene::passedFilters)
                .collect(toConcurrentMap(Gene::getGeneSymbol, gene -> (gene)));
        logger.info("Filtered {} genes - {} passed", genes.size(), passedGenes.size());
        return passedGenes;
    }

    protected List<Gene> getGenesWithVariants(Map<String, Gene> passedGenes) {
        return passedGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .collect(toList());
    }

    protected List<VariantEvaluation> getVariantsFromGenes(List<Gene> genes) {
        return genes
                .stream()
                .flatMap(gene -> (gene.getVariantEvaluations().stream()))
                .collect(toList());
    }
}
