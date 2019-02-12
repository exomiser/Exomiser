/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CrossSpeciesPhenotypeMatcherTest {

    // Nose phenotypes
    private final PhenotypeTerm bigNose = PhenotypeTerm.of("HP:0000001", "Big nose");
    private final PhenotypeTerm nose = PhenotypeTerm.of("HP:0000002", "Nose");
    private final PhenotypeTerm littleNose = PhenotypeTerm.of("HP:0000003", "Little nose");

    private final PhenotypeMatch bigNoseSelfMatch = PhenotypeMatch.builder()
            .query(bigNose).match(bigNose).lcs(bigNose).simj(1.0).score(4.0).build();

    private final PhenotypeMatch noseMatch = PhenotypeMatch.builder()
            .query(bigNose).match(littleNose).lcs(nose).simj(0.5).score(1.0).build();

    // Toe phenotypes
    private final PhenotypeTerm toe = PhenotypeTerm.of("HP:0000004", "Toe");
    private final PhenotypeTerm bigToe = PhenotypeTerm.of("HP:0000005", "Big toe");
    private final PhenotypeTerm crookedToe = PhenotypeTerm.of("HP:0000006", "Crooked toe");
    private final PhenotypeTerm longToe = PhenotypeTerm.of("HP:0000007", "Long toe");

    private final PhenotypeMatch bigToeSelfMatch = PhenotypeMatch.builder()
            .query(bigToe).match(bigToe).lcs(bigToe).simj(1.0).score(4.0).build();

    private final PhenotypeMatch bigToeLogToeMatch = PhenotypeMatch.builder()
            .query(bigToe).match(longToe).lcs(toe).score(2.0).build();

    private final PhenotypeMatch bigToeCrookedToeMatch = PhenotypeMatch.builder()
            .query(bigToe).match(crookedToe).lcs(toe).score(1.5).build();

    // Set-up
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = ImmutableMap.of(
            bigNose, Sets.newHashSet(bigNoseSelfMatch, noseMatch),
            bigToe, Sets.newHashSet(bigToeSelfMatch, bigToeLogToeMatch, bigToeCrookedToeMatch)
    );

    private final QueryPhenotypeMatch queryPhenotypeMatch = new QueryPhenotypeMatch(Organism.HUMAN, phenotypeMatches);
    private final CrossSpeciesPhenotypeMatcher instance = CrossSpeciesPhenotypeMatcher.of(queryPhenotypeMatch);

    @Test
    public void emptyInputValues() {
        CrossSpeciesPhenotypeMatcher instance = CrossSpeciesPhenotypeMatcher.of(Organism.HUMAN, Collections.emptyMap());

        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(instance.getQueryTerms(), equalTo(Collections.emptyList()));
        assertThat(instance.getTermPhenotypeMatches(), equalTo(Collections.emptyMap()));
    }

    @Test
    public void testEquals() {
        CrossSpeciesPhenotypeMatcher emptyHumanOne = CrossSpeciesPhenotypeMatcher.of(Organism.HUMAN, Collections.emptyMap());
        CrossSpeciesPhenotypeMatcher emptyMouseOne = CrossSpeciesPhenotypeMatcher.of(Organism.MOUSE, Collections.emptyMap());
        CrossSpeciesPhenotypeMatcher emptyHumanTwo = CrossSpeciesPhenotypeMatcher.of(Organism.HUMAN, Collections.emptyMap());
        assertThat(emptyHumanOne, equalTo(emptyHumanTwo));
        assertThat(emptyHumanOne, not(equalTo(emptyMouseOne)));
    }

    @Test
    public void testToString() {
        System.out.println(CrossSpeciesPhenotypeMatcher.of(Organism.HUMAN, Collections.emptyMap()));
        System.out.println(instance);
    }

    @Test
    public void testGetBestForwardAndReciprocalMatchesReturnsEmptyListFromEmptyQuery() throws Exception {
        assertThat(instance.calculateBestForwardAndReciprocalMatches(Collections.emptyList()), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetBestForwardAndReciprocalMatches() throws Exception {
        List<String> modelPhenotypes = ImmutableList.of(littleNose.getId(), longToe.getId());
        List<PhenotypeMatch> expected = ImmutableList.of(noseMatch, bigToeLogToeMatch, noseMatch, bigToeLogToeMatch);
        expected.forEach(match -> System.out.printf("%s-%s=%f%n", match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore()));
        assertThat(instance.calculateBestForwardAndReciprocalMatches(modelPhenotypes), equalTo(expected));
    }

    @Test
    public void testCanCalculateBestPhenotypeMatchesByTerm() {
        List<PhenotypeMatch> bestForwardAndReciprocalMatches = ImmutableList.of(noseMatch, bigToeLogToeMatch, bigNoseSelfMatch, bigToeLogToeMatch);
        List<PhenotypeMatch> result = instance.calculateBestPhenotypeMatchesByTerm(bestForwardAndReciprocalMatches);
        assertThat(result, containsInAnyOrder(bigToeLogToeMatch, bigNoseSelfMatch));
    }

    @Test
    public void testCalculateBestPhenotypeMatchesByTermReturnsEmptyMapForEmptyInputList() {
        assertThat(instance.calculateBestPhenotypeMatchesByTerm(Collections.emptyList()), equalTo(Collections.emptyList()));
    }

    @Test
    public void testCanGetTheoreticalBestModel() {
        assertThat(instance.getQueryPhenotypeMatch(), equalTo(new QueryPhenotypeMatch(Organism.HUMAN, instance.getTermPhenotypeMatches())));
    }

    @Test
    void testGetPhenodigmRawScoreImperfectMatch() {
        List<String> modelPhenotypes = ImmutableList.of(littleNose.getId(), longToe.getId());

        PhenodigmMatchRawScore result = instance.matchPhenotypeIds(modelPhenotypes);

        List<PhenotypeMatch> bestPhenotypeMatches = ImmutableList.of(noseMatch, bigToeLogToeMatch);
        PhenodigmMatchRawScore expected = new PhenodigmMatchRawScore(2.0, 6.0, modelPhenotypes, bestPhenotypeMatches);
        System.out.println(result);
        assertThat(result, equalTo(expected));
    }

    @Test
    void testGetPhenodigmRawScorePerfectMatch() {
        List<String> modelPhenotypes = ImmutableList.of(bigNose.getId(), bigToe.getId());

        PhenodigmMatchRawScore result = instance.matchPhenotypeIds(modelPhenotypes);

        List<PhenotypeMatch> bestPhenotypeMatches = ImmutableList.of(bigNoseSelfMatch, bigToeSelfMatch);
        PhenodigmMatchRawScore expected = new PhenodigmMatchRawScore(4.0, 16.0, modelPhenotypes, bestPhenotypeMatches);
        System.out.println(result);
        assertThat(result, equalTo(expected));
    }

    @Test
    void testGetPhenodigmRawScoreMissingTermMatch() {
        List<String> modelPhenotypes = ImmutableList.of(bigNose.getId(), "HP:0000100");

        PhenodigmMatchRawScore result = instance.matchPhenotypeIds(modelPhenotypes);

        List<PhenotypeMatch> bestPhenotypeMatches = ImmutableList.of(bigNoseSelfMatch);
        PhenodigmMatchRawScore expected = new PhenodigmMatchRawScore(4.0, 8.0, ImmutableList.of(bigNose.getId()), bestPhenotypeMatches);
        System.out.println(result);
        assertThat(result, equalTo(expected));
    }
}