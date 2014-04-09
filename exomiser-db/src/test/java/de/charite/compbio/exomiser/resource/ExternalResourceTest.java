/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.resource;

import de.charite.compbio.exomiser.resources.ExternalResource;
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
public class ExternalResourceTest {
    
    public ExternalResourceTest() {
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
     * Test of getStatus method, of class ExternalResource.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        ExternalResource instance = new ExternalResource();
        instance.setName("test");
        String expResult = "Status for: test     Download: UNTRIED, Extract: UNTRIED, Parse: UNTRIED";
        String result = instance.getStatus();
        assertEquals(expResult, result);

    }    
}
