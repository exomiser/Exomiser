/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.resources;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceTest {
    
    public ResourceTest() {
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
     * Test of getStatus method, of class Resource.
     */
    @Test
    public void testGetStatus() {
        Resource instance = new Resource("test");
        String expResult = "Status for: test                    Download: UNTRIED, Extract: UNTRIED, Parse: UNTRIED";
        String result = instance.getStatus();
        assertEquals(expResult, result);

    }    
}
