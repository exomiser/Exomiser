/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePrioritiserTest {
    
    private NoneTypePrioritiser instance;
    
    @Before
    public void setUp() {
        instance = new NoneTypePrioritiser();
    }

    @Test
    public void testRunAnalysisHasNoEffectOnGenes() {
        instance.prioritizeGenes(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void testGetPriorityTypeReturnsNoneType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.NONE));
    }
    
    @Test
    public void testToString(){
        assertThat(instance.toString(), equalTo("NoneTypePrioritiser{}"));
    }
}
