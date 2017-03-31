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

package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ArrayListMultimap;
import org.jblas.FloatMatrix;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneMatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveProteinInteractionScorerTest {

    //the Random-walk with restart (RWR) matrix used here is described in:
    //http://www.sciencedirect.com/science/article/pii/S0002929708001729
    // The matrix is "a global distance measure based on random walk with restart (RWR) to define similarity between genes
    // within this interaction network and to rank candidates on the basis of this similarity to known diseases genes.
    // Intuitively, the RWR algorithm calculates the similarity between two genes, i and j, on the basis of the
    // likelihood that a random walk through the interaction network starting at gene i will finish at gene j, whereby
    // all possible paths between the two genes are taken into account. In our implementation, we let the random walk
    // start with equal probability from each of the known disease-gene family members in order to search for an
    // additional family member in the linkage interval"

    //here we have 6 genes in two unconnected networks:
    // ((1-2-3-4), (5-6))
    // the probability of a walk hopping from one gene to its neighbour is

    float[][] matrix = {
            {0.7f, 0.1f, 0.2f, 0.3f}, //gene1
            {1.0f, 0.7f, 1.2f, 1.3f}, //gene2
            {2.0f, 2.1f, 0.7f, 2.3f}, //gene3
            {3.0f, 3.1f, 3.2f, 0.7f}  //gene4
    };
    FloatMatrix testMatrix = new FloatMatrix(matrix);


    @Test
    public void testEmpty() {
        HiPhiveProteinInteractionScorer instance = HiPhiveProteinInteractionScorer.EMPTY;
        assertThat(instance.getClosestPhenoMatchInNetwork(123), equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void testEmptyInputValues() {
        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(DataMatrix.EMPTY, ArrayListMultimap.create(), 0.0);
        assertThat(instance.getClosestPhenoMatchInNetwork(123), equalTo(GeneMatch.NO_HIT));
    }

}