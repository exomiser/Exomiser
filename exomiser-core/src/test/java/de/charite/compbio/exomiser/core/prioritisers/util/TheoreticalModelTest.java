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

package de.charite.compbio.exomiser.core.prioritisers.util;

import com.google.common.collect.Sets;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TheoreticalModelTest {

    private TheoreticalModel instance;

    //No phenotype match
    private final PhenotypeTerm noMatchTerm = new PhenotypeTerm("HP:0000000", "No match phenotype", 0.0);

    //Nose phenotypes
    private final PhenotypeTerm bigNose = new PhenotypeTerm("HP:0000001", "Big nose", 2.0);
    private final PhenotypeTerm nose = new PhenotypeTerm("HP:0000002", "Nose", 1.0);
    private final PhenotypeTerm littleNose = new PhenotypeTerm("HP:0000003", "Little nose", 2.0);

    private final PhenotypeMatch perfectNoseMatch = new PhenotypeMatch(bigNose, bigNose, 1.0, 4.0, bigNose);
    private final PhenotypeMatch noseMatch = new PhenotypeMatch(bigNose, littleNose, 0.5, 1.0, nose);

    //Toe phenotypes
    private final PhenotypeTerm toe = new PhenotypeTerm("HP:0000004", "Toe", 1.0);
    private final PhenotypeTerm bigToe = new PhenotypeTerm("HP:0000005", "Big toe", 2.0);
    private final PhenotypeTerm crookedToe = new PhenotypeTerm("HP:0000006", "Crooked toe", 2.0);
    private final PhenotypeTerm longToe = new PhenotypeTerm("HP:0000007", "Long toe", 2.0);

    private final PhenotypeMatch bestToeMatch = new PhenotypeMatch(bigToe, longToe, 1.0, 2.0, toe);
    private final PhenotypeMatch bigToeCrookedToeMatch = new PhenotypeMatch(bigToe, crookedToe, 1.0, 1.5, toe);

    private final Set<PhenotypeMatch> bestMatches = Sets.newHashSet(perfectNoseMatch, bestToeMatch);

    @Before
    public void setUp() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(bigToe, Sets.newHashSet(bestToeMatch, bigToeCrookedToeMatch));

        instance = new TheoreticalModel(Organism.HUMAN, phenotypeMatches);
    }

    @Test
    public void testWithNoPhenotypeMatch() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> noPhenotypeMatches = new LinkedHashMap<>();
        noPhenotypeMatches.put(noMatchTerm, Collections.emptySet());
        TheoreticalModel noMatchModel = new TheoreticalModel(Organism.HUMAN, noPhenotypeMatches);

        assertThat(noMatchModel.getBestPhenotypeMatches(), equalTo(Collections.emptySet()));
        assertThat(noMatchModel.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(noMatchModel.getBestAvgScore(), equalTo(0.0));
        assertThat(noMatchModel.getMaxMatchScore(), equalTo(0.0));

        assertThat(noMatchModel.compare(4.0, 3.0), equalTo(1d));
        assertThat(noMatchModel.compare(0.0, 3.0), equalTo(0d));

    }

    @Test
    public void testWithSomeEmptyPhenotypeMatches() {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        phenotypeMatches.put(bigNose, Sets.newHashSet(perfectNoseMatch, noseMatch));
        phenotypeMatches.put(noMatchTerm, Collections.emptySet());
        TheoreticalModel matchModel = new TheoreticalModel(Organism.HUMAN, phenotypeMatches);

        assertThat(matchModel.getBestPhenotypeMatches(), equalTo(Sets.newHashSet(perfectNoseMatch)));
        assertThat(matchModel.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(matchModel.getBestAvgScore(), equalTo(4d));
        assertThat(matchModel.getMaxMatchScore(), equalTo(4d));

        assertThat(matchModel.compare(4d, 4d), equalTo(1d));
    }

    @Test
    public void testGetOrganism() throws Exception {
        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
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
        TheoreticalModel noMatchesInstance = new TheoreticalModel(Organism.HUMAN, Collections.emptyMap());
        assertThat(noMatchesInstance.getMaxMatchScore(), equalTo(0d));
    }

    @Test
    public void testGetBestAverageScore() {
        double expected = (bestToeMatch.getScore() + perfectNoseMatch.getScore()) / 2d;
        assertThat(instance.getBestAvgScore(), equalTo(expected));
    }

    @Test
    public void testGetBestAverageScoreNoMatches() {
        TheoreticalModel noMatchesInstance = new TheoreticalModel(Organism.HUMAN, Collections.emptyMap());
        assertThat(noMatchesInstance.getBestAvgScore(), equalTo(0d));
    }


    @Test
    public void compare() throws Exception {
        // maxMatchScore = 4.0
        // bestAvgScore = 3.0

        assertThat(instance.compare(0d, 0d), equalTo(0d));
        assertThat(instance.compare(2.0, 0.0), equalTo(0.25));
        assertThat(instance.compare(2.0, 1.5), equalTo(0.5));
        assertThat(instance.compare(4.0, 1.5), equalTo(0.75));
        assertThat(instance.compare(4.0, 3.0), equalTo(1d));

        assertThat(instance.compare(4.0, 4.0), equalTo(1d));
    }

}