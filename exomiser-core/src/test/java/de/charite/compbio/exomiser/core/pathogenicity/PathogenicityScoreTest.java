/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.pathogenicity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class PathogenicityScoreTest {
    
    public PathogenicityScoreTest() {
    }
    
    @Before
    public void setUp() {
    }

    
    @Test
    public void testComparableOnlySiftScores() {
        SiftScore mostPathogenic = new SiftScore(0.001f);
        SiftScore belowThreshold = new SiftScore(SiftScore.SIFT_THRESHOLD + 0.01f);
        SiftScore aboveThreshold = new SiftScore(SiftScore.SIFT_THRESHOLD - 0.01f);
        SiftScore leastPathogenic = new SiftScore(0.999f);
        
        
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
        SiftScore mostPathogenic = new SiftScore(0.001f);
        SiftScore leastPathogenic = new SiftScore(0.998f);
        PolyPhenScore overThreshold = new PolyPhenScore(PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f);
        PolyPhenScore belowThreshold = new PolyPhenScore(PolyPhenScore.POLYPHEN_THRESHOLD - 0.01f);
        MutationTasterScore damaging = new MutationTasterScore(MutationTasterScore.MTASTER_THRESHOLD + 0.001f);
        MutationTasterScore notdamaging = new MutationTasterScore(MutationTasterScore.MTASTER_THRESHOLD - 0.001f);
        
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

        PolyPhenScore overThreshold = new PolyPhenScore(PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f);
        PolyPhenScore belowThreshold = new PolyPhenScore(PolyPhenScore.POLYPHEN_THRESHOLD - 0.01f);
        MutationTasterScore damaging = new MutationTasterScore(MutationTasterScore.MTASTER_THRESHOLD + 0.001f);
        
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
        PolyPhenScore polyPhenScore = new PolyPhenScore(1);
        PolyPhenScore nullScore = null;
        
        polyPhenScore.compareTo(nullScore);
    }

}
