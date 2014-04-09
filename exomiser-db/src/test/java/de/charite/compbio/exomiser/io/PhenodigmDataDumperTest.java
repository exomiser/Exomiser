/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.io;

import de.charite.compbio.exomiser.config.AppConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PhenodigmDataDumperTest {

    private static final Path outputPath = Paths.get("target/test-data");
    private static final File outputDir = outputPath.toFile();

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

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
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

        PhenodigmDataDumper.dumpPhenodigmData(outputPath);
        File outputFile = outputPath.toFile();
        assertTrue("Expected output path to be a directory", Files.isDirectory(outputPath));
        assertNotEquals("Expected output path to contain some files!", 0, outputFile.listFiles().length);

        List<File> expectedFiles = new ArrayList<>();
        expectedFiles.add(new File(outputPath.toFile(), "human2mouseOrthologs.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "diseaseHp.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "mouseMp.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "diseaseDisease.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "omimTerms.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "mouseGeneLevelSummary.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "hpMpMapping.pg"));
        expectedFiles.add(new File(outputPath.toFile(), "hpHpMapping.pg"));

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
        File expectedFile = PhenodigmDataDumper.dumpOmimTerms(outputPath, "testDumpOmim.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpHpHpMapping() {
        File expectedFile = PhenodigmDataDumper.dumpHpHpMapping(outputPath, "testDumpHpHp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpHpMpMapping() {
        File expectedFile = PhenodigmDataDumper.dumpHpMpMapping(outputPath, "testDumpHpMp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpDiseaseHp() {
        File expectedFile = PhenodigmDataDumper.dumpDiseaseHp(outputPath, "testDumpDiseaseHp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpMouseMp() {
        File expectedFile = PhenodigmDataDumper.dumpMouseMp(outputPath, "testDumpMouseMp.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpMouseGeneOrthologData() {
        File expectedFile = PhenodigmDataDumper.dumpMouseGeneOrthologs(outputPath, "testDumpMouseGeneOrtholog.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }

    @Test
    public void testDumpMouseGeneSummaryData() {
        File expectedFile = PhenodigmDataDumper.dumpMouseGeneLevelSummary(outputPath, "testDumpMouseGeneSummary.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
    
    @Test
    public void testDumpFishGeneLevelSummary() {
        File expectedFile = PhenodigmDataDumper.dumpFishGeneLevelSummary(outputPath, "testDumpFishGeneLevelSummary.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
    
    @Test
    public void testDumpFishGeneOrthologs() {
        File expectedFile = PhenodigmDataDumper.dumpFishGeneOrthologs(outputPath, "testDumpFishGeneOrtholog.pg");
        assertTrue(expectedFile.exists());
        assertNotEquals(0, expectedFile.length());
    }
}
