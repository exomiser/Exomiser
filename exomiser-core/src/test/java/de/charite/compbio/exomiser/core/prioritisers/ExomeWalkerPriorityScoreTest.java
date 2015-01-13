/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen (jules.jacobsen@sanger.ac.uk)
 */
public class ExomeWalkerPriorityScoreTest {
    
    private ExomeWalkerPriorityScore instance;
    private final float score = 1.0f;
    
    @Before
    public void setUp() {
        instance =  new ExomeWalkerPriorityScore(score);
    }

    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.EXOMEWALKER_PRIORITY));
    }

    @Test
    public void testGetScore() {
        assertThat(instance.getScore(), equalTo(score));
    }

    @Test
    public void testNoPPIDataScoreHasScoreOfZero() {
        ExomeWalkerPriorityScore noInteractionScore = ExomeWalkerPriorityScore.noPPIDataScore();
        assertThat(noInteractionScore.getScore(), equalTo(0.0f));
    }

    @Test
    public void testGetHTMLCode() {
    }

    @Test
    public void testGetRawScore() {
        assertThat(instance.getRawScore(), equalTo((double) score));
    }
    
    @Test
    public void testRawScoreIsUnchangedWhenScoreIsSet() {
        instance.setScore(0.5f);
        assertThat(instance.getRawScore(), equalTo((double) score));
    }

    @Test
    public void testGetScaledScore() {
        assertThat(instance.getScaledScore(), equalTo(-10d));
    }

    @Test
    public void testGetScaledScoreIsChangedToNewScoreWhenScoreIsSet() {
        float newScore = 0.5f;
        instance.setScore(newScore);
        assertThat(instance.getScaledScore(), equalTo((double) newScore));
    }    
    
    @Test
    public void testSetScore() {
        float newScore = 0.5f;
        instance.setScore(newScore);
        assertThat(instance.getScore(), equalTo(newScore));
    }

    @Test
    public void testGetFilterResultListIsEmpty() {
        assertThat(instance.getFilterResultList().isEmpty(), is(true));
    }
    
}
