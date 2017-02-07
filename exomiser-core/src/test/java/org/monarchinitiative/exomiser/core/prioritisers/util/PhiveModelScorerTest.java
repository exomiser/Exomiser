package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.*;

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
public class PhiveModelScorerTest {

    private PriorityService priorityService = TestPriorityServiceFactory.TEST_SERVICE;

    private Model makeBestHumanModel(OrganismPhenotypeMatches referenceOrganismPhenotypeMatches) {
        List<String> exactHumanPhenotypes = getBestMatchedPhenotypes(referenceOrganismPhenotypeMatches);
        return new DiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", exactHumanPhenotypes);
    }

    private Model makeBestMouseModel(OrganismPhenotypeMatches mouseOrganismPhenotypeMatches) {
        List<String> exactMousePhenotypes = getBestMatchedPhenotypes(mouseOrganismPhenotypeMatches);
        return new GeneModel("MOUSE:1", Organism.MOUSE, 12345, "GENE1", "MGI:12345", "gene1", exactMousePhenotypes);
    }

    private Model makeBestFishModel(OrganismPhenotypeMatches fishOrganismPhenotypeMatches) {
        List<String> exactFishPhenotypes = getBestMatchedPhenotypes(fishOrganismPhenotypeMatches);
        return new GeneModel("FISH:1", Organism.FISH, 12345, "GENE1", "ZFIN:12345", "gene-1", exactFishPhenotypes);
    }

    private List<String> getBestMatchedPhenotypes(OrganismPhenotypeMatches organismPhenotypeMatches) {
        return organismPhenotypeMatches.getBestPhenotypeMatches().stream().map(PhenotypeMatch::getMatchPhenotypeId).collect(toList());
    }

    @Test
    public void testScoreModelNoPhenotypesNoMatches() {
        OrganismPhenotypeMatches emptyMatches = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());

        PhiveModelScorer instance = PhiveModelScorer.forSameSpecies(emptyMatches);

        Model model = new DiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", Collections.emptyList());

        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
    }

    @Test
    public void testScoreModelNoMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.HUMAN);

        PhiveModelScorer instance = PhiveModelScorer.forSameSpecies(referenceOrganismPhenotypeMatches);

        PhenotypeTerm noMatchTerm = PhenotypeTerm.of("HP:000000", "No term");
        //The model should have no phenotypes in common with the query set.
        assertThat(queryTerms.contains(noMatchTerm), is(false));
        Model model = new DiseaseModel("DISEASE:2", Organism.HUMAN, 12345, "GENE2", "DISEASE:2", "disease 2", Collections.singletonList(noMatchTerm.getId()));
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.0));
    }

    @Test
    public void testScoreModelPerfectMatch() {
        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.HUMAN);

        PhiveModelScorer instance = PhiveModelScorer.forSameSpecies(referenceOrganismPhenotypeMatches);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatches);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreModelPartialMatch() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.HUMAN);

        PhiveModelScorer instance = PhiveModelScorer.forSameSpecies(referenceOrganismPhenotypeMatches);

        List<String> twoExactPhenotypeMatches = queryTerms.stream().limit(2).map(PhenotypeTerm::getId).collect(toList());

        Model model = new DiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", twoExactPhenotypeMatches);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(0.732228059966757));
    }

    @Test
    public void testScoreModelPerfectMatchModelAndUnmatchedQueryPhenotype() {

        List<PhenotypeTerm> queryTerms = new ArrayList<>(priorityService.getHpoTerms());
        queryTerms.add(PhenotypeTerm.of("HP:000001", "No match"));
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.HUMAN);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatches);
        PhiveModelScorer instance = PhiveModelScorer.forSameSpecies(referenceOrganismPhenotypeMatches);
        ModelPhenotypeMatch result = instance.scoreModel(model);

        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreSingleCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        OrganismPhenotypeMatches mouseOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.MOUSE);

        PhiveModelScorer mousePhiveModelScorer = PhiveModelScorer.forSingleCrossSpecies(mouseOrganismPhenotypeMatches);

        Model model = makeBestMouseModel(mouseOrganismPhenotypeMatches);

        ModelPhenotypeMatch result = mousePhiveModelScorer.scoreModel(model);
        System.out.println(result);
        assertThat(result.getScore(), equalTo(1.0));
    }

    @Test
    public void testScoreMultiCrossSpecies() {

        List<PhenotypeTerm> queryTerms = ImmutableList.copyOf(priorityService.getHpoTerms());
        queryTerms.forEach(System.out::println);

        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.HUMAN);
        TheoreticalModel bestTheoreticalModel = referenceOrganismPhenotypeMatches.getBestTheoreticalModel();


        PhiveModelScorer diseaseModelScorer = PhiveModelScorer.forMultiCrossSpecies(bestTheoreticalModel, referenceOrganismPhenotypeMatches);
        Model disease = makeBestHumanModel(referenceOrganismPhenotypeMatches);
        ModelPhenotypeMatch diseaseResult = diseaseModelScorer.scoreModel(disease);
        System.out.println(diseaseResult);
        assertThat(diseaseResult.getScore(), equalTo(1.0));


        OrganismPhenotypeMatches mouseOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.MOUSE);
        PhiveModelScorer mouseModelScorer = PhiveModelScorer.forMultiCrossSpecies(bestTheoreticalModel, mouseOrganismPhenotypeMatches);
        Model mouse = makeBestMouseModel(mouseOrganismPhenotypeMatches);
        ModelPhenotypeMatch mouseResult = mouseModelScorer.scoreModel(mouse);
        System.out.println(mouseResult);
        assertThat(mouseResult.getScore(), equalTo(0.9718528996668048));


        OrganismPhenotypeMatches fishOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(queryTerms, Organism.FISH);
        PhiveModelScorer fishModelScorer = PhiveModelScorer.forMultiCrossSpecies(bestTheoreticalModel, fishOrganismPhenotypeMatches);
        Model fish = makeBestFishModel(fishOrganismPhenotypeMatches);
        ModelPhenotypeMatch fishResult = fishModelScorer.scoreModel(fish);
        System.out.println(fishResult);
        assertThat(fishResult.getScore(), equalTo(0.628922135363762));
    }
}
