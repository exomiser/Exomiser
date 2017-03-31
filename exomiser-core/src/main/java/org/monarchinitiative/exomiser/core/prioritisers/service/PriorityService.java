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
package org.monarchinitiative.exomiser.core.prioritisers.service;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatcher;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service class which offers a single interface to other services required by
 * prioritisers. This aims to simplify getting data from different Services.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class PriorityService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriorityService.class);

    private final ModelService modelService;
    private final PhenotypeMatchService phenotypeMatchService;
    private final DiseaseDao diseaseDao;

    @Autowired
    public PriorityService(ModelService modelService, PhenotypeMatchService phenotypeMatchService, DiseaseDao diseaseDao) {
        this.modelService = modelService;
        this.phenotypeMatchService = phenotypeMatchService;
        this.diseaseDao = diseaseDao;
    }

    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        return ImmutableList.copyOf(diseaseDao.getHpoIdsForDiseaseId(diseaseId));
    }

    public List<PhenotypeTerm> makePhenotypeTermsFromHpoIds(List<String> hpoIds) {
        return phenotypeMatchService.makePhenotypeTermsFromHpoIds(hpoIds);
    }

    public PhenotypeMatcher getPhenotypeMatcherForOrganism(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        switch (organism) {
            case HUMAN:
                return phenotypeMatchService.getHumanPhenotypeMatcherForTerms(queryHpoPhenotypes);
            case MOUSE:
                return phenotypeMatchService.getMousePhenotypeMatcherForTerms(queryHpoPhenotypes);
            case FISH:
                return phenotypeMatchService.getFishPhenotypeMatcherForTerms(queryHpoPhenotypes);
            default:
                throw new IllegalArgumentException("Organism" + organism + "not valid");
        }
    }

    @Cacheable(value = "models", key = "#species", cacheResolver = "modelCacheResolver")
    public List<GeneModel> getModelsForOrganism(Organism species) {
        logger.info("Fetching disease/gene model phenotype annotations and HUMAN-{} gene orthologs", species);
        switch (species) {
            case HUMAN:
                return modelService.getHumanGeneDiseaseModels();
            case MOUSE:
                return modelService.getMouseGeneOrthologModels();
            case FISH:
                return modelService.getFishGeneOrthologModels();
            default:
                return Collections.emptyList();
        }
    }

    public List<Disease> getDiseaseDataAssociatedWithGeneId(int geneId) {
        return diseaseDao.getDiseaseDataAssociatedWithGeneId(geneId);
    }

}
