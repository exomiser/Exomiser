package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenodigmModelScorerTest {

    private OntologyService ontologyService = TestPriorityServiceFactory.TEST_ONTOLOGY_SERVICE;
    private PhenotypeMatchService priorityService = new PhenotypeMatchService(ontologyService);

    private Model makeBestHumanModel(PhenotypeMatcher referenceOrganismPhenotypeMatcher) {
        List<String> exactHumanPhenotypes = getBestMatchedPhenotypes(referenceOrganismPhenotypeMatcher);
        return new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", exactHumanPhenotypes);
    }

    private Model makeBestMouseModel(PhenotypeMatcher mouseOrganismPhenotypeMatcher) {
        List<String> exactMousePhenotypes = getBestMatchedPhenotypes(mouseOrganismPhenotypeMatcher);
        return new GeneOrthologModel("MOUSE:1", Organism.MOUSE, 12345, "GENE1", "MGI:12345", "gene1", exactMousePhenotypes);
    }

    private Model makeBestFishModel(PhenotypeMatcher fishOrganismPhenotypeMatcher) {
        List<String> exactFishPhenotypes = getBestMatchedPhenotypes(fishOrganismPhenotypeMatcher);
        return new GeneOrthologModel("FISH:1", Organism.FISH, 12345, "GENE1", "ZFIN:12345", "gene-1", exactFishPhenotypes);
    }

    private List<String> getBestMatchedPhenotypes(PhenotypeMatcher organismPhenotypeMatcher) {
        return organismPhenotypeMatcher.getBestPhenotypeMatches()
                .stream()
                .map(PhenotypeMatch::getMatchPhenotypeId)
                .collect(toList());
    }

    @Test
    public void testScoreModelNoPhenotypesNoMatches() {
        PhenotypeMatcher emptyMatches = new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap());

        ModelScorer instance = PhenodigmModelScorer.forSameSpecies(emptyMatches);

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", Collections.emptyList());

        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
    }

    @Test
    public void testScoreModelNoMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(ontologyService.getHpoTerms());
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        PhenotypeTerm noMatchTerm = PhenotypeTerm.of("HP:000000", "No term");
        //The model should have no phenotypes in common with the query set.
        assertThat(queryTerms.contains(noMatchTerm), is(false));
        Model model = new GeneDiseaseModel("DISEASE:2", Organism.HUMAN, 12345, "GENE2", "DISEASE:2", "disease 2", Collections.singletonList(noMatchTerm.getId()));
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
        assertThat(result.getBestPhenotypeMatches().isEmpty(), is(true));
    }

    @Test
    public void testScoreModelPerfectMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(ontologyService.getHpoTerms());
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreModelPartialMatch() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(ontologyService.getHpoTerms());
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        List<String> twoExactPhenotypeMatches = queryTerms.stream().limit(2).map(PhenotypeTerm::getId).collect(toList());

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", twoExactPhenotypeMatches);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.732228059966757));
    }

    @Test
    public void testScoreModelPerfectMatchModelAndUnmatchedQueryPhenotype() {
        List<PhenotypeTerm> queryTerms = new ArrayList<>(ontologyService.getHpoTerms());
        queryTerms.add(PhenotypeTerm.of("HP:000000", "No match"));
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreSingleCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(ontologyService.getHpoTerms());
        PhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getMousePhenotypeMatcherForTerms(queryTerms);

        ModelScorer mousePhiveModelScorer = PhenodigmModelScorer.forSingleCrossSpecies(mouseOrganismPhenotypeMatcher);

        Model model = makeBestMouseModel(mouseOrganismPhenotypeMatcher);

        ModelPhenotypeMatch result = mousePhiveModelScorer.scoreModel(model);
        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreMultiCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(ontologyService.getHpoTerms());
        queryTerms.forEach(System.out::println);

        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);
        QueryPhenotypeMatch bestQueryPhenotypeMatch = referenceOrganismPhenotypeMatcher.getQueryPhenotypeMatch();


        ModelScorer diseaseModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, referenceOrganismPhenotypeMatcher);
        Model disease = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch diseaseResult = diseaseModelScorer.scoreModel(disease);
        System.out.println(diseaseResult);
        assertThat(diseaseResult.getScore(), equalTo(1.0));


        PhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getMousePhenotypeMatcherForTerms(queryTerms);
        ModelScorer mouseModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, mouseOrganismPhenotypeMatcher);
        Model mouse = makeBestMouseModel(mouseOrganismPhenotypeMatcher);
        ModelPhenotypeMatch mouseResult = mouseModelScorer.scoreModel(mouse);
        System.out.println(mouseResult);
        assertThat(mouseResult.getScore(), equalTo(0.9718528996668048));


        PhenotypeMatcher fishOrganismPhenotypeMatcher = priorityService.getFishPhenotypeMatcherForTerms(queryTerms);
        ModelScorer fishModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, fishOrganismPhenotypeMatcher);
        Model fish = makeBestFishModel(fishOrganismPhenotypeMatcher);
        ModelPhenotypeMatch fishResult = fishModelScorer.scoreModel(fish);
        System.out.println(fishResult);
        assertThat(fishResult.getScore(), equalTo(0.628922135363762));
    }
}
