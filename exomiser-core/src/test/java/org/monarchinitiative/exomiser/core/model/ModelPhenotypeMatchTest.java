package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ModelPhenotypeMatchTest {

    @Test
    public void testCreateType() {
        ModelPhenotypeMatch mouseGeneOrthologMatch = new ModelPhenotypeMatch(1.0, null, Collections.emptyList());
        ModelPhenotypeMatch diseaseGeneOrthologMatch = new ModelPhenotypeMatch(1.0, null, Collections.emptyList());

        List<ModelPhenotypeMatch> modelPhenotypeMatches = Arrays.asList(mouseGeneOrthologMatch, diseaseGeneOrthologMatch);
        modelPhenotypeMatches.forEach(System.out::println);
    }

}