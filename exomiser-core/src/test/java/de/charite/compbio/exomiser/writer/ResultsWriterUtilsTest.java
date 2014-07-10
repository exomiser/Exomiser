/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.util.OutputFormat;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterUtilsTest {

    SettingsBuilder settingsbuilder;
    
    public ResultsWriterUtilsTest() {
    }

    @Before
    public void before() {
        settingsbuilder = new ExomiserSettings.SettingsBuilder();
        settingsbuilder.vcfFilePath(Paths.get("wibble"));
    }
    
    @Test
    public void testThatDefaultFileExtensionIsHtml() {
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = "results/wibble-exomiser-results.html";
        String result = ResultsWriterUtils.determineFileExtension(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedTsvFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.TSV;
        settingsbuilder.outputFormat(testedFormat);
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = String.format("results/wibble-exomiser-results.%s", testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedVcfFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.VCF;
        settingsbuilder.outputFormat(testedFormat);
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = String.format("results/wibble-exomiser-results.%s", testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedOutputFormatOverwritesGivenOutFileExtension() {
        OutputFormat testedFormat = OutputFormat.VCF;
        settingsbuilder.outputFormat(testedFormat);
        settingsbuilder.outFileName("/user/jules/exomes/analysis/slartibartfast.xml");
        ExomiserSettings settings = settingsbuilder.build();        
        String expResult = String.format("/user/jules/exomes/analysis/slartibartfast.%s", testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testDefaultOutputFormatIsNotDestroyedByIncorrectFileExtensionDetection() {
        settingsbuilder.buildVersion("2.1.0");
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = "results/wibble-exomiser-2.1.0-results.html";
        String result = ResultsWriterUtils.determineFileExtension(settings);
        assertThat(result, equalTo(expResult));
    }
}
