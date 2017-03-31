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

package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class QueryPhenotypeMatchTest {

    private QueryPhenotypeMatch instance;

    //No phenotype match
    private final PhenotypeTerm noMatchTerm = PhenotypeTerm.of("HP:0000000", "No match phenotype");

    //Nose phenotypes
    private final PhenotypeTerm bigNose = PhenotypeTerm.of("HP:0000001", "Big nose");
    private final PhenotypeTerm nose = PhenotypeTerm.of("HP:0000002", "Nose");
    private final PhenotypeTerm littleNose = PhenotypeTerm.of("HP:0000003", "Little nose");

    private final PhenotypeMatch perfectNoseMatch = PhenotypeMatch.builder().query(bigNose).match(bigNose).lcs(bigNose).ic(2.0).simj(1.0).score(4.0).build();
    private final PhenotypeMatch noseMatch = PhenotypeMatch.builder().query(bigNose).match(littleNose).lcs(nose).ic(1.0).simj(0.5).score(1.0).build();

    //Toe phenotypes
    private final PhenotypeTerm toe = PhenotypeTerm.of("HP:0000004", "Toe");
    private final PhenotypeTerm bigToe = PhenotypeTerm.of("HP:0000005", "Big toe");
    private final PhenotypeTerm crookedToe = PhenotypeTerm.of("HP:0000006", "Crooked toe");
    private final PhenotypeTerm longToe = PhenotypeTerm.of("HP:0000007", "Long toe");

    private final PhenotypeMatch bestToeMatch = PhenotypeMatch.builder().query(bigToe).match(longToe).lcs(toe).ic(1.0).simj(1.0).score(2.0).build();
    private final PhenotypeMatch bigToeCrookedToeMatch = PhenotypeMatch.builder().query(bigToe).match(crookedToe).lcs(toe).ic(1.0).simj(1.0).score(1.5).build();

    private final Set<PhenotypeMatch> bestMatches = Sets.newHashSet(perfectNoseMatch, bestToeMatch);

    @Before
    public void setUp() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(bigToe, Sets.newHashSet(bestToeMatch, bigToeCrookedToeMatch));

        instance = new QueryPhenotypeMatch(Organism.HUMAN, phenotypeMatches);
    }

    @Test
    public void testWithNoPhenotypeMatch() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> noPhenotypeMatches = new LinkedHashMap<>();
        noPhenotypeMatches.put(noMatchTerm, Collections.emptySet());
        QueryPhenotypeMatch noMatchModel = new QueryPhenotypeMatch(Organism.HUMAN, noPhenotypeMatches);

        assertThat(noMatchModel.getBestPhenotypeMatches(), equalTo(Collections.emptySet()));
        assertThat(noMatchModel.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(noMatchModel.getBestAvgScore(), equalTo(0.0));
        assertThat(noMatchModel.getMaxMatchScore(), equalTo(0.0));
    }

    @Test
    public void testWithSomeEmptyPhenotypeMatches() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(noMatchTerm, Collections.emptySet());
        QueryPhenotypeMatch matchModel = new QueryPhenotypeMatch(Organism.HUMAN, phenotypeMatches);

        assertThat(matchModel.getBestPhenotypeMatches(), equalTo(Sets.newHashSet(perfectNoseMatch)));
        assertThat(matchModel.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(matchModel.getBestAvgScore(), equalTo(2d));
        assertThat(matchModel.getMaxMatchScore(), equalTo(4d));
    }

    @Test
    public void testGetOrganism() throws Exception {
        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
    }

    @Test
    public void testGetQueryTerms() {
        assertThat(instance.getQueryTerms(), equalTo(Arrays.asList(bigNose, bigToe)));
    }

    @Test
    public void testGetBestPhenotypeMatches() throws Exception {
        assertThat(instance.getBestPhenotypeMatches(), equalTo(bestMatches));
    }

    @Test
    public void testGetTheoreticalMaxMatchScore() {
        assertThat(instance.getMaxMatchScore(), equalTo(4d));
    }

    @Test
    public void testGetTheoreticalMaxMatchScoreNoMatches() {
        QueryPhenotypeMatch noMatchesInstance = new QueryPhenotypeMatch(Organism.HUMAN, Collections.emptyMap());
        assertThat(noMatchesInstance.getMaxMatchScore(), equalTo(0d));
    }

    @Test
    public void testGetBestAverageScore() {
        double expected = (bestToeMatch.getScore() + perfectNoseMatch.getScore()) / 2d;
        assertThat(instance.getBestAvgScore(), equalTo(expected));
    }

    @Test
    public void testGetBestAverageScoreNoMatches() {
        QueryPhenotypeMatch noMatchesInstance = new QueryPhenotypeMatch(Organism.HUMAN, Collections.emptyMap());
        assertThat(noMatchesInstance.getBestAvgScore(), equalTo(0d));
    }

}