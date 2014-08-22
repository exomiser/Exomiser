/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.io;

import de.charite.compbio.exomiser.config.AppConfig;
import de.charite.compbio.exomiser.config.DataSourceConfig;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jj8
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, DataSourceConfig.class})
public class PhenodigmDataDumperTest {

    private static final Path outputPath = Paths.get("target/test-data");
    private static final File outputDir = outputPath.toFile();

    private PhenodigmDataDumper instance;
    
    public PhenodigmDataDumperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        outputDir.mkdir();
        //clean up all files
        for (File file : outputDir.listFiles()) {
            file.delete();
        }
        assertTrue(outputDir.isDirectory());
        assertEquals("Expected output path to be empty before tests start", 0, outputDir.listFiles().length);

    }

    @Before
    public void setUp() {
        //TODO: add the Config classes to a testContext
        instance = new PhenodigmDataDumper();
    }

    @After
    public void tearDown() {
        //clean up all files
        for (File file : outputDir.listFiles()) {
            //file.delete();
        }
    }

    /**
     * Test of dumpPhenodigmData method, of class PhenodigmDataDumper.
     */
    @Test
    public void testDumpPhenodigmData() {

        instance.dumpPhenodigmData(outputPath);
        File outputFile = outputPath.toFile();
        assertTrue("Expected output path to be a directory", Files.isDirectory(outputPath));
        assertNotEquals("Expected output path to contain some files!", 0, outputFile.listFiles().length);

        List<File> expectedFiles = new ArrayList<>();
        expectedFiles.add(new File(outputPath.toFile(), "human2mouseOrthologs.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "diseaseHp.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "mouseMp.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "diseaseDisease.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "omimTerms.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "hpMpMapping.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "hpHpMapping.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "orphanet.pg"));

        int expectedNoFiles = expectedFiles.size();
        assertEquals("Wrong number of files in output directory", expectedNoFiles, outputDir.listFiles().length);

        for (File file : expectedFiles) {
            assertTrue(file.exists());
        }

        for (File file : outputFile.listFiles()) {
            assertNotEquals(0, file.length());
        }
    }

//    @Test
//    public void testDumpDiseaseDiseaseSummary() {
//        File expectedFile = PhenodigmDataDumper.dumpDiseaseDiseaseSummary(outputPath, "testDumpDiseaseDisease.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }

    @Test
    public void testDumpOmimTerms() {
        File expectedFile = instance.dumpOmimTerms(outputPath, "testDumpOmim.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpHpHpMapping() {
        File expectedFile = instance.dumpHpHpMapping(outputPath, "testDumpHpHp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpHpMpMapping() {
        File expectedFile = instance.dumpHpMpMapping(outputPath, "testDumpHpMp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpDiseaseHp() {
        File expectedFile = instance.dumpDiseaseHp(outputPath, "testDumpDiseaseHp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpMouseMp() {
        File expectedFile = instance.dumpMouseMp(outputPath, "testDumpMouseMp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpMouseGeneOrthologData() {
        File expectedFile = instance.dumpMouseGeneOrthologs(outputPath, "testDumpMouseGeneOrtholog.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
    
    @Test
    public void testDumpFishGeneOrthologs() {
        File expectedFile = instance.dumpFishGeneOrthologs(outputPath, "testDumpFishGeneOrtholog.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
    
    @Test
    public void testDumpOrphanet() {
        File expectedFile = instance.dumpOrphanet(outputPath, "testDumpOrphanet.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
}
