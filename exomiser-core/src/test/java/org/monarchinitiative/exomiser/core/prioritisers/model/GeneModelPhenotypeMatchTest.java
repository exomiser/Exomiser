package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GeneModelPhenotypeMatchTest {

    @Test
    public void testCreateType() {
        GeneModelPhenotypeMatch mouseGeneOrthologMatch = new GeneModelPhenotypeMatch(1.0, null, Collections.emptyList());
        GeneModelPhenotypeMatch diseaseGeneOrthologMatch = new GeneModelPhenotypeMatch(1.0, null, Collections.emptyList());

        List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = Arrays.asList(mouseGeneOrthologMatch, diseaseGeneOrthologMatch);
        geneModelPhenotypeMatches.forEach(System.out::println);
    }

}