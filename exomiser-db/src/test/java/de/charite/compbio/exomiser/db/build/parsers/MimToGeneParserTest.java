/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.parsers;

import de.charite.compbio.exomiser.db.build.parsers.MimToGeneParser;
import de.charite.compbio.exomiser.db.build.resources.Resource;
import de.charite.compbio.exomiser.db.build.resources.ResourceOperationStatus;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test of the <code>MimToGeneParser</code>.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MimToGeneParserTest {
    
    public MimToGeneParserTest() {
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
     * Test of parseResource method, of class MimToGeneParser.
     */
    @Test
    public void testParse() {
        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        Resource testResource = new Resource("MIM2GENE");
        testResource.setExtractedFileName("mim2gene.txt");
        testResource.setParsedFileName("testMim2Gene.out");
        
        MimToGeneParser instance = new MimToGeneParser(mim2geneMap);
        ResourceOperationStatus expResult = ResourceOperationStatus.SUCCESS;
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), Paths.get("target/test-data"));
        assertFalse(mim2geneMap.isEmpty());
        for (Entry<Integer, Set<Integer>> entry : mim2geneMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println(entry);            
            }
        }
        assertEquals(expResult, testResource.getParseStatus());

    }
    
}
