/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.reference;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantPathogenicityTest {
   
    private VariantPathogenicity instance;
    
    private static final float SIFT = 0.1f;
    private static final float POLYPHEN = 0.2f;
    private static final float MUT_TASTER = 0.3f;
            
    @Before
    public void setUp() {
        instance = new VariantPathogenicity(1, 2, "A", "B", SIFT, POLYPHEN, MUT_TASTER, 0.4f, 0.4f, 0.4f);
    }

    /**
     * Test of toDumpLine method, of class VariantPathogenicity.
     */
    @Test
    public void testGetDumpLine() {
        String expResult = String.format("1|2|A|B|0.1|0.2|0.3|0.4|0.4|0.4%n");
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }
  
}
