package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.model.Organism;
import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.model.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public PhenotypeMatchService(OntologyService ontologyService) {
        Objects.requireNonNull(ontologyService, "ontologyService cannot be null");
        this.ontologyService = ontologyService;
    }

    public OrganismPhenotypeMatcher getHumanPhenotypeMatcherForIds(List<String> hpoIds) {
        return getOrganismPhenotypeMatcher(hpoIds, Organism.HUMAN);
    }

    public OrganismPhenotypeMatcher getHumanPhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.HUMAN);
    }

    public OrganismPhenotypeMatcher getMousePhenotypeMatcherForIds(List<String> hpoIds) {
        return getOrganismPhenotypeMatcher(hpoIds, Organism.MOUSE);
    }

    public OrganismPhenotypeMatcher getMousePhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.MOUSE);
    }

    public OrganismPhenotypeMatcher getFishPhenotypeMatcherForIds(List<String> hpoIds) {
        return getOrganismPhenotypeMatcher(hpoIds, Organism.FISH);
    }

    public OrganismPhenotypeMatcher getFishPhenotypeMatcherForTerms(List<PhenotypeTerm> hpoPhenotypeTerms) {
        return getOrganismPhenotypeMatcherFromTerms(hpoPhenotypeTerms, Organism.FISH);
    }

    private OrganismPhenotypeMatcher getOrganismPhenotypeMatcher(List<String> hpoIds, Organism organism) {
        List<PhenotypeTerm> queryHpoPhenotypes = makePhenotypeTermsFromHpoIds(hpoIds);
        return getOrganismPhenotypeMatcherFromTerms(queryHpoPhenotypes, organism);
    }

    private OrganismPhenotypeMatcher getOrganismPhenotypeMatcherFromTerms(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatcher(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

    private List<PhenotypeTerm> makePhenotypeTermsFromHpoIds(List<String> hpoIds) {
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
