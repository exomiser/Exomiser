/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the HPO ontology parser
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HPOOntologyFileParserTest {
    
    public HPOOntologyFileParserTest() {
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
     * Test of parseHPO method, of class HPOOntologyFileParser.
     */
    @Test
    public void testParseHPO() {
        System.out.println("parseHPO");
        String inpath = "";
        String outpath = "";
        HPOOntologyFileParser instance = new HPOOntologyFileParser();
        instance.parse(inpath, outpath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
