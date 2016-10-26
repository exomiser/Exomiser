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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenixPriorityResultTest {

    @Test
    public void testResultsAreCorrectlyOrdered() {
        double maxSimilarityScore = 4.5;
        //int geneId, String geneSymbol, double negLogPVal, double semScore, double normalizationFactor
        PhenixPriorityResult best = new PhenixPriorityResult(1, "BEST", 1.0, -0.00001, maxSimilarityScore);
        PhenixPriorityResult middleGeneOne = new PhenixPriorityResult(2, "MIDDLE_A", 0.25, 0.002, 2.5);
        PhenixPriorityResult middleGeneTwo = new PhenixPriorityResult(3, "MIDDLE_B", 0.25, 0.002, 2.5);
        PhenixPriorityResult worst = new PhenixPriorityResult(4, "WORST", 0.001, 10.0, 0.01);

        List<PhenixPriorityResult> actual = Arrays.asList(middleGeneTwo, worst, middleGeneOne, best);
        Collections.sort(actual);

        assertThat(actual, equalTo(Arrays.asList(best, middleGeneOne, middleGeneTwo, worst)));
    }

}