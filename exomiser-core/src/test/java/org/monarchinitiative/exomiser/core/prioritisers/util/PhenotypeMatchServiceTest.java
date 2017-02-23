package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.PhenotypeTerm;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeMatchServiceTest {

    private OntologyService ontologyService = TestPriorityServiceFactory.TEST_ONTOLOGY_SERVICE;

    private List<String> getAllHpoIds() {
        return ontologyService.getHpoTerms().stream().map(PhenotypeTerm::getId).collect(toList());
    }

    @Test
    public void testWithModelScorer() {
        PhenotypeMatchService instance = new PhenotypeMatchService(ontologyService);
        List<String> hpoIds = getAllHpoIds();
        //Rename OrganismPhenotypeMatches to QueryPhenotypeMatches?
        OrganismPhenotypeMatches hpHpMatches = instance.getBestHumanPhenotypeMatchesForQuery(hpoIds);
    //        QueryPhenotypeMatches queryMpoPhenotypeMatches = phenotypeMatchService.getBestMousePhenotypeMatches(hpoTerms);
    //        QueryPhenotypeMatches queryZpoPhenotypeMatches = phenotypeMatchService.getBestFishPhenotypeMatches(hpoTerms);

            ModelScorer modelScorer = ModelScorer.forSameSpecies(hpHpMatches);
    //        for (Model model : diseaseModels) {
    //          PhenotypeMatchScore score = modelScorer.scorePhenotypes(model.getPhenotypeIds());
    //        }

    }

}