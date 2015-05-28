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
    private PriorityService priorityService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    @Lazy
    private DataMatrix randomWalkMatrix;
    @Autowired
    private Path phenixDataDirectory;

    /**
     * Returns a list of prioritisers, ready to run according to the settings
     * provided. This list will *ALWAYS* contain a single OMIM prioritiser,
     * unless the NONE or NOT_SET types have been set, in which case the list
     * will be empty.
     *
     * @param exomiserSettings
     * @return
     */
    public List<Prioritiser> makePrioritisers(ExomiserSettings exomiserSettings) {
        //TODO: Should this move into the Exomiser class? The only reason this
        //method exists is that we always run OMIM, even when not specified. 
        //There is no actual evidence that doing this improves the overall performance though. 
        
        PriorityType prioritiserType = exomiserSettings.getPrioritiserType();
        if (prioritiserType == PriorityType.NONE || prioritiserType == PriorityType.NOT_SET) {
            return Collections.emptyList();
        }

        List<Prioritiser> prioritisers = new ArrayList<>();
        //TODO: OmimPrioritizer is specified implicitly - perhaps they should be different types of ExomiserSettings?
        //probably better as a specific type of Exomiser - either a RareDiseaseExomiser or DefaultExomiser. 
        //These might be badly named as the OMIM proritiser is currently the default.
        //always run OMIM unless the user specified what they really don't want to run any prioritisers
        prioritisers.add(makePrioritiser(PriorityType.OMIM_PRIORITY, exomiserSettings));
        if (prioritiserType == PriorityType.OMIM_PRIORITY) {
            return prioritisers;
        } else {
            Prioritiser prioritiser = makePrioritiser(prioritiserType, exomiserSettings);
            if (prioritiser.getPriorityType() != PriorityType.NONE) {
                prioritisers.add(prioritiser);
            }
        }
        return prioritisers;
    }

    /**
     * Returns a Prioritiser of the given type, ready to run according to the
     * settings provided. Will return a non-functional prioritiser in cases
     * where the type is not recognised.
     *
     * @param priorityType
     * @param settings
     * @return
     */
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
        HiPhivePriority priority = new HiPhivePriority(hpoIds, candGene, disease, hiPhiveParams, randomWalkMatrix);
        priority.setPriorityService(priorityService);
        logger.info("Made new prioritiser: {}", priority);
        return priority;
    }

}
