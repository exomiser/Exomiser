/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.pathogenicity;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataTest {
    
    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = new SiftScore(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = new SiftScore(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = new PolyPhenScore(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = new PolyPhenScore(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = new MutationTasterScore(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = new MutationTasterScore(MTASTER_FAIL_SCORE);


    public PathogenicityDataTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testGetMostPathogenicScoreWhenNoScorePresent() {
        PathogenicityData instance = new PathogenicityData(null, null, null, null);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, nullValue());
    }
    
    @Test
    public void testGetMostPathogenicScoreWhenOneScorePresent() {
        PathogenicityData instance = new PathogenicityData(POLYPHEN_FAIL, null, null, null);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo((PathogenicityScore) POLYPHEN_FAIL));
    }
    
    @Test
    public void testGetPolyPhenScore() {
        PathogenicityData instance = new PathogenicityData(POLYPHEN_FAIL, null, null, null);
        PolyPhenScore result = instance.getPolyPhenScore();
        assertThat(result, equalTo(POLYPHEN_FAIL));
    }

    @Test
    public void testGetMutationTasterScore() {
        PathogenicityData instance = new PathogenicityData(null, MTASTER_PASS, null, null);
        MutationTasterScore result = instance.getMutationTasterScore();
        assertThat(result, equalTo(MTASTER_PASS));
    }

    @Test
    public void testGetSiftScore() {
        PathogenicityData instance = new PathogenicityData(null, null, SIFT_FAIL, null);
        SiftScore result = instance.getSiftScore();
        assertThat(result, equalTo(SIFT_FAIL));
    }

    @Test
    public void testGetCaddScore() {
        CaddScore caddScore = new CaddScore(POLYPHEN_PASS_SCORE);
        PathogenicityData instance = new PathogenicityData(null, null, null, caddScore);
        CaddScore result = instance.getCaddScore();
        assertThat(result, equalTo(caddScore));
    }

    @Test
    public void testGetPredictedPathogenicityScores() {
        PathogenicityData instance = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL, null);
        List<PathogenicityScore> expResult = new ArrayList<>();
        expResult.add(POLYPHEN_PASS);
        expResult.add(MTASTER_PASS);
        expResult.add(SIFT_FAIL);
        
        List<PathogenicityScore> result = instance.getPredictedPathogenicityScores();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testHasPredictedScore() {
        PathogenicityData instance = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL, null);
        boolean result = instance.hasPredictedScore();
        assertThat(result, is(true));
    }
    
    @Test
    public void testHasNoPredictedScore() {
        PathogenicityData instance = new PathogenicityData(null, null, null, null);
        boolean result = instance.hasPredictedScore();
        assertThat(result, is(false));
    }

    @Test
    public void testHashCodeEquals() {
        PathogenicityData instance = new PathogenicityData(null, null, null, null);
        PathogenicityData otherInstance = new PathogenicityData(null, null, null, null);
        assertThat(instance.hashCode(), equalTo(otherInstance.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEquals() {
        PathogenicityData instance = new PathogenicityData(null, null, null, null);
        PathogenicityData otherInstance = new PathogenicityData(null, MTASTER_FAIL, null, null);
        assertThat(instance.hashCode(), not(otherInstance.hashCode()));
    }

    @Test
    public void testNotEquals() {
        PathogenicityData instance = new PathogenicityData(null, null, null, null);
        PathogenicityData otherInstance = new PathogenicityData(null, MTASTER_FAIL, null, null);
        assertThat(instance.equals(otherInstance), is(false));
    }
    
    @Test
    public void testEquals() {
        PathogenicityData instance = new PathogenicityData(null, MTASTER_FAIL, null, null);
        PathogenicityData otherInstance = new PathogenicityData(null, MTASTER_FAIL, null, null);
        assertThat(instance.equals(otherInstance), is(true));
    }

}
