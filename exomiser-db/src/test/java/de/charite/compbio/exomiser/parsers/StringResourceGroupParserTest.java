/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceGroup;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class StringResourceGroupParserTest {
    
    public StringResourceGroupParserTest() {
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
     * Test of parseResources method, of class StringResourceGroupParser.
     */
    @Test
    public void testParseResources() {
        ResourceGroup resourceGroup = new ResourceGroup("wibble", StringResourceGroupParser.class);
        
        Resource stringResource = new Resource("StringDb");
        stringResource.setParserClass(StringParser.class);
        resourceGroup.addResource(stringResource);
        
        Resource entrezResource = new Resource("Entrez");
        entrezResource.setParserClass(EntrezParser.class);
        resourceGroup.addResource(entrezResource);
        
        Path inDir = Paths.get(".");
        Path outDir = Paths.get(".");
        
        StringResourceGroupParser instance = new StringResourceGroupParser();
        instance.parseResources(resourceGroup, inDir, outDir);

    }
}
