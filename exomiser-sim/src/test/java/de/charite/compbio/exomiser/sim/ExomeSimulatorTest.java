/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.sim;

import de.charite.compbio.exomiser.config.AppConfig;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jj8
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
public class ExomeSimulatorTest {
    
    @Autowired
    ExomeSimulator instance;
    
    public ExomeSimulatorTest() {
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
     * Test of outputExome method, of class ExomeSimulator.
     */
    @Test
    public void testOutputExome() {
        System.out.println("outputExome");
        String populationGroup = "ALL";
        String outputPath = "target/exomeSim.vcf";
        File output = instance.outputExome(populationGroup, outputPath);
        assertTrue(output.exists());
        output.delete();
    }

    /**
     * Test of outputChromosome method, of class ExomeSimulator.
     */
    @Test
    public void testOutputChromosome() {
        System.out.println("outputChromosome");
        String populationGroup = "ALL";
        int chrom = 22;
        String outputPath = String.format("target/exomeSimChromosome%s.vcf", chrom);
        File output = instance.outputChromosome(populationGroup, chrom, outputPath);
        assertTrue(output.exists());
        output.delete();
    }
    
}
