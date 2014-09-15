/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataFactory;
import de.charite.compbio.exomiser.core.filter.Filter;
import de.charite.compbio.exomiser.core.filter.FilterFactory;
import de.charite.compbio.exomiser.core.filter.SimpleVariantFilterer;
import de.charite.compbio.exomiser.core.filter.SparseVariantFilterer;
import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.InheritanceFilterScore;
import de.charite.compbio.exomiser.core.filter.VariantFilterer;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import de.charite.compbio.exomiser.priority.GenePrioritiser;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.ScoringMode;
import jannovar.common.ModeOfInheritance;
import java.util.List;
import java.util.Set;
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
    private SparseVariantFilterer sparseVariantFilterer;
            
    public void analyse(SampleData sampleData, ExomiserSettings exomiserSettings) {

        logger.info("MAKING FILTERS");
        List<Filter> filterList = filterFactory.makeFilters(exomiserSettings);

        //create the priority factory - this will deliberately fail if there are
        //incorrect input options for the specified prioritiser. 
        logger.info("MAKING PRIORITISERS");
        List<Priority> priorityList = priorityFactory.makePrioritisers(exomiserSettings);

        VariantFilterer variantFilterer;
        logger.info("FILTERING VARIANTS");
        if (exomiserSettings.runFullAnalysis()) {
            setVariantFrequencyAndPathogenicityData(sampleData.getVariantEvaluations());
            variantFilterer = new SimpleVariantFilterer();
        } else {
            //the sparseVariantFilterer will handle getting data when it needs it.
            variantFilterer = sparseVariantFilterer;
        }
        variantFilterer.filterVariants(filterList, sampleData.getVariantEvaluations());

        //Filter the resulting Genes for their inheritance mode 
        filterGenesForInheritanceMode(exomiserSettings.getModeOfInheritance(), sampleData);

        logger.info("PRIORITISING GENES");
        //for VCF we need the priority scores for all genes, even those with no passed
        //variants. For other output formats we only need to do if for genes with at
        //least one passed variant and this is much faster
        if (exomiserSettings.getOutputFormats().contains(OutputFormat.VCF)) {
            GenePrioritiser.prioritiseGenes(priorityList, sampleData.getGeneList());
        } else {
            GenePrioritiser.prioritiseFilteredGenes(priorityList, sampleData.getGeneList());
        }
        logger.info("SCORING GENES");
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        PriorityType prioriserType = exomiserSettings.getPrioritiserType();
        ScoringMode scoreMode = prioriserType.getScoringMode();

        GeneScorer.scoreGenes(sampleData.getGeneList(), exomiserSettings.getModeOfInheritance(), sampleData.getPedigree(), scoreMode);
    }

    private void setVariantFrequencyAndPathogenicityData(List<VariantEvaluation> variantEvaluations) {
        logger.info("Setting variant frequency and pathogenicity data");
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            variantEvaluationFactory.addFrequencyData(variantEvaluation);
            variantEvaluationFactory.addPathogenicityData(variantEvaluation);
        }
    }

    //TODO: move into a GeneFilter class
    private void filterGenesForInheritanceMode(ModeOfInheritance modeOfInheritance, SampleData sampleData) {
        logger.info("Calculating inheritance mode for genes which passed filters");
        //check the inheritance mode for the genes
        InheritanceModeAnalyser inheritanceModeAnalyser = new InheritanceModeAnalyser(sampleData.getPedigree());
        //Yuk - this ought to go into a GeneFilter - Damian made me do it.
        //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
        FilterScore passedScore = new InheritanceFilterScore(1f);
        FilterScore failedScore = new InheritanceFilterScore(0f);
        
        for (Gene gene : sampleData.getGeneList()) {
            if (!gene.passesFilters()) {
                continue;
            } 
            Set<ModeOfInheritance> compatibleInheritanceModesForGene = inheritanceModeAnalyser.analyseInheritanceModesForGene(gene);
            gene.setInheritanceModes(compatibleInheritanceModesForGene);
            //now do the gene filtering once we have the compatible modes of inheritance    
            filterGeneForInheritanceMode(modeOfInheritance, gene, passedScore, failedScore);
            
        }
    }

    
    private void filterGeneForInheritanceMode(ModeOfInheritance modeOfInheritance, Gene gene, FilterScore passedScore, FilterScore failedScore) {
        
        if (modeOfInheritance == ModeOfInheritance.UNINITIALIZED) {
            //don't do this if ModeOfInheritance.UNINITIALIZED as otherwise the filter will fail erroneously.
            return;
        }
        
        //set the filter scores for the variant evaluations
        if (gene.isConsistentWith(modeOfInheritance)) {
            //yay we're compatible!
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                variantEvaluation.addPassedFilter(FilterType.INHERITANCE_FILTER, passedScore);
            }
        } else {
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                variantEvaluation.addFailedFilter(FilterType.INHERITANCE_FILTER, failedScore);
            }
        }
    }

}
