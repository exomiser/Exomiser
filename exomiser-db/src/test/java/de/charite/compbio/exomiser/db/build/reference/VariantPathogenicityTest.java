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
    private static final float CADD_RAW_RANK = 0.4f;
    private static final float CADD_RAW_SCORE = 0.5f;
            
    @Before
    public void setUp() {
        instance = new VariantPathogenicity(1, 2, "A", "B", SIFT, POLYPHEN, MUT_TASTER, CADD_RAW_RANK, CADD_RAW_SCORE);
    }

    @Test
    public void testGetDumpLine() {
        String expResult = String.format("1|2|A|B|0.1|0.2|0.3|0.4|0.5%n");
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }
  
    @Test
    public void testGetDumpLineWithNullSift() {
        String expResult = String.format("1|2|A|B|null|0.2|0.3|0.4|0.5%n");
        instance = new VariantPathogenicity(1, 2, "A", "B", null, POLYPHEN, MUT_TASTER, CADD_RAW_RANK, CADD_RAW_SCORE);
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }
}
