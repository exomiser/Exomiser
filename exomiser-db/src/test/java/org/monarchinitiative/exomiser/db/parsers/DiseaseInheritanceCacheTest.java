/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.junit.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.db.resources.Resource;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests for Class DiseaseInheritanceCache.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseInheritanceCacheTest {
    
    private static DiseaseInheritanceCache instance;
    
    @BeforeClass
    public static void setUpClass() {
        Resource testResource = new Resource("test_hpo_annotation_resource");
        testResource.setExtractedFileName("phenotype_annotation_test.tab");
        instance = new DiseaseInheritanceCache();
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), Paths.get("target/test-data"));
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
        InheritanceMode expResult = InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeRecessive() {
        Integer phenID = 100100;
        InheritanceMode expResult = InheritanceMode.AUTOSOMAL_RECESSIVE;
        InheritanceMode result = instance.getInheritanceMode(phenID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeDominant() {
        Integer phenID = 100200;
        InheritanceMode expResult = InheritanceMode.AUTOSOMAL_DOMINANT;
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
        DiseaseInheritanceCache emptyCache = new DiseaseInheritanceCache();
        assertTrue(emptyCache.isEmpty());
    }
}
