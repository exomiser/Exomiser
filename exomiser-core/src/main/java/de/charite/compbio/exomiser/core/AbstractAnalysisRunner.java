package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
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

import java.util.List;
import java.util.Set;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAnalysisRunner.class);

    protected final SampleDataFactory sampleDataFactory;
    protected final VariantFilterRunner variantFilterRunner;
    protected final GeneFilterRunner geneFilterRunner;
    protected final PrioritiserRunner prioritiserRunner;

    public AbstractAnalysisRunner(VariantFactory variantFactory, VariantFilterRunner variantFilterRunner, GeneFilterRunner geneFilterRunner) {
        this.sampleDataFactory = new SampleDataFactory(variantFactory);
        this.variantFilterRunner = variantFilterRunner;
        this.geneFilterRunner = geneFilterRunner;
        this.prioritiserRunner = new PrioritiserRunner();
    }

    @Override
    public void runAnalysis(Analysis analysis) {
        long startSampleDataTimeMillis = System.currentTimeMillis();

        final SampleData sampleData = makeSampleData(analysis);

        long endSampleDataTimeMillis = System.currentTimeMillis();
        double sampleDataTimeSecs = (double) (endSampleDataTimeMillis - startSampleDataTimeMillis) / 1000;
        logger.info("Finished creating sample data in {} secs", sampleDataTimeSecs);

        final List<Gene> genes = sampleData.getGenes();
        final Pedigree pedigree = sampleData.getPedigree();
        logger.info("Running analysis on sample: {} ({} genes, {} variants)", sampleData.getSampleNames(), genes.size(), sampleData.getVariantEvaluations().size());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        runSteps(analysis.getAnalysisSteps(), genes, pedigree);
        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
        logger.info("Total sample data and analysis time: {} secs", sampleDataTimeSecs + analysisTimeSecs);
    }

    private SampleData makeSampleData(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleData(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
    }

    protected void runSteps(List<AnalysisStep> analysisSteps, List<Gene> genes, Pedigree pedigree) {
        boolean inheritanceModesCalculated = false;
        for (AnalysisStep analysisStep : analysisSteps) {
            if (!inheritanceModesCalculated && requiresInheritanceModes(analysisStep)) {
                analyseGeneInheritanceModes(genes, pedigree);
                inheritanceModesCalculated = true;
            }
            runStep(analysisStep, genes);
        }
    }

    //TODO: would this be better using the Visitor pattern?
    private void runStep(AnalysisStep analysisStep, List<Gene> genes) {
        if (VariantFilter.class.isInstance(analysisStep)) {
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

    private boolean requiresInheritanceModes(AnalysisStep analysisStep) {
        if (GeneFilter.class.isInstance(analysisStep)) {
            GeneFilter filter = (GeneFilter) analysisStep;
            return (filter.getFilterType() == FilterType.INHERITANCE_FILTER);
        }
        if (Prioritiser.class.isInstance(analysisStep)) {
            Prioritiser prioritiser = (Prioritiser) analysisStep;
            return (prioritiser.getPriorityType() == PriorityType.OMIM_PRIORITY);
        }
        return false;
    }

    private void analyseGeneInheritanceModes(List<Gene> genes, Pedigree pedigree) {
        logger.info("Calculating inheritance modes for genes which passed filters");
        //check the inheritance mode for the genes
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser();
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                Set<ModeOfInheritance> inheritanceModes = inheritanceModeAnalyser.analyseInheritanceModes(gene, pedigree);
                gene.setInheritanceModes(inheritanceModes);
            }
        }
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
}
