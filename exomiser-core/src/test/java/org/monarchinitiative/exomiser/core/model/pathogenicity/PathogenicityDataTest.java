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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import com.google.common.collect.ImmutableList;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataTest {

    private static final PathogenicityData EMPTY_DATA = PathogenicityData.empty();
    
    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = SiftScore.of(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = SiftScore.of(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = PolyPhenScore.of(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = PolyPhenScore.of(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = MutationTasterScore.of(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = MutationTasterScore.of(MTASTER_FAIL_SCORE);

    @Test
    public void testEmptyData() {
        PathogenicityData instance = PathogenicityData.empty();
        assertThat(instance.hasPredictedScore(), is(false));
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(true));
        assertThat(instance.getMostPathogenicScore(), nullValue());
        assertThat(instance.getScore(), equalTo(0f));
        System.out.println(instance);
    }

    @Test
    public void testHasPredictedScoreReturnsFalseWhenNullsUsedInConstructor() {
        PathogenicityData instance = PathogenicityData.of(ClinVarData.empty(), Collections.emptySet());
        assertThat(instance.hasClinVarData(), is(false));
        assertThat(instance.hasPredictedScore(), is(false));
    }

    @Test
    public void testGetPredictedPathogenicityScoresIsEmptyWhenNullsUsedInConstructor() {
        PathogenicityData instance = PathogenicityData.of(ClinVarData.empty(), Collections.emptySet());
        assertThat(instance.getClinVarData().isEmpty(), is(true));
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(true));
    }

    @Test
    public void testPathogenicityDataRemovesNullsUsedInConstructor() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_FAIL, null);
        assertThat(instance.getPredictedPathogenicityScores().isEmpty(), is(false));
        assertThat(instance.getPredictedPathogenicityScores().size(), equalTo(1));
    }
    
    @Test
    public void testGetMostPathogenicScoreReturnsNullWhenNoScorePresent() {
        PathogenicityScore mostPathogenicScore = PathogenicityData.empty().getMostPathogenicScore();
        assertThat(mostPathogenicScore, nullValue());
    }
    
    @Test
    public void testGetMostPathogenicScoreReturnsOnlyScoreWhenOneScorePresent() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_FAIL);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo(POLYPHEN_FAIL));
    }

    @Test
    public void testConstructorWithCollectionReturnsMostPathogenicScore() {
        PathogenicityData instance = PathogenicityData.of(Arrays.asList(POLYPHEN_FAIL, SIFT_PASS));
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo(SIFT_PASS));
    }

    @Test
    public void testGetMostPathogenicScoreReturnsMostPathogenicScore() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_FAIL, SIFT_PASS);
        PathogenicityScore mostPathogenicScore = instance.getMostPathogenicScore();
        assertThat(mostPathogenicScore, equalTo(SIFT_PASS));
    }
    
    @Test
    public void testGetPolyPhenScore() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_FAIL);
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, equalTo(POLYPHEN_FAIL));
    }

    @Test
    public void testGetMutationTasterScore() {
        PathogenicityData instance = PathogenicityData.of(MTASTER_PASS);
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.MUTATION_TASTER);
        assertThat(result, equalTo(MTASTER_PASS));
    }
    
    @Test
    public void testGetSiftScore() {
        PathogenicityData instance = PathogenicityData.of(SIFT_FAIL);
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.SIFT);
        assertThat(result, equalTo(SIFT_FAIL));
    }
    
    @Test
    public void testGetRemmScore() {
        PathogenicityData instance = PathogenicityData.of(RemmScore.of(1f));
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.REMM);
        assertThat(result, equalTo(RemmScore.of(1f)));
    }

    @Test
    public void testGetCaddScore() {
        CaddScore caddScore = CaddScore.of(POLYPHEN_PASS_SCORE);
        PathogenicityData instance = PathogenicityData.of(caddScore);
        PathogenicityScore result = instance.getPredictedScore(PathogenicitySource.CADD);
        assertThat(result, equalTo(caddScore));
    }

    @Test
    public void testGetPredictedPathogenicityScores() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        List<PathogenicityScore> expResult = ImmutableList.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);

        List<PathogenicityScore> result = instance.getPredictedPathogenicityScores();
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testGetPredictedPathogenicityScoresIsImmutable() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        List<PathogenicityScore> expResult = ImmutableList.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        //try and add another score to the instance post-construction
        instance.getPredictedPathogenicityScores().add(SIFT_PASS);
        
        List<PathogenicityScore> result = instance.getPredictedPathogenicityScores();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testHasPredictedScore() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_FAIL);
        boolean result = instance.hasPredictedScore();
        assertThat(result, is(true));
    }
    
    @Test
    public void testHasPredictedScoreForSourceIsTrue() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_PASS);
        boolean result = instance.hasPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, is(true));
    }
    
    @Test
    public void testHasPredictedScoreForSourceIsFalse() {
        boolean result = EMPTY_DATA.hasPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result, is(false));
    }

    @Test
    public void testGetPredictedScoreWhenScorePresent() {
        PathogenicityData instance = PathogenicityData.of(POLYPHEN_PASS);
        PathogenicityScore result =  instance.getPredictedScore(PathogenicitySource.POLYPHEN);
        assertThat(result.getScore(), equalTo(POLYPHEN_PASS.getScore()));
        assertThat(result.getSource(), equalTo(POLYPHEN_PASS.getSource()));
        assertThat(result, equalTo(POLYPHEN_PASS));
    }
    
    @Test
    public void testGetPredictedScoreScoreMissingReturnsNull() {
        assertThat(EMPTY_DATA.getPredictedScore(PathogenicitySource.POLYPHEN), is(nullValue()));
    }

    @Test
    public void testIsEmpty() {
        assertThat(EMPTY_DATA.isEmpty(), is(true));
    }

    @Test
    public void testHasNoPredictedScore() {
        assertThat(EMPTY_DATA.hasPredictedScore(), is(false));
    }

    @Test
    public void testHasNoClinVarData() {
        assertThat(EMPTY_DATA.hasClinVarData(), is(false));
    }

    @Test
    public void testEmptyClinVarData() {
        assertThat(EMPTY_DATA.getClinVarData(), equalTo(ClinVarData.empty()));
    }

    @Test
    public void testClinVarData() {
        ClinVarData clinVarData = ClinVarData.builder().alleleId("12345").primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC).build();
        PathogenicityData instance = PathogenicityData.of(clinVarData, POLYPHEN_PASS);
        assertThat(instance.hasClinVarData(), is(true));
        assertThat(instance.getClinVarData(), equalTo(clinVarData));
    }

    @Test
    public void testHashCodeEquals() {
        PathogenicityData otherInstance = PathogenicityData.empty();
        assertThat(EMPTY_DATA.hashCode(), equalTo(otherInstance.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEquals() {
        PathogenicityData otherInstance = PathogenicityData.of(MTASTER_FAIL);
        assertThat(EMPTY_DATA.hashCode(), CoreMatchers.not(otherInstance.hashCode()));
    }

    @Test
    public void testNotEquals() {
        PathogenicityData otherInstance = PathogenicityData.of(MTASTER_FAIL);
        assertThat(EMPTY_DATA.equals(otherInstance), is(false));
    }

    @Test
    public void testGetScoreNoPredictedData() {
        assertThat(PathogenicityData.empty().getScore(), equalTo(0f));
    }

    @Test
    public void testGetScoreNonSiftPredictedData() {
        PathogenicityData instance = PathogenicityData.of(MTASTER_PASS, POLYPHEN_FAIL);
        assertThat(instance.getScore(), equalTo(MTASTER_PASS_SCORE));
    }

    @Test
    public void testGetScoreReturnsNormalisedSiftScore() {
        PathogenicityData instance = PathogenicityData.of(MTASTER_FAIL, SIFT_PASS);
        assertThat(instance.getScore(), equalTo(1 - SIFT_PASS_SCORE));
    }

    @Test
    public void testEquals() {
        PathogenicityData instance = PathogenicityData.of(MTASTER_FAIL);
        PathogenicityData otherInstance = PathogenicityData.of(MTASTER_FAIL);
        assertThat(instance.equals(otherInstance), is(true));
    }

    @Test
    public void testToString() {
        PathogenicityData instance = PathogenicityData.of(MTASTER_FAIL, POLYPHEN_PASS, SIFT_PASS);
        System.out.println(instance);
    }
}
