/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.FilterRunner;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserRunner;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisRunner.class);

    private final VariantDataService variantDataService;

    //TODO: variantFilterRunner is not typed here as there is something a bit mis-aligned with the interfaces and what they can work on - this needs fixing
    //probably by removing the FilterRunner interface and just using the VariantFilterRunner and GeneFilterRunner interfaces VariantFilterRunners can then 
    //also implement the GeneFilterRunner interface which is what we need here but type erasure is preventing this from being possible.
    private final FilterRunner variantFilterRunner;
    private final FilterRunner<GeneFilter, Gene> geneFilterRunner = new SimpleGeneFilterRunner();
    private final PrioritiserRunner prioritiserRunner = new PrioritiserRunner();

    public AnalysisRunner(VariantDataService variantDataService, ExomiserSettings settings) {
        this.variantDataService = variantDataService;
        this.variantFilterRunner = makeFilterRunner(settings.runFullAnalysis());
    }

    private FilterRunner makeFilterRunner(boolean runFullAnalysis) {
        //the VariantFilterRunner will handle getting variant data when it needs it.
        //perhaps this ought to be two classes? A SimpleAnalysisRunner and a SparseAnalysisRunner?
        if (runFullAnalysis) {
            return new SimpleVariantFilterRunner(variantDataService);
        } else {
            return new SparseVariantFilterRunner(variantDataService);
        }
    }

    public void runAnalysis(Analysis analysis) {
        final SampleData sampleData = analysis.getSampleData();
        final ExomiserSettings settings = analysis.getSettings();
        final List<Gene> genes = sampleData.getGenes();
        final Pedigree pedigree = sampleData.getPedigree();
        boolean inheritanceModesCalculated = false;

        logger.info("Running analysis on {} genes ({} variants)", genes.size(), sampleData.getVariantEvaluations().size());
        long startTimeinMillis = System.currentTimeMillis();

        for (AnalysisStep analysisStep : analysis.getAnalysisSteps()) {
            if (!inheritanceModesCalculated && requiresInheritanceModes(analysisStep)) {
                analyseGeneInheritanceModes(pedigree, genes);
                inheritanceModesCalculated = true;
            }
            runStep(analysisStep, genes);
        }
        scoreGenes(settings, genes);

        long endTimeinMillis = System.currentTimeMillis();
        double analysisTimeInSecs = (double) (endTimeinMillis - startTimeinMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeInSecs);
    }

    public void runStep(AnalysisStep analysisStep, List<Gene> genes) {
        if (VariantFilter.class.isInstance(analysisStep)) {
            VariantFilter filter = (VariantFilter) analysisStep;
            logger.info("Running VariantFilter: {}", filter);
            for (Gene gene : genes) {
                if (gene.passedFilters()) {
                    variantFilterRunner.run(filter, gene.getVariantEvaluations());
                }
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

    private void analyseGeneInheritanceModes(Pedigree pedigree, List<Gene> genes) {
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

    private void scoreGenes(ExomiserSettings exomiserSettings, List<Gene> genes) {
        logger.info("Scoring genes");
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        PriorityType prioritiserType = exomiserSettings.getPrioritiserType();
        ScoringMode scoreMode = prioritiserType.getScoringMode();

        GeneScorer.scoreGenes(genes, exomiserSettings.getModeOfInheritance(), scoreMode);
    }
}
