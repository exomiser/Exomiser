/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.data.phenotype.resources;

import org.junit.Test;
import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceGroupParser;
import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceParser;

import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author jj8
 */
public class ResourceGroupTest {

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
    public void testAddResourceNoParserClass() {
        ResourceGroup instance = new ResourceGroup("wibble", TestResourceGroupParser.class);
        Resource resource = new Resource("Dave");
        resource.setParserClass(null);
        assertTrue(instance.addResource(resource));
    }

    /**
     * Test of getResource method, of class ResourceGroup.
     */
    @Test
    public void testGetResourceClass() {
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
    public void testGetResourceIncorrectClassType() {
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
    public void testGetResourceString() {
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
