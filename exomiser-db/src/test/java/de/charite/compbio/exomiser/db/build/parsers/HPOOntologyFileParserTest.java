/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.parsers;

import de.charite.compbio.exomiser.db.build.parsers.HPOOntologyFileParser;
import de.charite.compbio.exomiser.db.build.resources.Resource;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        Resource testResource = new Resource("HPO");
        testResource.setExtractedFileName("hp.obo");
        testResource.setParsedFileName("hpoTestOut.pg");
        HPOOntologyFileParser instance = new HPOOntologyFileParser();
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), Paths.get("target/test-data"));
    }
    
}
