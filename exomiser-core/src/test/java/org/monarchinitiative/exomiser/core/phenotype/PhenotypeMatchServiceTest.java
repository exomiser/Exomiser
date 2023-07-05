/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
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

    private final OntologyService ontologyService = TestPriorityServiceFactory.testOntologyService();

    @Test
    public void testIntegrationWithModelScorer() {
        PhenotypeMatchService instance = new PhenotypeMatchService(ontologyService);
        List<String> hpoIds = TestPriorityServiceFactory.pfeifferSyndromePhenotypes()
                .stream()
                .map(PhenotypeTerm::id)
                .collect(toList());

        List<PhenotypeTerm> queryTerms = instance.makePhenotypeTermsFromHpoIds(hpoIds);
        PhenotypeMatcher hpHpQueryMatcher = instance.getHumanPhenotypeMatcherForTerms(queryTerms);

        ModelScorer<TestModel> modelScorer = PhenodigmModelScorer.forSameSpecies(hpHpQueryMatcher);

        TestModel exactMatch = new TestModel("EXACT_MATCH", hpoIds);
        TestModel noMatch = new TestModel("NO_MATCH", Collections.emptyList());

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
    private static class TestModel implements Model {

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