/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

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
                return new NoneTypePrioritiser();
            default:
                logger.warn("Prioritiser: '{}' not supported. Returning '{}' type", priorityType, PriorityType.NONE);
                return new NoneTypePrioritiser();
        }

    }

    private List<String> getHpoIdsForDiseaseId(String diseaseId) {
        if (diseaseId == null || diseaseId.isEmpty()) {
            return Collections.emptyList();
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
        return new PhenixPriority(phenixDataDirectory.toString(), hpoIds, symmetric);
    }

    @Override
    public PhivePriority makePhivePrioritiser(List<String> hpoIds) {
        PhivePriority priority = new PhivePriority(hpoIds, priorityService);
        priority.setDataSource(dataSource);
        return priority;
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        return new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions) {
        return new HiPhivePriority(hpoIds, hiPhiveOptions, randomWalkMatrix, priorityService);
    }

}
