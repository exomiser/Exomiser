/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PolyPhenScoreTest {
    
    private PolyPhenScore instance;
    private final float pathogenicScore = 1f;
    private final float nonPathogenicScore = 0f;
    
    @BeforeEach
    public void setUp() {
        instance = PolyPhenScore.of(pathogenicScore);
    }

    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.POLYPHEN));
    }
    
    @Test
    public void testCompareToBefore() {
        PolyPhenScore nonPathogenicPolyphen = PolyPhenScore.of(nonPathogenicScore);
        assertThat(instance.compareTo(nonPathogenicPolyphen), equalTo(-1));
    }
    
    @Test
    public void testCompareToAfter() {
        PolyPhenScore nonPathogenicPolyphen = PolyPhenScore.of(nonPathogenicScore);
        assertThat(nonPathogenicPolyphen.compareTo(instance), equalTo(1));
    }
    
    @Test
    public void testCompareToEquals() {
        PolyPhenScore sameScore = PolyPhenScore.of(pathogenicScore);
        assertThat(instance.compareTo(sameScore), equalTo(0));
    }
}
