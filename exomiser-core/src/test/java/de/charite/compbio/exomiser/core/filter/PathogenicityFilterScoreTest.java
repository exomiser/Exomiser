/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FrequencyFilterScore;
import de.charite.compbio.exomiser.core.filter.PathogenicityFilterScore;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class PathogenicityFilterScoreTest{


    @Before
    public void setUp() throws Exception {
    }
    
    
    @Test
    public void testScoreIsSetByConstructor() {
        float expectedScore = 1.0f;
        PathogenicityFilterScore instance = new PathogenicityFilterScore(expectedScore);
        assertThat(instance.getScore(), equalTo(expectedScore));
    }

    @Test
    public void testNotEqualsNull() {
        Object obj = null;
        PathogenicityFilterScore instance = new PathogenicityFilterScore(1.0f);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testNotEqualsOtherScoreValue() {
        FilterScore obj = new PathogenicityFilterScore(0.9f);
        PathogenicityFilterScore instance = new PathogenicityFilterScore(1.0f);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testNotEqualsOtherScoreClass() {
        FilterScore obj = new FrequencyFilterScore(0.45f);
        PathogenicityFilterScore instance = new PathogenicityFilterScore(0.45f);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testEqualsOtherScoreClass() {
        FilterScore obj = new PathogenicityFilterScore(0.99f);
        PathogenicityFilterScore instance = new PathogenicityFilterScore(0.99f);
        assertThat(instance.equals(obj), is(true));
    }

    @Test
    public void testToString() {
        PathogenicityFilterScore instance = new PathogenicityFilterScore(0.04999f);
        String expResult = "Path score: 0.050";
        assertThat(instance.toString(), equalTo(expResult));
    }
    
    

}
