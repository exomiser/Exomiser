/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.FilterFactory;
import de.charite.compbio.exomiser.core.filters.SimpleVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.FilterRunner;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.prioritisers.GenePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main class for analysing variant data. This will orchestrate the set-up of
 * Filters and Priotitisers according to the supplied settings and then apply
 * them to the data.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class Exomiser {

    private static final Logger logger = LoggerFactory.getLogger(Exomiser.class);

    @Autowired
    private PriorityFactory priorityFactory;
    @Autowired
    private VariantDataService variantEvaluationFactory;
    //TODO: might be better using constructor injection to supply the  VariantDataService to the SparseVariantFilterRunner
    @Autowired
    private SparseVariantFilterRunner sparseVariantFilterRunner;

    //TODO: this could be made more programmatically configuarable by also allowing
    //the Filters and Prioritisers be passsed in / set so as to remove the dependency on ExomiserSettings
    public void analyse(SampleData sampleData, ExomiserSettings exomiserSettings) {
        logger.info("STARTING ANALYSIS...");
        //don't change the order here - variants should ALWAYS be filtered before
        //genes otherwise the inheritance mode will break leading to altered
        //predictions downstream.
        FilterFactory filterFactory = new FilterFactory();
        logger.info("MAKING VARIANT FILTERS");
        List<VariantFilter> variantFilters = filterFactory.makeVariantFilters(exomiserSettings);
        runVariantFilters(variantFilters, exomiserSettings, sampleData);

        logger.info("MAKING GENE FILTERS");
        List<GeneFilter> geneFilters = filterFactory.makeGeneFilters(exomiserSettings);
        runGeneFilters(geneFilters, sampleData);

        //Prioritisers should ALWAYS run last.
        logger.info("MAKING PRIORITISERS");
        List<Prioritiser> priorityList = priorityFactory.makePrioritisers(exomiserSettings);
        runPrioritisers(priorityList, exomiserSettings, sampleData);
        
        scoreGenes(exomiserSettings, sampleData);
        logger.info("FINISHED ANALYSIS");
    }

    private void runVariantFilters(List<VariantFilter> variantFilters, ExomiserSettings exomiserSettings, SampleData sampleData) {
        logger.info("FILTERING VARIANTS");
        FilterRunner variantFilterRunner = prepareFilterRunner(exomiserSettings, sampleData);
        variantFilterRunner.run(variantFilters, sampleData.getVariantEvaluations());
    }

    private FilterRunner prepareFilterRunner(ExomiserSettings exomiserSettings, SampleData sampleData) {
        FilterRunner variantFilterRunner = new SimpleVariantFilterRunner();
        if (exomiserSettings.runFullAnalysis()) {
            setVariantFrequencyAndPathogenicityData(sampleData.getVariantEvaluations());
        } else {
            //the sparseVariantFilterer will handle getting data when it needs it.
            variantFilterRunner = sparseVariantFilterRunner;
        }
        return variantFilterRunner;
    }

    private void setVariantFrequencyAndPathogenicityData(List<VariantEvaluation> variantEvaluations) {
        logger.info("Setting variant frequency and pathogenicity data");
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            variantEvaluationFactory.setVariantFrequencyAndPathogenicityData(variantEvaluation);
        }
    }

    private void runGeneFilters(List<GeneFilter> geneFilters, SampleData sampleData) {
        // this is needed even if we don't have an inheritance gene filter set as the OMIM prioritiser relies on it
        calculateInheritanceModesForGenesWhichPassedFilters(sampleData);
        if (!geneFilters.isEmpty()) {
            logger.info("FILTERING GENES");
            //Filter the resulting Genes for their inheritance mode
            FilterRunner geneFilterRunner = new SimpleGeneFilterRunner();
            geneFilterRunner.run(geneFilters, sampleData.getGenes());
        }
    }

    private void calculateInheritanceModesForGenesWhichPassedFilters(SampleData sampleData) {
        logger.info("Calculating inheritance mode for genes which passed filters");
        //check the inheritance mode for the genes
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser(sampleData.getPedigree());

        for (Gene gene : sampleData.getGenes()) {
            if (gene.passedFilters()) {
                gene.setInheritanceModes(inheritanceModeAnalyser.analyseInheritanceModesForGene(gene));
            }
        }
    }

    private void runPrioritisers(List<Prioritiser> priorityList, ExomiserSettings exomiserSettings, SampleData sampleData) {
        logger.info("PRIORITISING GENES");
        //for VCF we need the priority scores for all genes, even those with no passed
        //variants. For other output formats we only need to do if for genes with at
        //least one passed variant and this is much faster
        if (exomiserSettings.getOutputFormats().contains(OutputFormat.VCF)) {
            GenePrioritiser.prioritiseGenes(priorityList, sampleData.getGenes());
        } else {
            GenePrioritiser.prioritiseFilteredGenes(priorityList, sampleData.getGenes());
        }
    }

    private void scoreGenes(ExomiserSettings exomiserSettings, SampleData sampleData) {
        logger.info("SCORING GENES");
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        PriorityType prioritiserType = exomiserSettings.getPrioritiserType();
        ScoringMode scoreMode = prioritiserType.getScoringMode();

        GeneScorer.scoreGenes(sampleData.getGenes(), exomiserSettings.getModeOfInheritance(), scoreMode);
    }

}
