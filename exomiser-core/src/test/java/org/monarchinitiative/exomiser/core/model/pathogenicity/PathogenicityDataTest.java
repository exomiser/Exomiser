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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataTest {
    
    private PathogenicityData instance = PathogenicityData.EMPTY_DATA;
    
    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = SiftScore.valueOf(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = SiftScore.valueOf(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = PolyPhenScore.valueOf(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = PolyPhenScore.valueOf(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = MutationTasterScore.valueOf(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = MutationTasterScore.valueOf(MTASTER_FAIL_SCORE);

    @Test
    public void testEmptyData() {
        instance = PathogenicityData.EMPTY_DATA;
        assertThat(instance.hasPredictedScore(), is(false));
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(true));
        assertThat(instance.getMostPathogenicScore(), nullValue());
        assertThat(instance.getScore(), equalTo(0f));
        System.out.println(instance);
    }

    @Test
    public void testHasPredictedScore_returnsFalseWhenNullsUsedInConstructor() {
        instance = new PathogenicityData(null, null);
        assertThat(instance.hasPredictedScore(), is(false));
    }
    
    @Test
    public void testGetPredictedPathogenicityScores_isEmptyWhenNullsUsedInConstructor() {
        instance = new PathogenicityData(null, null);
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(true));
    }
    
    @Test
    public void testPathogenicityData_RemovesNullsUsedInConstructor() {
        instance = new PathogenicityData(POLYPHEN_FAIL, null);
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(false));
        assertThat(instance.getPredictedPathogenicityScores().size(), equalTo(1));
    }
    
    @Test
    public void testGetMostPathogenicScore_ReturnsNullWhenNoScorePresent() {
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, nullValue());
    }
    
    @Test
    public void testGetMostPathogenicScore_ReturnsOnlyScoreWhenOneScorePresent() {
        instance = new PathogenicityData(POLYPHEN_FAIL);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo(POLYPHEN_FAIL));
    }
    
    @Test
    public void testGetMostPathogenicScore_ReturnsMostPathogenicScore() {
        instance = new PathogenicityData(POLYPHEN_FAIL, SIFT_PASS);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo(SIFT_PASS));
    }
    
    @Test
    public void testGetPolyPhenScore() {
        instance = new PathogenicityData(POLYPHEN_FAIL);
        PolyPhenScore result = instance.getPolyPhenScore();
        assertThat(result, equalTo(POLYPHEN_FAIL));
    }

    @Test
    public void testGetMutationTasterScore() {
        instance = new PathogenicityData(MTASTER_PASS);
        MutationTasterScore result = instance.getMutationTasterScore();
        assertThat(result, equalTo(MTASTER_PASS));
    }
    
    @Test
    public void testGetSiftScore() {
        instance = new PathogenicityData(SIFT_FAIL);
        SiftScore result = instance.getSiftScore();
        assertThat(result, equalTo(SIFT_FAIL));
    }
    
    @Test
    public void testGetRemmScore() {
        instance = new PathogenicityData(RemmScore.valueOf(1f));
        RemmScore result = instance.getRemmScore();
        assertThat(result, equalTo(RemmScore.valueOf(1f)));
    }
    
    @Test
    public void testGetSiftScore_ReturnsNullWhenNoSiftScorePresent() {
        instance = new PathogenicityData();
        SiftScore result = instance.getSiftScore();
        assertThat(result, nullValue());
    }

    @Test
    public void testGetCaddScore() {
        CaddScore caddScore = CaddScore.valueOf(POLYPHEN_PASS_SCORE);
        instance = new PathogenicityData(caddScore);
        CaddScore result = instance.getCaddScore();
        assertThat(result, equalTo(caddScore));
    }

    @Test
    public void testGetPredictedPathogenicityScores() {
        instance = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        List<PathogenicityScore> expResult = new ArrayList<>();
        expResult.add(POLYPHEN_PASS);
        expResult.add(MTASTER_PASS);
        expResult.add(SIFT_FAIL);
        
        List<PathogenicityScore> result = instance.getPredictedPathogenicityScores();
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testGetPredictedPathogenicityScores_isImmutable() {
        instance = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        List<PathogenicityScore> expResult = new ArrayList<>();
        expResult.add(POLYPHEN_PASS);
        expResult.add(MTASTER_PASS);
        expResult.add(SIFT_FAIL);
        //try and add another score to the instance post-construction
        instance.getPredictedPathogenicityScores().add(SIFT_PASS);
        
        List<PathogenicityScore> result = instance.getPredictedPathogenicityScores();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testHasPredictedScore() {
        instance = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        boolean result = instance.hasPredictedScore();
        assertThat(result, is(true));
    }
    
    @Test
    public void testHasPredictedScoreForSource_isTrue() {
        instance = new PathogenicityData(POLYPHEN_PASS);
        boolean result = instance.hasPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, is(true));
    }
    
    @Test
    public void testHasPredictedScoreForSource_isFalse() {
        boolean result = instance.hasPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, is(false));
    }

    @Test
    public void testGetPredictedScore_scorePresent() {
        instance = new PathogenicityData(POLYPHEN_PASS);
        PathogenicityScore result =  instance.getPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result.getScore(), equalTo(POLYPHEN_PASS.getScore()));
        assertThat(result.getSource(), equalTo(POLYPHEN_PASS.getSource()));
        assertThat(result, equalTo(POLYPHEN_PASS));
    }
    
    @Test
    public void testGetPredictedScore_scoreMissingReturnsNull() {
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testHasNoPredictedScore() {
        boolean result = instance.hasPredictedScore();
        assertThat(result, is(false));
    }

    @Test
    public void testHashCodeEquals() {
        PathogenicityData otherInstance = new PathogenicityData();
        assertThat(instance.hashCode(), equalTo(otherInstance.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEquals() {
        PathogenicityData otherInstance = new PathogenicityData(MTASTER_FAIL);
        assertThat(instance.hashCode(), CoreMatchers.not(otherInstance.hashCode()));
    }

    @Test
    public void testNotEquals() {
        PathogenicityData otherInstance = new PathogenicityData(MTASTER_FAIL);
        assertThat(instance.equals(otherInstance), is(false));
    }

    @Test
    public void testGetScore_NoPredictedData() {
        assertThat(instance.getScore(), equalTo(0f));
    }

    @Test
    public void testGetScore_NonSiftPredictedData() {
        instance = new PathogenicityData(MTASTER_PASS, POLYPHEN_FAIL);
        assertThat(instance.getScore(), equalTo(MTASTER_PASS_SCORE));
    }

    @Test
    public void testGetScore_ReturnsNormalisedSiftScore() {
        instance = new PathogenicityData(MTASTER_FAIL, SIFT_PASS);
        assertThat(instance.getScore(), equalTo(1 - SIFT_PASS_SCORE));
    }

    @Test
    public void testEquals() {
        instance = new PathogenicityData(MTASTER_FAIL);
        PathogenicityData otherInstance = new PathogenicityData(MTASTER_FAIL);
        assertThat(instance.equals(otherInstance), is(true));
    }

    @Test
    public void testToString() {
        instance = new PathogenicityData(MTASTER_FAIL, POLYPHEN_PASS, SIFT_PASS);
        System.out.println(instance);
    }
}
