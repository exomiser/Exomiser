/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filter.Filter;
import de.charite.compbio.exomiser.core.filter.FilterFactory;
import de.charite.compbio.exomiser.core.filter.VariantFilterer;
import de.charite.compbio.exomiser.priority.GenePrioritiser;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.ScoringMode;
import de.charite.compbio.exomiser.core.util.GeneScorer;
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
    FilterFactory filterFactory;
    @Autowired
    PriorityFactory priorityFactory;

    
    public void analyse(SampleData sampleData, ExomiserSettings exomiserSettings) {
        
        logger.info("MAKING FILTERS");
        List<Filter> filterList = filterFactory.makeFilters(exomiserSettings);
        
        logger.info("FILTERING VARIANTS");
        VariantFilterer.useNonDestructiveFiltering(filterList, sampleData.getVariantEvaluations());
            
        //create the priority factory - this will deliberately fail if there are
        //incorrect input options for the specified prioritiser. 
        logger.info("MAKING PRIORITISERS");
        List<Priority> priorityList = priorityFactory.makePrioritisers(exomiserSettings);
             
        logger.info("PRIORITISING GENES");        
        //this is currently handled by Prioritiser but should probably be part of an Exomiser - 
        //e.g. RareDiseaseExomiser would OMIMPriority by default (the current situation)
        GenePrioritiser.prioritiseFilteredGenes(priorityList, sampleData.getGeneList());
        
        logger.info("SCORING GENES");        
        //prioritser needs to provide the mode of scoring it requires. Mostly it is RAW_SCORE.
        //Either RANK_BASED or RAW_SCORE
        PriorityType prioriserType = exomiserSettings.getPrioritiserType();
        ScoringMode scoreMode = prioriserType.getScoringMode();
        
        GeneScorer.scoreGenes(sampleData.getGeneList(), exomiserSettings.getModeOfInheritance(), sampleData.getPedigree(), scoreMode);
    }
    
}
