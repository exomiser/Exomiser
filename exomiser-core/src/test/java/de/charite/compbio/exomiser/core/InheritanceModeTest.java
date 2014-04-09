/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core;

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
public class InheritanceModeTest {
    
    public InheritanceModeTest() {
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
     * Test of valueOf method, of class InheritanceMode.
     */
    @Test
    public void testValueOf() {
        String name = "X_RECESSIVE";
        InheritanceMode expResult = InheritanceMode.X_RECESSIVE;
        InheritanceMode result = InheritanceMode.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getHpoTerm method, of class InheritanceMode.
     */
    @Test
    public void testGetHpoTerm() {
        InheritanceMode instance = InheritanceMode.RECESSIVE;
        String expResult = "HP:0000007";
        String result = instance.getHpoTerm();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInheritanceCode method, of class InheritanceMode.
     */
    @Test
    public void testGetInheritanceCode() {
        InheritanceMode instance = InheritanceMode.MITOCHONDRIAL;
        String expResult = "M";
        String result = instance.getInheritanceCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOfInheritanceCode method, of class InheritanceMode.
     */
    @Test
    public void testValueOfInheritanceCode() {
        String inheritanceCode = "P";
        InheritanceMode expResult = InheritanceMode.POLYGENIC;
        InheritanceMode result = InheritanceMode.valueOfInheritanceCode(inheritanceCode);
        assertEquals(expResult, result);

    }

    /**
     * Test of valueOfHpoTerm method, of class InheritanceMode.
     */
    @Test
    public void testValueOfHpoTerm() {
        String hpoTerm = "kjghdgh";
        InheritanceMode expResult = InheritanceMode.UNKNOWN;
        InheritanceMode result = InheritanceMode.valueOfHpoTerm(hpoTerm);
        assertEquals(expResult, result);
    }
    
}
