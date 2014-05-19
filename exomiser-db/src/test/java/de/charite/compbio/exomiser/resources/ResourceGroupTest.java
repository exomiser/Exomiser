/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.resources;

import de.charite.compbio.exomiser.parsers.ResourceGroupParser;
import de.charite.compbio.exomiser.parsers.ResourceParser;
import de.charite.compbio.exomiser.parsers.StringParser;
import java.nio.file.Path;
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
public class ResourceGroupTest {
    
    public ResourceGroupTest() {
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
     * Test of getName method, of class ResourceGroup.
     */
    @Test
    public void testGetName() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        String expResult = "wibble";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParserClass method, of class ResourceGroup.
     */
    @Test
    public void testGetParserClass() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Class expResult = TestResourceGroupParser.class;
        Class result = instance.getParserClass();
        assertEquals(expResult, result);

    }

    /**
     * Test of addResource method, of class ResourceGroup.
     */
    @Test
    public void testAddResource() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource resource = new Resource("Clive");
        resource.setParserClass(ResourceParser.class);
        assertTrue(instance.addResource(resource));
    }

    /**
     * Test of addResource method, of class ResourceGroup.
     */
    @Test
    public void testAddResource_NoParserClass() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource resource = new Resource("Dave");
        resource.setParserClass(null);
        assertTrue(instance.addResource(resource));
    }

    /**
     * Test of getResource method, of class ResourceGroup.
     */
    @Test
    public void testGetResource_Class() {
        Class clazz = ResourceParser.class;
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource expResult = new Resource("test");
        expResult.setParserClass(clazz);
        instance.addResource(expResult);
        Resource result = instance.getResource(clazz);
        assertEquals(expResult, result);

    }
    
    /**
     * Test of getResource method, of class ResourceGroup.
     */
    @Test
    public void testGetResource_IncorrectClassType() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource resource = new Resource("test");
        //shouldn't happen as this is defined in the properties file
        resource.setParserClass(ResourceParser.class);
        instance.addResource(resource);
        Resource result = instance.getResource(TestParser.class);
        assertNull(result);
    }
    
    /**
     * Test of getResource method, of class ResourceGroup.
     */
    @Test
    public void testGetResource_String() {
        String resourceName = "David";
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource expResult = new Resource(resourceName);
        instance.addResource(expResult);
        Resource result = instance.getResource(resourceName);
        assertEquals(expResult, result);

    }    
    
    private class TestParser implements ResourceParser {

        @Override
        public void parseResource(Resource resource, Path inDir, Path outDir) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    
    private class TestResourceGroupParser implements ResourceGroupParser {

        @Override
        public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
      
    }
}
