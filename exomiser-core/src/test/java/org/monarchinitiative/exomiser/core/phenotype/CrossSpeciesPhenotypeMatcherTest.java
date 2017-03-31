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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CrossSpeciesPhenotypeMatcherTest {

    private CrossSpeciesPhenotypeMatcher instance;

    //Nose phenotypes
    private final PhenotypeTerm bigNose = PhenotypeTerm.of("HP:0000001", "Big nose");
    private final PhenotypeTerm nose = PhenotypeTerm.of("HP:0000002", "Nose");
    private final PhenotypeTerm littleNose = PhenotypeTerm.of("HP:0000003", "Little nose");

    private final PhenotypeMatch perfectNoseMatch = PhenotypeMatch.builder().query(bigNose).match(bigNose).lcs(bigNose).simj(1.0).score(4.0).build();
    private final PhenotypeMatch noseMatch = PhenotypeMatch.builder().query(bigNose).match(littleNose).lcs(nose).simj(0.5).score(1.0).build();

    //Toe phenotypes
    private final PhenotypeTerm toe = PhenotypeTerm.of("HP:0000004", "Toe");
    private final PhenotypeTerm bigToe = PhenotypeTerm.of("HP:0000005", "Big toe");
    private final PhenotypeTerm crookedToe = PhenotypeTerm.of("HP:0000006", "Crooked toe");
    private final PhenotypeTerm longToe = PhenotypeTerm.of("HP:0000007", "Long toe");

    private final PhenotypeMatch bestToeMatch = PhenotypeMatch.builder().query(bigToe).match(longToe).lcs(toe).score(2.0).build();
    private final PhenotypeMatch bigToeCrookedToeMatch = PhenotypeMatch.builder().query(bigToe).match(crookedToe).lcs(toe).score(1.5).build();

    @Before
    public void setUp() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(bigToe, Sets.newHashSet(bestToeMatch, bigToeCrookedToeMatch));

        instance = new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, phenotypeMatches);

        //TODO: would this make more sense?
        //QueryPhenotypeMatch queryPhenotypeMatch = new QueryPhenotypeMatch(Organism.HUMAN, phenotypeMatches);
        //CrossSpeciesPhenotypeMatcher phenotypeMatcher = new CrossSpeciesPhenotypeMatcher(queryPhenotypeMatch);
    }

    @Test
    public void emptyInputValues() throws Exception {
        CrossSpeciesPhenotypeMatcher instance = new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap());

        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(instance.getQueryTerms(), equalTo(Collections.emptyList()));
        assertThat(instance.getTermPhenotypeMatches(), equalTo(Collections.emptyMap()));
    }

    @Test
    public void testEquals() {
        CrossSpeciesPhenotypeMatcher emptyHumanOne = new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap());
        CrossSpeciesPhenotypeMatcher emptyMouseOne = new CrossSpeciesPhenotypeMatcher(Organism.MOUSE, Collections.emptyMap());
        CrossSpeciesPhenotypeMatcher emptyHumanTwo = new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap());
        assertThat(emptyHumanOne, equalTo(emptyHumanTwo));
        assertThat(emptyHumanOne, not(equalTo(emptyMouseOne)));
    }

    @Test
    public void testToString() {
        System.out.println(new CrossSpeciesPhenotypeMatcher(Organism.HUMAN, Collections.emptyMap()));
        System.out.println(instance);
    }

    @Test
    public void testGetBestForwardAndReciprocalMatches_returnsEmptyListFromEmptyQuery() throws Exception {
        assertThat(instance.calculateBestForwardAndReciprocalMatches(Collections.emptyList()), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetBestForwardAndReciprocalMatches() throws Exception {
        List<String> modelPhenotypes = Lists.newArrayList(littleNose.getId(), longToe.getId());
        List<PhenotypeMatch> expected = Lists.newArrayList(noseMatch, bestToeMatch, noseMatch, bestToeMatch);
        expected.forEach(match -> System.out.printf("%s-%s=%f%n", match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore()));
        assertThat(instance.calculateBestForwardAndReciprocalMatches(modelPhenotypes), equalTo(expected));
    }

    @Test
    public void testCanCalculateBestPhenotypeMatchesByTerm() {
        List<PhenotypeMatch> bestForwardAndReciprocalMatches = Lists.newArrayList(noseMatch, bestToeMatch, perfectNoseMatch, bestToeMatch);
        List<PhenotypeMatch> result = instance.calculateBestPhenotypeMatchesByTerm(bestForwardAndReciprocalMatches);
        assertThat(result, containsInAnyOrder(bestToeMatch, perfectNoseMatch));
    }

    @Test
    public void testCalculateBestPhenotypeMatchesByTermReturnsEmptyMapForEmptyInputList() {
        assertThat(instance.calculateBestPhenotypeMatchesByTerm(Collections.emptyList()), equalTo(Collections.emptyList()));
    }

    @Test
    public void testCanGetTheoreticalBestModel() {
        assertThat(instance.getQueryPhenotypeMatch(), equalTo(new QueryPhenotypeMatch(Organism.HUMAN, instance.getTermPhenotypeMatches())));
    }
}