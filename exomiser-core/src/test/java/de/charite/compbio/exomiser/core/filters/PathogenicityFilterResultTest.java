/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilterResult;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static de.charite.compbio.exomiser.core.filters.FilterResultStatus.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityFilterResultTest{


    @Before
    public void setUp() throws Exception {
    }
    
    
    @Test
    public void testScoreIsSetByConstructor() {
        float expectedScore = 1.0f;
        PathogenicityFilterResult instance = new PathogenicityFilterResult(expectedScore, PASS);
        assertThat(instance.getScore(), equalTo(expectedScore));
    }

    @Test
    public void testNotEqualsNull() {
        Object obj = null;
        PathogenicityFilterResult instance = new PathogenicityFilterResult(1.0f, PASS);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testNotEqualsOtherScoreValue() {
        FilterResult obj = new PathogenicityFilterResult(0.9f, PASS);
        PathogenicityFilterResult instance = new PathogenicityFilterResult(1.0f, PASS);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testNotEqualsOtherScoreClass() {
        FilterResult obj = new FrequencyFilterResult(0.45f, PASS);
        PathogenicityFilterResult instance = new PathogenicityFilterResult(0.45f, PASS);
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testEqualsOtherScoreClass() {
        FilterResult obj = new PathogenicityFilterResult(0.99f, PASS);
        PathogenicityFilterResult instance = new PathogenicityFilterResult(0.99f, PASS);
        assertThat(instance.equals(obj), is(true));
    }

    @Test
    public void testToString() {
        PathogenicityFilterResult instance = new PathogenicityFilterResult(0.04999f, PASS);
        String expResult = "Filter=Pathogenicity score=0.050 status=PASS";
        assertThat(instance.toString(), equalTo(expResult));
    }

}
