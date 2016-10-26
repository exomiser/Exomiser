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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.pathogenicity;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PolyPhenScoreTest {
    
    private PolyPhenScore instance;
    private final float pathogenicScore = 1f;
    private final float nonPathogenicScore = 0f;
    
    @Before
    public void setUp() {
        instance = PolyPhenScore.valueOf(pathogenicScore);
    }

    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.POLYPHEN));
    }
    
    @Test
    public void testCompareTo_Before() {
        PolyPhenScore nonPathogenicPolyphen = PolyPhenScore.valueOf(nonPathogenicScore);
        assertThat(instance.compareTo(nonPathogenicPolyphen), equalTo(-1));
    }
    
    @Test
    public void testCompareTo_After() {
        PolyPhenScore nonPathogenicPolyphen = PolyPhenScore.valueOf(nonPathogenicScore);
        assertThat(nonPathogenicPolyphen.compareTo(instance), equalTo(1));
    }
    
    @Test
    public void testCompareTo_Equals() {
        PolyPhenScore sameScore = PolyPhenScore.valueOf(pathogenicScore);
        assertThat(instance.compareTo(sameScore), equalTo(0));
    }
}
