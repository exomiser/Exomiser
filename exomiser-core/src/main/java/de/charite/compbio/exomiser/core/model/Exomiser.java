/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataFactory;
import de.charite.compbio.exomiser.core.filter.Filter;
import de.charite.compbio.exomiser.core.filter.FilterFactory;
import de.charite.compbio.exomiser.core.filter.SimpleVariantFilterRunner;
import de.charite.compbio.exomiser.core.filter.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filter.FilterRunner;
import de.charite.compbio.exomiser.core.filter.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import de.charite.compbio.exomiser.priority.GenePrioritiser;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.ScoringMode;
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
    private FilterFactory filterFactory;
    @Autowired
    private PriorityFactory priorityFactory;
    @Autowired
    private VariantEvaluationDataFactory variantEvaluationFactory;
    @Autowired
    private SparseVariantFilterRunner sparseVariantFilterRunner;
            
    public void analyse(SampleData sampleData, ExomiserSettings exomiserSettings) {

        logger.info("MAKING FILTERS");
        List<Filter> variantFilters = filterFactory.makeVariantFilters(exomiserSettings);
        List<Filter> geneFilters = filterFactory.makeGeneFilters(exomiserSettings);

        logger.info("MAKING PRIORITISERS");
        List<Priority> priorityList = priorityFactory.makePrioritisers(exomiserSettings);

        FilterRunner variantFilterRunner;
        logger.info("FILTERING VARIANTS");
        if (exomiserSettings.runFullAnalysis()) {
            setVariantFrequencyAndPathogenicityData(sampleData.getVariantEvaluations());
            variantFilterRunner = new SimpleVariantFilterRunner();
        } else {
            //the sparseVariantFilterer will handle getting data when it needs it.
            variantFilterRunner = sparseVariantFilterRunner;
        }
        variantFilterRunner.run(variantFilters, sampleData.getVariantEvaluations());

        if (!geneFilters.isEmpty()) {
            logger.info("FILTERING GENES");
            calculateInheritanceModesForGenesWhichPassedFilters(sampleData);

            //Filter the resulting Genes for their inheritance mode
            FilterRunner geneFilterRunner = new SimpleGeneFilterRunner();
            geneFilterRunner.run(geneFilters, sampleData.getGenes());
        }
        

        logger.info("PRIORITISING GENES");
        //for VCF we need the priority scores for all genes, even those with no passed
        //variants. For other output formats we only need to do if for genes with at
        //least one passed variant and this is much faster
        if (exomiserSettings.getOutputFormats().contains(OutputFormat.VCF)) {
            GenePrioritiser.prioritiseGenes(priorityList, sampleData.getGenes());
        } else {
            GenePrioritiser.prioritiseFilteredGenes(priorityList, sampleData.getGenes());
        }
        logger.info("SCORING GENES");
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        PriorityType prioriserType = exomiserSettings.getPrioritiserType();
        ScoringMode scoreMode = prioriserType.getScoringMode();

        GeneScorer.scoreGenes(sampleData.getGenes(), exomiserSettings.getModeOfInheritance(), sampleData.getPedigree(), scoreMode);
    }

    private void setVariantFrequencyAndPathogenicityData(List<VariantEvaluation> variantEvaluations) {
        logger.info("Setting variant frequency and pathogenicity data");
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            variantEvaluationFactory.addFrequencyData(variantEvaluation);
            variantEvaluationFactory.addPathogenicityData(variantEvaluation);
        }
    }

    private void calculateInheritanceModesForGenesWhichPassedFilters(SampleData sampleData) {
        logger.info("Calculating inheritance mode for genes which passed filters");
        //check the inheritance mode for the genes
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser(sampleData.getPedigree());
                
        for (Gene gene : sampleData.getGenes()) {
            if (gene.passesFilters()) {
                gene.setInheritanceModes(inheritanceModeAnalyser.analyseInheritanceModesForGene(gene));
            }          
        }
    }
}
