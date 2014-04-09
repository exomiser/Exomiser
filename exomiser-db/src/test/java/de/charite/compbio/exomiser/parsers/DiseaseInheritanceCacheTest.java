/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.core.InheritanceMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Class DiseaseInheritanceCache.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseInheritanceCacheTest {
    
    private static DiseaseInheritanceCache instance;
    
    public DiseaseInheritanceCacheTest() {    
    }
    
    @BeforeClass
    public static void setUpClass() {
        instance = new DiseaseInheritanceCache("src/test/resources/data/phenotype_annotation_test.tab");
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
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeUnknownOrphanet() {
        Integer phenID = 36237;
        InheritanceMode expResult = InheritanceMode.UNKNOWN;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeBoth() {
        Integer phenID = 100300;
        InheritanceMode expResult = InheritanceMode.DOMINANT_AND_RECESSIVE;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeRecessive() {
        Integer phenID = 100100;
        InheritanceMode expResult = InheritanceMode.RECESSIVE;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeDominant() {
        Integer phenID = 100200;
        InheritanceMode expResult = InheritanceMode.DOMINANT;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }  
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeUnknownDisease() {
        Integer phenID = 000000;
        InheritanceMode expResult = InheritanceMode.UNKNOWN;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }  
    
        /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeMitochondrial() {
        Integer phenID = 560000;
        InheritanceMode expResult = InheritanceMode.MITOCHONDRIAL;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsEmptyFalse() {
        assertFalse(instance.isEmpty());
    }
    
    @Test
    public void testIsEmptyTrue() {
        DiseaseInheritanceCache emptyCache = new DiseaseInheritanceCache("");
        assertTrue(emptyCache.isEmpty());
    }
}
