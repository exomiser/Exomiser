/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Test of parseResource method, of class MorbidMapParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        Path testResourceDir = Paths.get("src/test/resources/data");
        Path testOutDir = Paths.get("target/test-data");
        
        Resource testResource = new Resource();
        testResource.setName("OMIM_morbidmap");
        testResource.setExtractedFileName("morbidmap");
        testResource.setParsedFileName("testMorbidMap.txt");
        
        Resource diseaseInheritanceResource = new Resource();
        diseaseInheritanceResource.setName("HPO_phenotype_annotation_test");
        diseaseInheritanceResource.setExtractedFileName("phenotype_annotation_test.tab");
        
        DiseaseInheritanceCache cache = new DiseaseInheritanceCache();
        cache.parseResource(diseaseInheritanceResource, testResourceDir, testOutDir);
        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        //todo: add some stub data to stop the test failing...
        
        MorbidMapParser instance = new MorbidMapParser(cache, mim2geneMap);
        ResourceOperationStatus expResult = ResourceOperationStatus.SUCCESS;
        instance.parseResource(testResource, testResourceDir, testOutDir);
        assertEquals(expResult, testResource.getParseStatus());  
        
    }
    
}
