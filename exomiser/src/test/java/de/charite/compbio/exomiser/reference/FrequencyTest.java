/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.reference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
public class FrequencyTest {
    
    public FrequencyTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of set_dbSNP_GMAF method, of class Frequency.
     */
    @Test
    public void testSet_dbSNP_GMAF() {
        System.out.println("set_dbSNP_GMAF");
        float maf = 0.0F;
        Frequency instance = null;
        instance.set_dbSNP_GMAF(maf);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setESPFrequencyEA method, of class Frequency.
     */
    @Test
    public void testSetESPFrequencyEA() {
        System.out.println("setESPFrequencyEA");
        float f = 0.0F;
        Frequency instance = null;
        instance.setESPFrequencyEA(f);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setESPFrequencyAA method, of class Frequency.
     */
    @Test
    public void testSetESPFrequencyAA() {
        System.out.println("setESPFrequencyAA");
        float f = 0.0F;
        Frequency instance = null;
        instance.setESPFrequencyAA(f);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setESPFrequencyAll method, of class Frequency.
     */
    @Test
    public void testSetESPFrequencyAll() {
        System.out.println("setESPFrequencyAll");
        float f = 0.0F;
        Frequency instance = null;
        instance.setESPFrequencyAll(f);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaximumFrequency method, of class Frequency.
     */
    @Test
    public void testGetMaximumFrequency() {
        System.out.println("getMaximumFrequency");
        Frequency instance = null;
        float expResult = 0.0F;
        float result = instance.getMaximumFrequency();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDumpLine method, of class Frequency.
     */
    @Test
    public void testGetDumpLine() {
        System.out.println("getDumpLine");
        Frequency instance = new Frequency((byte) 1, 4, "foop", "wibble", 2);
        String expResult = "";
        String result = instance.getDumpLine();
        assertEquals(expResult, result);

    }

    /**
     * Test of compareTo method, of class Frequency.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        Frequency f = null;
        Frequency instance = null;
        int expResult = 0;
        int result = instance.compareTo(f);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIdenticalSNP method, of class Frequency.
     */
    @Test
    public void testIsIdenticalSNP() {
        System.out.println("isIdenticalSNP");
        Frequency other = null;
        Frequency instance = null;
        boolean expResult = false;
        boolean result = instance.isIdenticalSNP(other);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetFrequencyValues method, of class Frequency.
     */
    @Test
    public void testResetFrequencyValues() {
        System.out.println("resetFrequencyValues");
        Frequency other = null;
        Frequency instance = null;
        instance.resetFrequencyValues(other);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
