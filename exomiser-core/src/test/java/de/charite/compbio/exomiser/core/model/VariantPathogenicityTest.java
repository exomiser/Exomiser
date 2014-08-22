/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.model.VariantPathogenicity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantPathogenicityTest {
    
    public VariantPathogenicityTest() {
    }

    /**
     * Test of maxPathogenicity method, of class VariantPathogenicity.
     */
    @Test
    public void testMaxPathogenicitySift() {
        float siftScore = 0.1f;
        float polyPhenScore = 0.2f;
        float mutTasterScore = 0.3f;
        VariantPathogenicity instance = new VariantPathogenicity(1, 2, 'A', 'B', 'C', 'D', 3, siftScore, polyPhenScore, mutTasterScore, 0.4f,0.4f,0.4f);
        float expResult = 1 - siftScore;
        float result = instance.maxPathogenicity();
        assertEquals(expResult, result, 0.01);
    }
    
    /**
     * Test of maxPathogenicity method, of class VariantPathogenicity.
     */
    @Test
    public void testMaxPathogenicityMutTaster() {
        float siftScore = 1.0f;
        float polyPhenScore = 0.2f;
        float mutTasterScore = 0.3f;
        VariantPathogenicity instance = new VariantPathogenicity(1, 2, 'A', 'B', 'C', 'D', 3, siftScore, polyPhenScore, mutTasterScore, 0.4f,0.4f,0.4f);
        float expResult = mutTasterScore;
        float result = instance.maxPathogenicity();
        assertEquals(expResult, result, 0.01);
    }
    
    /**
     * Test of maxPathogenicity method, of class VariantPathogenicity.
     */
    @Test
    public void testMaxPathogenicityPolyPhen() {
        float siftScore = 1.0f;
        float polyPhenScore = 0.4f;
        float mutTasterScore = 0.3f;
        VariantPathogenicity instance = new VariantPathogenicity(1, 2, 'A', 'B', 'C', 'D', 3, siftScore, polyPhenScore, mutTasterScore, 0.4f,0.4f,0.4f);
        float expResult = polyPhenScore;
        float result = instance.maxPathogenicity();
        assertEquals(expResult, result, 0.01);
    }

    /**
     * Test of toDumpLine method, of class VariantPathogenicity.
     */
    @Test
    public void testGetDumpLine() {
        VariantPathogenicity instance = new VariantPathogenicity(1, 2, 'A', 'B', 'C', 'D', 3, 0.1f, 0.2f, 0.3f, 0.4f,0.4f,0.4f);
        String expResult = String.format("1|2|A|B|C|D|3|0.1|0.2|0.3|0.4|0.4|0.4%n");
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }
  
}
