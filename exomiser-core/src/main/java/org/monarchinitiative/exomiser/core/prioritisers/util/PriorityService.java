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
package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.model.Model;
import org.monarchinitiative.exomiser.core.model.Organism;
import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.model.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Service class which offers a single interface to other services required by
 * prioritisers. This aims to simplify getting data from different Services.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class PriorityService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriorityService.class);

    private final OntologyService ontologyService;
    private final ModelService modelService;
    private final DiseaseDao diseaseDao;

    @Autowired
    public PriorityService(OntologyService ontologyService, ModelService modelService, DiseaseDao diseaseDao) {
        this.ontologyService = ontologyService;
        this.modelService = modelService;
        this.diseaseDao = diseaseDao;
    }

    public List<PhenotypeTerm> makePhenotypeTermsFromHpoIds(List<String> hpoIds) {
        return hpoIds.stream()
                .map(ontologyService::getPhenotypeTermForHpoId)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public Set<PhenotypeTerm> getHpoTerms() {
        return ontologyService.getHpoTerms();
    }

    public Set<PhenotypeTerm> getMpoTerms() {
        return ontologyService.getMpoTerms();
    }

    public Set<PhenotypeTerm> getZpoTerms() {
        return ontologyService.getZpoTerms();
    }

    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        return ontologyService.getHpoIdsForDiseaseId(diseaseId);
    }

    public OrganismPhenotypeMatches getMatchingPhenotypesForOrganism(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatches(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

    public Set<PhenotypeMatch> getSpeciesMatchesForHpoTerm(PhenotypeTerm hpoTerm, Organism species) {
        switch (species) {
            case HUMAN:
                return ontologyService.getHpoMatchesForHpoTerm(hpoTerm);
            case MOUSE:
                return ontologyService.getMpoMatchesForHpoTerm(hpoTerm);
            case FISH:
                return ontologyService.getZpoMatchesForHpoTerm(hpoTerm);
            default:
                return Collections.emptySet();
        }
    }

    @Cacheable(value = "models", key = "#species", cacheResolver = "modelCacheResolver")
    public List<Model> getModelsForOrganism(Organism species) {
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
