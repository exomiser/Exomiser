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

package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityResultTest {

    private final GeneOrthologModel mouseModel = new GeneOrthologModel("mouse-model_1", Organism.MOUSE, 2263, "FGFR2", "MGI:95523", "Fgfr2", Collections.emptyList());
    private final GeneModelPhenotypeMatch geneModelPhenotypeMatch = new GeneModelPhenotypeMatch(0.827862024307251, mouseModel, Collections
            .emptyList());

    @Test
    public void testIsEquals() throws Exception {
        PhivePriorityResult result = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, geneModelPhenotypeMatch);
        PhivePriorityResult other = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, geneModelPhenotypeMatch);
        assertThat(result, equalTo(other));
    }

    @Test
    public void testToString() throws Exception {
        PhivePriorityResult result = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, geneModelPhenotypeMatch);
        assertThat(result.toString(), startsWith("PhivePriorityResult{geneId=2263, geneSymbol='FGFR2', score=0.827862024307251"));
    }

}