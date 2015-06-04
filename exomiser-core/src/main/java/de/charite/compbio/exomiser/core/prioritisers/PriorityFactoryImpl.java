/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import java.nio.file.Path;
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
public class PriorityFactoryImpl implements PriorityFactory {

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactoryImpl.class);

    @Autowired
    private PriorityService priorityService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    @Lazy
    private DataMatrix randomWalkMatrix;
    @Autowired
    private Path phenixDataDirectory;

    /**
     * Returns a Prioritiser of the given type, ready to run according to the
     * settings provided. Will return a non-functional prioritiser in cases
     * where the type is not recognised.
     *
     * @param priorityType
     * @param settings
     * @return
     */
    @Override
    public Prioritiser makePrioritiser(PriorityType priorityType, ExomiserSettings settings) {
        //These should form the PrioritySettings interface
        String diseaseId = settings.getDiseaseId();
        String candidateGene = settings.getCandidateGene();
        List<String> hpoIds = settings.getHpoIds();
        List<Integer> entrezSeedGenes = settings.getSeedGeneList();
        String exomiser2Params = settings.getExomiser2Params();

        hpoIds = addDiseasePhenotypeTermsIfHpoIdsIsEmpty(diseaseId, hpoIds);

        switch (priorityType) {
            case OMIM_PRIORITY:
                return getOmimPrioritizer();
            case PHENIX_PRIORITY:
                return getPhenixPrioritiser(hpoIds);
            case HI_PHIVE_PRIORITY:
                return getHiPhivePrioritiser(hpoIds, candidateGene, diseaseId, exomiser2Params);
            case PHIVE_PRIORITY:
                return getPhivePrioritiser(hpoIds, diseaseId);
            case EXOMEWALKER_PRIORITY:
                return getExomeWalkerPrioritiser(entrezSeedGenes);
            case NONE:
            case NOT_SET:
                return new NoneTypePrioritiser();
            default:
                logger.warn("Prioritiser: '{}' not supported. Returning '{}' type", priorityType, PriorityType.NONE.getCommandLineValue());
                return new NoneTypePrioritiser();
        }

    }

    private List<String> addDiseasePhenotypeTermsIfHpoIdsIsEmpty(String diseaseId, List<String> hpoIds) {
        if (hpoIds.isEmpty() && diseaseId != null && !diseaseId.isEmpty()) {
            logger.info("HPO terms have not been specified. Setting HPO IDs using disease annotations for {}", diseaseId);
            return priorityService.getHpoIdsForDiseaseId(diseaseId);
        }
        return hpoIds;
    }

    private OMIMPriority getOmimPrioritizer() {
        OMIMPriority priority = new OMIMPriority();
        priority.setDataSource(dataSource);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

    private PhenixPriority getPhenixPrioritiser(List<String> hpoIds) {
        Set<String> hpoIDset = new HashSet<>();
        hpoIDset.addAll(hpoIds);

        boolean symmetric = false;
        PhenixPriority priority = new PhenixPriority(phenixDataDirectory.toString(), hpoIDset, symmetric);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

    private PhivePriority getPhivePrioritiser(List<String> hpoIds, String disease) {
        PhivePriority priority = new PhivePriority(hpoIds, disease);
        priority.setDataSource(dataSource);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

    private ExomeWalkerPriority getExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        ExomeWalkerPriority priority = new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

    private HiPhivePriority getHiPhivePrioritiser(List<String> hpoIds, String candGene, String disease, String hiPhiveParams) {
        HiPhivePriority priority = new HiPhivePriority(hpoIds, new HiPhiveOptions(disease, candGene, hiPhiveParams), randomWalkMatrix);
        priority.setPriorityService(priorityService);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

}
