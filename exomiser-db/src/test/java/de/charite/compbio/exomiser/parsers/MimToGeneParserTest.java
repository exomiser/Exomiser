/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.config.AppConfig;
import de.charite.compbio.exomiser.io.FileOperationStatus;
import java.util.HashMap;
import java.util.List;
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
     * Test of parse method, of class MimToGeneParser.
     */
    @Test
    public void testParse() {
        String mim2geneTestFile = "src/test/resources/data/mim2gene.txt";
        String outPath = "target/test-data/testMim2Gene.out";
        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();

        MimToGeneParser instance = new MimToGeneParser(mim2geneMap);
        FileOperationStatus expResult = FileOperationStatus.SUCCESS;
        FileOperationStatus result = instance.parse(mim2geneTestFile, outPath);
        assertFalse(mim2geneMap.isEmpty());
        for (Entry<Integer, Set<Integer>> entry : mim2geneMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println(entry);            
            }
        }
        assertEquals(expResult, result);

    }
    
}
