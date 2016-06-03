package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriority;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Created by jules on 03/06/2016.
 */
public class OrganismPhenotypeMatchesTest {

    @Test
    public void emptyInputValues() throws Exception {
        OrganismPhenotypeMatches instance = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());

        assertThat(instance.getOrganism(), equalTo(Organism.HUMAN));
        assertThat(instance.getBestPhenotypeMatches(), equalTo(Collections.emptySet()));
        assertThat(instance.getQueryTerms(), equalTo(Collections.emptyList()));
        assertThat(instance.getTermPhenotypeMatches(), equalTo(Collections.emptyMap()));
        assertThat(instance.getCompoundKeyIndexedPhenotypeMatches(), equalTo(Collections.emptyMap()));
    }

    @Test
    public void testEquals() {
        OrganismPhenotypeMatches emptyHumanOne = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());
        OrganismPhenotypeMatches emptyMouseOne = new OrganismPhenotypeMatches(Organism.MOUSE, Collections.emptyMap());
        OrganismPhenotypeMatches emptyHumanTwo = new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap());
        assertThat(emptyHumanOne, equalTo(emptyHumanTwo));
        assertThat(emptyHumanOne, not(equalTo(emptyMouseOne)));
    }

    @Test
    public void testToString() {
        System.out.println(new OrganismPhenotypeMatches(Organism.HUMAN, Collections.emptyMap()));
    }

    @Test
    public void testGetBestPhenotypeMatches() throws Exception {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> phenotypeMatches = new LinkedHashMap<>();
        PhenotypeTerm bigNose = new PhenotypeTerm("HP:0000001", "Big nose", 2.0);
        PhenotypeTerm nose = new PhenotypeTerm("HP:0000002", "Nose", 1.0);
        PhenotypeTerm littleNose = new PhenotypeTerm("HP:0000003", "Little nose", 2.0);

        PhenotypeMatch perfectNoseMatch = new PhenotypeMatch(bigNose, bigNose, 1d, 2d, bigNose);
        PhenotypeMatch noseMatch = new PhenotypeMatch(bigNose, littleNose, 0.5, 1d, nose);

        Set<PhenotypeMatch> matches = new HashSet<>();
        matches.add(perfectNoseMatch);
        matches.add(noseMatch);
        phenotypeMatches.put(bigNose, matches);

        OrganismPhenotypeMatches instance = new OrganismPhenotypeMatches(Organism.HUMAN, phenotypeMatches);
        System.out.println(instance);

        Set<PhenotypeMatch> bestMatches = new HashSet<>();
        bestMatches.add(perfectNoseMatch);

        assertThat(instance.getBestPhenotypeMatches(), equalTo(bestMatches));

    }
}