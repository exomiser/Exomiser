/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Factory class for handling creation of FilterType objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class PriorityFactory {

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactory.class);

    @Autowired
    private DataSource dataSource;
    @Autowired
    @Lazy
    private DataMatrix randomWalkMatrix;
    @Autowired
    private Path phenomizerDataDirectory;
    
    public List<Priority> makePrioritisers(ExomiserSettings exomiserSettings) {
        
        String disease = exomiserSettings.getDiseaseId();
        String candidateGene = exomiserSettings.getCandidateGene();
        List<String> hpoIds = exomiserSettings.getHpoIds();
        List<Integer> entrezSeedGenes = exomiserSettings.getSeedGeneList();
        String exomiser2Params = exomiserSettings.getExomiser2Params();
        List<Priority> genePriorityList = new ArrayList<>();
        //TODO: OmimPrioritizer is specified implicitly - perhaps they should be different types of ExomiserSettings?
        //probably better as a specific type of Exomiser - either a RareDiseaseExomiser or DefaultExomiser. These might be badly named as the OMIM proritiser is currently the default.
        genePriorityList.add(getOmimPrioritizer());
        
        switch (exomiserSettings.getPrioritiserType()) {
            case PHENIX_PRIORITY:
                genePriorityList.add(getPhenixPrioritiser(hpoIds));
                break;
            case HI_PHIVE_PRIORITY:
                genePriorityList.add(getHiPhivePrioritiser(hpoIds, candidateGene, disease, exomiser2Params));
                break;  
            case PHIVE_PRIORITY:
                genePriorityList.add(getPhivePrioritiser(hpoIds,disease));
                break;
            case EXOMEWALKER_PRIORITY:
                genePriorityList.add(getExomeWalkerPrioritiser(entrezSeedGenes));
                break;                      
        }

        return genePriorityList;
    }

    public Priority getOmimPrioritizer() {
        OMIMPriority priority = new OMIMPriority();
        priority.setDataSource(dataSource);
        logger.info("Made new OMIM Priority: {}", priority);
        return priority;
    }

    public Priority getPhenixPrioritiser(List<String> hpoIds) {
        Set<String> hpoIDset = new HashSet<>();
        hpoIDset.addAll(hpoIds);
        
        boolean symmetric = false;
        Priority priority = new PhenixPriority(phenomizerDataDirectory.toString(), hpoIDset, symmetric);
        logger.info("Made new Phenomizer Priority: {}", priority);
        return priority;
    }

    public Priority getPhivePrioritiser(List<String> hpoIds,String disease) {
        PhivePriority priority = new PhivePriority(hpoIds,disease);
        priority.setDataSource(dataSource);
        logger.info("Made new PHIVE Priority: {}", priority);
        return priority;
    }

    public Priority getExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        Priority priority = new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
        logger.info("Made new GeneWanderer Priority: {}", priority);
        return priority;
    }
    
    public Priority getHiPhivePrioritiser(List<String> hpoIds, String candGene, String disease, String hiPhiveParams) {
        HiPhivePriority priority = new HiPhivePriority(hpoIds, candGene, disease, hiPhiveParams, randomWalkMatrix);
        priority.setDataSource(dataSource);
        logger.info("Made new HiPHIVE Priority: {}", priority);
        return priority;
    }

}
