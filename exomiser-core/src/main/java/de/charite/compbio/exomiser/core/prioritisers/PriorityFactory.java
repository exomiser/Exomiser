/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PrioritiserService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
    private PrioritiserService prioritiserService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    @Lazy
    private DataMatrix randomWalkMatrix;
    @Autowired
    private Path phenixDataDirectory;

    public List<Prioritiser> makePrioritisers(ExomiserSettings exomiserSettings) {

        String diseaseId = exomiserSettings.getDiseaseId();
        String candidateGene = exomiserSettings.getCandidateGene();
        List<String> hpoIds = exomiserSettings.getHpoIds();
        List<Integer> entrezSeedGenes = exomiserSettings.getSeedGeneList();
        String exomiser2Params = exomiserSettings.getExomiser2Params();

        PriorityType priorityType = exomiserSettings.getPrioritiserType();
        if (priorityType == PriorityType.NONE) {
            return Collections.emptyList();
        }

        List<Prioritiser> genePriorityList = new ArrayList<>();
        //TODO: OmimPrioritizer is specified implicitly - perhaps they should be different types of ExomiserSettings?
        //probably better as a specific type of Exomiser - either a RareDiseaseExomiser or DefaultExomiser. These might be badly named as the OMIM proritiser is currently the default.
        //always run OMIM unless the user specified what they really don't want to run any prioritisers
        genePriorityList.add(getOmimPrioritizer());

        switch (priorityType) {
            case PHENIX_PRIORITY:
                genePriorityList.add(getPhenixPrioritiser(hpoIds));
                break;
            case HI_PHIVE_PRIORITY:
                hpoIds = addDiseasePhenotypeTermsIfHpoIdsIsEmpty(diseaseId, hpoIds);
                genePriorityList.add(getHiPhivePrioritiser(hpoIds, candidateGene, diseaseId, exomiser2Params));
                break;
            case PHIVE_PRIORITY:
                hpoIds = addDiseasePhenotypeTermsIfHpoIdsIsEmpty(diseaseId, hpoIds);
                genePriorityList.add(getPhivePrioritiser(hpoIds, diseaseId));
                break;
            case EXOMEWALKER_PRIORITY:
                genePriorityList.add(getExomeWalkerPrioritiser(entrezSeedGenes));
                break;
        }

        return genePriorityList;
    }

    private List<String> addDiseasePhenotypeTermsIfHpoIdsIsEmpty(String diseaseId, List<String> hpoIds) {
        if (hpoIds.isEmpty() && diseaseId != null && !diseaseId.isEmpty()) {
            logger.info("HPO terms have not been specified. Setting HPO IDs using disease annotations for {}", diseaseId);
            return prioritiserService.getHpoIdsForDiseaseId(diseaseId);
        }
        return hpoIds;
    }

    public OMIMPriority getOmimPrioritizer() {
        OMIMPriority priority = new OMIMPriority();
        priority.setDataSource(dataSource);
        logger.info("Made new OMIM Priority: {}", priority);
        return priority;
    }

    public PhenixPriority getPhenixPrioritiser(List<String> hpoIds) {
        Set<String> hpoIDset = new HashSet<>();
        hpoIDset.addAll(hpoIds);

        boolean symmetric = false;
        PhenixPriority priority = new PhenixPriority(phenixDataDirectory.toString(), hpoIDset, symmetric);
        logger.info("Made new PhenIX Priority: {}", priority);
        return priority;
    }

    public PhivePriority getPhivePrioritiser(List<String> hpoIds, String disease) {
        PhivePriority priority = new PhivePriority(hpoIds, disease);
        priority.setDataSource(dataSource);
        logger.info("Made new PHIVE Priority: {}", priority);
        return priority;
    }

    public ExomeWalkerPriority getExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        ExomeWalkerPriority priority = new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
        logger.info("Made new GeneWanderer Priority: {}", priority);
        return priority;
    }

    public HiPhivePriority getHiPhivePrioritiser(List<String> hpoIds, String candGene, String disease, String hiPhiveParams) {
        HiPhivePriority priority = new HiPhivePriority(hpoIds, candGene, disease, hiPhiveParams, randomWalkMatrix);
        priority.setDataSource(dataSource);
        logger.info("Made new HiPHIVE Priority: {}", priority);
        return priority;
    }

}
