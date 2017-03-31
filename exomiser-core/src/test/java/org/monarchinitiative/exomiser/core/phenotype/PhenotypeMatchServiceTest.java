package org.monarchinitiative.exomiser.core.phenotype;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeMatchServiceTest {

    private OntologyService ontologyService = TestPriorityServiceFactory.TEST_ONTOLOGY_SERVICE;

    private List<String> getAllHpoIds() {
        return ontologyService.getHpoTerms().stream().map(PhenotypeTerm::getId).collect(toList());
    }

    @Test
    public void testIntegrationWithModelScorer() {
        PhenotypeMatchService instance = new PhenotypeMatchService(ontologyService);
        List<String> hpoIds = getAllHpoIds();

        List<PhenotypeTerm> queryTerms = instance.makePhenotypeTermsFromHpoIds(hpoIds);
        PhenotypeMatcher hpHpQueryMatcher = instance.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer modelScorer = PhenodigmModelScorer.forSameSpecies(hpHpQueryMatcher);

        Model exactMatch = new TestModel("EXACT_MATCH", hpoIds);
        Model noMatch = new TestModel("NO_MATCH", Collections.emptyList());

        List<ModelPhenotypeMatch> matches = Stream.of(exactMatch, noMatch)
                .map(modelScorer::scoreModel)
                .sorted()
                .collect(toList());

        ModelPhenotypeMatch topScore = matches.get(0);
        assertThat(topScore.getScore(), equalTo(1d));
        assertThat(topScore.getModel(), equalTo(exactMatch));

        ModelPhenotypeMatch bottomScore = matches.get(1);
        assertThat(bottomScore.getScore(), equalTo(0d));
        assertThat(bottomScore.getModel(), equalTo(noMatch));

    }

    /**
     * Simple class to enable testing the ModelScorer.
     */
    private class TestModel implements Model {

        private final String id;
        private final List<String> phenotypes;

        public TestModel(String id, List<String> phenotypes) {
            this.id = id;
            this.phenotypes = phenotypes;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public List<String> getPhenotypeIds() {
            return phenotypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestModel testModel = (TestModel) o;
            return Objects.equals(id, testModel.id) &&
                    Objects.equals(phenotypes, testModel.phenotypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, phenotypes);
        }

        @Override
        public String toString() {
            return "TestModel{" +
                    "id='" + id + '\'' +
                    ", phenotypes=" + phenotypes +
                    '}';
        }
    }
}