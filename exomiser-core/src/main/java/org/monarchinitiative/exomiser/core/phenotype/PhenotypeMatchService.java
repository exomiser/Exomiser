/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.phenotype;

import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Service for generating the best phenotypic matches for a given set of HPO terms against human, mouse or fish
 * ontologies. The matches are produced from Phenodigm data which computed the scores using OwlSim. 
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
@Service
public class PhenotypeMatchService {

    Logger logger = LoggerFactory.getLogger(PhenotypeMatchService.class);

    private final OntologyService ontologyService;

    @Autowired
    public PhenotypeMatchService(OntologyService ontologyService) {
        Objects.requireNonNull(ontologyService, "ontologyService cannot be null");
        this.ontologyService = ontologyService;
    }

    public PhenotypeMatcher getHumanPhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.HUMAN);
    }

    public PhenotypeMatcher getMousePhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.MOUSE);
    }

    public PhenotypeMatcher getFishPhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.FISH);
    }

    private PhenotypeMatcher getOrganismPhenotypeMatcherFromTerms(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.debug("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return CrossSpeciesPhenotypeMatcher.of(organism, speciesPhenotypeMatches);
    }

    public List<PhenotypeTerm> makePhenotypeTermsFromHpoIds(List<String> hpoIds) {
        return hpoIds.stream()
                .map(ontologyService::getPhenotypeTermForHpoId)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private Set<PhenotypeMatch> getSpeciesMatchesForHpoTerm(PhenotypeTerm hpoTerm, Organism species) {
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

}
