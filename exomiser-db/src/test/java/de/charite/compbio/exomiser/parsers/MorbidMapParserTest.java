/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class MorbidMapParserTest {
    
    public MorbidMapParserTest() {
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
     * Test of parse method, of class MorbidMapParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String inPath = "src/test/resources/data/morbidmap";
        String outPath = "target/test-data/testMorbidMap.out";
        DiseaseInheritanceCache cache = new DiseaseInheritanceCache("src/test/resources/data/phenotype_annotation_test.tab");
        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        
        MorbidMapParser instance = new MorbidMapParser(cache, mim2geneMap);
        ResourceOperationStatus expResult = ResourceOperationStatus.SUCCESS;
        ResourceOperationStatus result = instance.parse(inPath, outPath);
        assertEquals(expResult, result); 
        
    }
    
}
