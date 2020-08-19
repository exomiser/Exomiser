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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.gene;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class GeneModelTest {

    @Test
    void toOutputLineEmpty() {
        GeneModel instance = new GeneModel("Model:1", "GENE:1", "GENEONE", List.of());
        assertThat(instance.toOutputLine(), equalTo("GENE:1|GENEONE|Model:1|"));
    }

    @Test
    void toOutputLineUnderLimitIsUnchanged() {
        GeneModel instance = new GeneModel("Model:1", "GENE:1", "GENEONE", List.of("HP:0000012", "HP:0000034"));
        assertThat(instance.toOutputLine(), equalTo("GENE:1|GENEONE|Model:1|HP:0000012,HP:0000034"));
    }

    @Test
    void toOutputLineOverLimitIsTruncatedToNearestOntologyIdentifier() {
        String identifier = "HP:0000012";
        // this will lead to a string of (500 * identifier.length()) + (500 - 1)
        List<String> phenotypes = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            phenotypes.add(identifier);
        }

        GeneModel instance = new GeneModel("Model:1", "GENE:1", "GENEONE", phenotypes);
        String outputLine = instance.toOutputLine();
        String geneModelIdentifierPart = "GENE:1|GENEONE|Model:1|";
        assertThat(outputLine, startsWith(geneModelIdentifierPart + "HP:0000012,HP:0000012,"));
        assertThat(outputLine, endsWith("HP:0000012,HP:0000012"));
        assertTrue(outputLine.length() < (geneModelIdentifierPart.length() + 3000));
    }
}