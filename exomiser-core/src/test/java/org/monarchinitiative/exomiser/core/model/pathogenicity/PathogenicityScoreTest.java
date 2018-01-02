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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jj8
 */
public class PathogenicityScoreTest {

    @Test
    public void testComparableOnlySiftScores() {
        SiftScore mostPathogenic = SiftScore.valueOf(0.001f);
        SiftScore belowThreshold = SiftScore.valueOf(SiftScore.SIFT_THRESHOLD + 0.01f);
        SiftScore aboveThreshold = SiftScore.valueOf(SiftScore.SIFT_THRESHOLD - 0.01f);
        SiftScore leastPathogenic = SiftScore.valueOf(0.999f);
        
        
        List<PathogenicityScore> scores = new ArrayList<>();
        scores.add(leastPathogenic);
        scores.add(mostPathogenic);
        scores.add(belowThreshold);
        scores.add(aboveThreshold);
        
        List<PathogenicityScore> expected = new ArrayList<>();
        expected.add(mostPathogenic);
        expected.add(aboveThreshold);
        expected.add(belowThreshold);
        expected.add(leastPathogenic);

        Collections.sort(scores);
        
        assertThat(scores, equalTo(expected));
        
    }
    
    @Test
    public void testComparableWithSiftScores() {
        SiftScore mostPathogenic = SiftScore.valueOf(0.001f);
        SiftScore leastPathogenic = SiftScore.valueOf(0.998f);
        PolyPhenScore overThreshold = PolyPhenScore.valueOf(PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f);
        PolyPhenScore belowThreshold = PolyPhenScore.valueOf(PolyPhenScore.POLYPHEN_THRESHOLD - 0.01f);
        MutationTasterScore damaging = MutationTasterScore.valueOf(MutationTasterScore.MTASTER_THRESHOLD + 0.001f);
        MutationTasterScore notdamaging = MutationTasterScore.valueOf(MutationTasterScore.MTASTER_THRESHOLD - 0.001f);
        
        List<PathogenicityScore> scores = new ArrayList<>();
        scores.add(damaging);
        scores.add(leastPathogenic);
        scores.add(mostPathogenic);
        scores.add(notdamaging);
        scores.add(belowThreshold);
        scores.add(overThreshold);
        
        Collections.sort(scores);

        List<PathogenicityScore> expected = new ArrayList<>();
        expected.add(mostPathogenic);
        expected.add(damaging);
        expected.add(notdamaging);
        expected.add(overThreshold);
        expected.add(belowThreshold);
        expected.add(leastPathogenic);
 
        assertThat(scores, equalTo(expected));
        
    }
    
    @Test
    public void testComparableNoSiftScores() {

        PolyPhenScore overThreshold = PolyPhenScore.valueOf(PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f);
        PolyPhenScore belowThreshold = PolyPhenScore.valueOf(PolyPhenScore.POLYPHEN_THRESHOLD - 0.01f);
        MutationTasterScore damaging = MutationTasterScore.valueOf(MutationTasterScore.MTASTER_THRESHOLD + 0.001f);
        
        List<PathogenicityScore> scores = new ArrayList<>();
        scores.add(damaging);
        scores.add(belowThreshold);
        scores.add(overThreshold);
        
        List<PathogenicityScore> expected = new ArrayList<>();
        expected.add(damaging);
        expected.add(overThreshold);
        expected.add(belowThreshold);

        Collections.sort(scores);
        
        assertThat(scores, equalTo(expected));
        
    }
    
    @Test(expected = NullPointerException.class)
    public void comparingAPathogenicityScoreToANullThrowsANullPOinterException() {
        PolyPhenScore polyPhenScore = PolyPhenScore.valueOf(1);
        PolyPhenScore nullScore = null;
        
        polyPhenScore.compareTo(nullScore);
    }

}
