/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.writers.VariantTypeCount;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

// TODO(holtgrew): Rename to VariantEffect*

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantTypeCountTest {
    
    private VariantTypeCount instance;
    
    private VariantEffect effect;
    private List<Integer> counts;
    
    @Before
    public void setUp() {
        
        effect = VariantEffect.MISSENSE_VARIANT;
        
        counts = new ArrayList<>();
        counts.addAll(Arrays.asList(1, 2, 3));
        
        instance = new VariantTypeCount(effect, counts);
    }

    @Test
    public void testGetVariantType() {
        assertThat(instance.getVariantType(), equalTo(effect));
    }

    @Test
    public void testGetSampleVariantTypeCounts() {
        assertThat(instance.getSampleVariantTypeCounts(), equalTo(counts));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("MISSENSE_VARIANT=[1, 2, 3]"));
    }
    
}
