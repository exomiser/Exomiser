/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.pathogenicity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MutationTasterScoreTest {
    
    private final MutationTasterScore instance = MutationTasterScore.valueOf(1.0f);

    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.MUTATION_TASTER));
    }

    @Test
    public void testNotEqualsOtherSourceType() {
        MutationTasterScore mutationTasterScore = MutationTasterScore.valueOf(0f);
        SiftScore siftScore = SiftScore.valueOf(0f);
        assertThat(mutationTasterScore.equals(siftScore), is(false));
    }

    @Test
    public void testEqualsSameSourceType() {
        MutationTasterScore first = MutationTasterScore.valueOf(0f);
        MutationTasterScore other = MutationTasterScore.valueOf(0f);
        assertThat(first.equals(other), is(true));
    }
    
}
