/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

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
    //TODO: this should probably move into the Exomiser class now - it's only used there.
    public Prioritiser makePrioritiser(PriorityType priorityType, PrioritiserSettings settings) {
        //These should form the PrioritySettings interface
        List<String> hpoIds = settings.getHpoIds();
        List<Integer> entrezSeedGenes = settings.getSeedGeneList();
        String diseaseId = settings.getDiseaseId();
        String candidateGene = settings.getCandidateGene();
        String hiPhiveParams = settings.getExomiser2Params();
        
        if (hpoIds.isEmpty()) {
            logger.info("HPO terms have not been specified. Setting HPO IDs using disease annotations for {}", diseaseId);
            hpoIds = getHpoIdsForDiseaseId(diseaseId);
        }

        switch (priorityType) {
            case OMIM_PRIORITY:
                return makeOmimPrioritiser();
            case PHENIX_PRIORITY:
                return makePhenixPrioritiser(hpoIds);
            case HIPHIVE_PRIORITY:
                return makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions(diseaseId, candidateGene, hiPhiveParams));
            case PHIVE_PRIORITY:
                return makePhivePrioritiser(hpoIds);
            case EXOMEWALKER_PRIORITY:
                return makeExomeWalkerPrioritiser(entrezSeedGenes);
            case NONE:
            case NOT_SET:
                return new NoneTypePrioritiser();
            default:
                logger.warn("Prioritiser: '{}' not supported. Returning '{}' type", priorityType, PriorityType.NONE.getCommandLineValue());
                return new NoneTypePrioritiser();
        }

    }

    private List<String> getHpoIdsForDiseaseId(String diseaseId) {
        if (diseaseId == null || diseaseId.isEmpty()) {
            return new ArrayList<>();
        }
        return priorityService.getHpoIdsForDiseaseId(diseaseId);
    }

    @Override
    public OMIMPriority makeOmimPrioritiser() {
        OMIMPriority priority = new OMIMPriority();
        priority.setDataSource(dataSource);
        return priority;
    }

    @Override
    public PhenixPriority makePhenixPrioritiser(List<String> hpoIds) {
        boolean symmetric = false;
        PhenixPriority priority = new PhenixPriority(phenixDataDirectory.toString(), hpoIds, symmetric);
        return priority;
    }

    @Override
    public PhivePriority makePhivePrioritiser(List<String> hpoIds) {
        PhivePriority priority = new PhivePriority(hpoIds);
        priority.setDataSource(dataSource);
        return priority;
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        ExomeWalkerPriority priority = new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
        return priority;
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions) {
        HiPhivePriority priority = new HiPhivePriority(hpoIds, hiPhiveOptions, randomWalkMatrix);
        priority.setPriorityService(priorityService);
        return priority;
    }

}
