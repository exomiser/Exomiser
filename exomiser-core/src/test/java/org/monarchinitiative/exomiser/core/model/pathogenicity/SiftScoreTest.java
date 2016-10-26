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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jues.jacobsen@sanger.ac.uk>
 */
public class SiftScoreTest {
    
    private SiftScore instance;
    
    //Higher scores are more pathogenic so this is the reverse of what's normal
    //a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    private static final int MORE_PATHOGENIC = BasePathogenicityScore.MORE_PATHOGENIC;
    private static final int EQUALS = BasePathogenicityScore.EQUALS;
    private static final int LESS_PATHOGENIC = BasePathogenicityScore.LESS_PATHOGENIC;
    
    private static final float SIFT_PATHOGENIC_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_NON_PATHOGENIC_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;
       
    @Test
    public void testGetSource() {
        instance = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        assertThat(instance.getSource(), equalTo(PathogenicitySource.SIFT));
    }
    
    @Test
    public void testToStringTolerated() {
        instance = SiftScore.valueOf(0.1f);
        assertThat(instance.toString(), equalTo("SIFT: 0.100 (T)"));
    }
    
    @Test
    public void testToStringDamaging() {
        instance = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        assertThat(instance.toString(), equalTo(String.format("SIFT: %.3f (D)", SIFT_PATHOGENIC_SCORE)));
    }
    
    @Test
    public void testNotEqualToAnotherSiftScore() {
        SiftScore pathogenic = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = SiftScore.valueOf(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(nonPathogenic.equals(pathogenic), is(false));
    }
    
    @Test
    public void testEqualToAnotherSiftScore() {
        SiftScore sift = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore anotherPathogenic = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        assertThat(anotherPathogenic.equals(sift), is(true));
    }
    
    @Test
    public void testNotEqualToAnotherPathogenicityScoreWithSameScore() {
        SiftScore sift = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        PathogenicityScore poly = PolyPhenScore.valueOf(SIFT_PATHOGENIC_SCORE);
        assertThat(sift.equals(poly), is(false));
    }
    
    @Test
    public void testCompareToAfterAgainstAnotherSiftScore() {
        SiftScore pathogenic = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = SiftScore.valueOf(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(pathogenic.compareTo(nonPathogenic), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToBeforeAgainstAnotherSiftScore() {
        SiftScore pathogenic = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = SiftScore.valueOf(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(nonPathogenic.compareTo(pathogenic), equalTo(LESS_PATHOGENIC));
    }
    
    @Test
    public void testCompareToEqualsAgainstAnotherSiftScore() {
        SiftScore sift = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore equalScoreSift = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        assertThat(sift.compareTo(equalScoreSift), equalTo(EQUALS));
    }
    
    @Test(expected = NullPointerException.class)
    public void testCompareToAfterAgainstANullSiftScoreThrowsANullPointer() {
        SiftScore pathogenic = SiftScore.valueOf(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = null;
        assertThat(pathogenic.compareTo(nonPathogenic), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToAfterAnotherPathogenicityScore() {
        PathogenicityScore sift = SiftScore.valueOf(0.999f);
        PathogenicityScore poly = PolyPhenScore.valueOf(0.999f);
        assertThat(sift.compareTo(poly), equalTo(LESS_PATHOGENIC));
    }
    
    @Test
    public void testCompareToBeforeAnotherPathogenicityScore() {
        PathogenicityScore sift = SiftScore.valueOf(0.01f);
        PathogenicityScore poly = MutationTasterScore.valueOf(0.01f);
        assertThat(sift.compareTo(poly), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToEqualsAnotherPathogenicityScore() {
        PathogenicityScore sift = SiftScore.valueOf(0.4f);
        PathogenicityScore poly = PolyPhenScore.valueOf(0.6f);
        assertThat(sift.compareTo(poly), equalTo(EQUALS));
    }
    
    @Test
    public void testAnotherPathogenicityScoreCompareToEquals() {
        PathogenicityScore sift = SiftScore.valueOf(0.4f);
        PathogenicityScore poly = PolyPhenScore.valueOf(0.6f);
        assertThat(poly.compareTo(sift), equalTo(EQUALS));
    }
}
