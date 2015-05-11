/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.pathogenicity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jues.jacobsen@sanger.ac.uk>
 */
public class SiftScoreTest {
    
    private SiftScore instance;
    
    //Higher scores are more pathogenic so this is the reverse of what's normal
    //a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    private static final int MORE_PATHOGENIC = AbstractPathogenicityScore.MORE_PATHOGENIC;
    private static final int EQUALS = AbstractPathogenicityScore.EQUALS;
    private static final int LESS_PATHOGENIC = AbstractPathogenicityScore.LESS_PATHOGENIC;
    
    private static final float SIFT_PATHOGENIC_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_NON_PATHOGENIC_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;
       
    @Test
    public void testGetSource() {
        instance = new SiftScore(SIFT_PATHOGENIC_SCORE);
        assertThat(instance.getSource(), equalTo(PathogenicitySource.SIFT));
    }
    
    @Test
    public void testToStringTolerated() {
        instance = new SiftScore(0.1f);
        assertThat(instance.toString(), equalTo("SIFT: 0.100 (T)"));
    }
    
    @Test
    public void testToStringDamaging() {
        instance = new SiftScore(SIFT_PATHOGENIC_SCORE);
        assertThat(instance.toString(), equalTo(String.format("SIFT: %.3f (D)", SIFT_PATHOGENIC_SCORE)));
    }
    
    @Test
    public void testNotEqualToAnotherSiftScore() {
        SiftScore pathogenic = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = new SiftScore(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(nonPathogenic.equals(pathogenic), is(false));
    }
    
    @Test
    public void testEqualToAnotherSiftScore() {
        SiftScore sift = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore anotherPathogenic = new SiftScore(SIFT_PATHOGENIC_SCORE);
        assertThat(anotherPathogenic.equals(sift), is(true));
    }
    
    @Test
    public void testNotEqualToAnotherPathogenicityScoreWithSameScore() {
        SiftScore sift = new SiftScore(SIFT_PATHOGENIC_SCORE);
        PathogenicityScore poly = new PolyPhenScore(SIFT_PATHOGENIC_SCORE);
        assertThat(sift.equals(poly), is(false));
    }
    
    @Test
    public void testCompareToAfterAgainstAnotherSiftScore() {
        SiftScore pathogenic = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = new SiftScore(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(pathogenic.compareTo(nonPathogenic), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToBeforeAgainstAnotherSiftScore() {
        SiftScore pathogenic = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = new SiftScore(SIFT_NON_PATHOGENIC_SCORE);
        assertThat(nonPathogenic.compareTo(pathogenic), equalTo(LESS_PATHOGENIC));
    }
    
    @Test
    public void testCompareToEqualsAgainstAnotherSiftScore() {
        SiftScore sift = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore equalScoreSift = new SiftScore(SIFT_PATHOGENIC_SCORE);
        assertThat(sift.compareTo(equalScoreSift), equalTo(EQUALS));
    }
    
    @Test(expected = NullPointerException.class)
    public void testCompareToAfterAgainstANullSiftScoreThrowsANullPointer() {
        SiftScore pathogenic = new SiftScore(SIFT_PATHOGENIC_SCORE);
        SiftScore nonPathogenic = null;
        assertThat(pathogenic.compareTo(nonPathogenic), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToAfterAnotherPathogenicityScore() {
        PathogenicityScore sift = new SiftScore(0.999f);
        PathogenicityScore poly = new PolyPhenScore(0.999f);
        assertThat(sift.compareTo(poly), equalTo(LESS_PATHOGENIC));
    }
    
    @Test
    public void testCompareToBeforeAnotherPathogenicityScore() {
        PathogenicityScore sift = new SiftScore(0.01f);
        PathogenicityScore poly = new MutationTasterScore(0.01f);
        assertThat(sift.compareTo(poly), equalTo(MORE_PATHOGENIC));
    }
    
    @Test
    public void testCompareToEqualsAnotherPathogenicityScore() {
        PathogenicityScore sift = new SiftScore(0.4f);
        PathogenicityScore poly = new PolyPhenScore(0.6f);
        assertThat(sift.compareTo(poly), equalTo(EQUALS));
    }
    
    @Test
    public void testAnotherPathogenicityScoreCompareToEquals() {
        PathogenicityScore sift = new SiftScore(0.4f);
        PathogenicityScore poly = new PolyPhenScore(0.6f);
        assertThat(poly.compareTo(sift), equalTo(EQUALS));
    }
}
