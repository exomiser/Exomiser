/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
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
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenodigmModelScorerTest {

    private final OntologyService ontologyService = TestPriorityServiceFactory.testOntologyService();
    private final PhenotypeMatchService priorityService = new PhenotypeMatchService(ontologyService);

    private GeneDiseaseModel makeBestHumanModel(PhenotypeMatcher referenceOrganismPhenotypeMatcher) {
        List<String> exactHumanPhenotypes = getBestMatchedPhenotypes(referenceOrganismPhenotypeMatcher);
        return new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", exactHumanPhenotypes);
    }

    private GeneOrthologModel makeBestMouseModel(PhenotypeMatcher mouseOrganismPhenotypeMatcher) {
        List<String> exactMousePhenotypes = getBestMatchedPhenotypes(mouseOrganismPhenotypeMatcher);
        return new GeneOrthologModel("MOUSE:1", Organism.MOUSE, 12345, "GENE1", "MGI:12345", "gene1", exactMousePhenotypes);
    }

    private GeneOrthologModel makeBestFishModel(PhenotypeMatcher fishOrganismPhenotypeMatcher) {
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
        PhenotypeMatcher emptyMatches = CrossSpeciesPhenotypeMatcher.of(Organism.HUMAN, Collections.emptyMap());

        ModelScorer<Model> instance = PhenodigmModelScorer.forSameSpecies(emptyMatches);

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", Collections.emptyList());

        ModelPhenotypeMatch<Model> result = instance.scoreModel(model);

        assertThat(result.getScore(), equalTo(0.0));
    }

    @Test
    public void testScoreModelNoMatch() {
        List<PhenotypeTerm> queryTerms = TestPriorityServiceFactory.pfeifferSyndromePhenotypes();
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer<Model> instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        PhenotypeTerm noMatchTerm = PhenotypeTerm.of("HP:000000", "No term");
        //The model should have no phenotypes in common with the query set.
        assertThat(queryTerms.contains(noMatchTerm), is(false));
        Model model = new GeneDiseaseModel("DISEASE:2", Organism.HUMAN, 12345, "GENE2", "DISEASE:2", "disease 2", Collections.singletonList(noMatchTerm.getId()));
        ModelPhenotypeMatch<Model> result = instance.scoreModel(model);

        assertThat(result.getScore(), equalTo(0.0));
        assertThat(result.getBestPhenotypeMatches().isEmpty(), is(true));
    }

    @Test
    public void testScoreModelPerfectMatch() {
        List<PhenotypeTerm> queryTerms = TestPriorityServiceFactory.pfeifferSyndromePhenotypes();
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer<Model> instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch<Model> result = instance.scoreModel(model);

        assertThat(result.getScore(), equalTo(1.0));
        assertThat(result.getModel(), equalTo(model));
    }

    @Test
    public void testScoreModelPartialMatch() {

        List<PhenotypeTerm> queryTerms = TestPriorityServiceFactory.pfeifferSyndromePhenotypes();
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer<Model> instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        List<String> twoExactPhenotypeMatches = ImmutableList.of("HP:0001156", "HP:0001363");

        Model model = new GeneDiseaseModel("DISEASE:1", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "disease", twoExactPhenotypeMatches);
        ModelPhenotypeMatch<Model> result = instance.scoreModel(model);

        assertThat(result.getScore(), equalTo(0.732228059966757));
        assertThat(result.getModel(), equalTo(model));
    }

    @Test
    public void testScoreModelPerfectMatchModelAndUnmatchedQueryPhenotype() {
        List<PhenotypeTerm> queryTerms = new ArrayList<>(TestPriorityServiceFactory.pfeifferSyndromePhenotypes());
        queryTerms.add(PhenotypeTerm.of("HP:000000", "No match"));
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer<Model> instance = PhenodigmModelScorer.forSameSpecies(referenceOrganismPhenotypeMatcher);

        Model model = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch<Model> result = instance.scoreModel(model);

        assertThat(result.getScore(), equalTo(1.0));
        assertThat(result.getModel(), equalTo(model));
    }

    @Test
    public void testScoreSingleCrossSpecies() {

        List<PhenotypeTerm> queryTerms = TestPriorityServiceFactory.pfeifferSyndromePhenotypes();
        PhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getMousePhenotypeMatcherForTerms(queryTerms);

        ModelScorer<Model> mousePhiveModelScorer = PhenodigmModelScorer.forSingleCrossSpecies(mouseOrganismPhenotypeMatcher);

        Model model = makeBestMouseModel(mouseOrganismPhenotypeMatcher);

        ModelPhenotypeMatch<Model> result = mousePhiveModelScorer.scoreModel(model);

        assertThat(result.getScore(), equalTo(1.0));
        assertThat(result.getModel(), equalTo(model));
    }

    @Test
    public void testScoreMultiCrossSpecies() {

        List<PhenotypeTerm> queryTerms = TestPriorityServiceFactory.pfeifferSyndromePhenotypes();

        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getHumanPhenotypeMatcherForTerms(queryTerms);
        QueryPhenotypeMatch bestQueryPhenotypeMatch = referenceOrganismPhenotypeMatcher.getQueryPhenotypeMatch();


        ModelScorer<GeneDiseaseModel> diseaseModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, referenceOrganismPhenotypeMatcher);
        GeneDiseaseModel disease = makeBestHumanModel(referenceOrganismPhenotypeMatcher);
        ModelPhenotypeMatch<GeneDiseaseModel> diseaseResult = diseaseModelScorer.scoreModel(disease);
        assertThat(diseaseResult.getScore(), equalTo(1.0));


        PhenotypeMatcher mouseOrganismPhenotypeMatcher = priorityService.getMousePhenotypeMatcherForTerms(queryTerms);
        ModelScorer<GeneOrthologModel> mouseModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, mouseOrganismPhenotypeMatcher);
        GeneOrthologModel mouse = makeBestMouseModel(mouseOrganismPhenotypeMatcher);
        ModelPhenotypeMatch<GeneOrthologModel> mouseResult = mouseModelScorer.scoreModel(mouse);
        assertThat(mouseResult.getScore(), equalTo(0.9718528996668048));


        PhenotypeMatcher fishOrganismPhenotypeMatcher = priorityService.getFishPhenotypeMatcherForTerms(queryTerms);
        ModelScorer<GeneOrthologModel> fishModelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, fishOrganismPhenotypeMatcher);
        GeneOrthologModel fish = makeBestFishModel(fishOrganismPhenotypeMatcher);
        ModelPhenotypeMatch<GeneOrthologModel> fishResult = fishModelScorer.scoreModel(fish);
        assertThat(fishResult.getScore(), equalTo(0.628922135363762));
    }
}
