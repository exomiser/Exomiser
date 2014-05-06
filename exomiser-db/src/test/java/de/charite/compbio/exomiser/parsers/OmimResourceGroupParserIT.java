/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceGroup;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration test for OmimResourceGroupParser.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OmimResourceGroupParserIT {
    
    public OmimResourceGroupParserIT() {
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
     * Test of parseResources method, of class OmimResourceGroupParser.
     */
    @Test
    public void testParseResources() {
        System.out.println("parseResources");
        ResourceGroup resourceGroup = null;
        Path inDir = null;
        Path outDir = null;
        OmimResourceGroupParser instance = new OmimResourceGroupParser();
        instance.parseResources(resourceGroup, inDir, outDir);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
