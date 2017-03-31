package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.ImmutableMap;
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
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new CrossSpeciesPhenotypeMatcher(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
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
