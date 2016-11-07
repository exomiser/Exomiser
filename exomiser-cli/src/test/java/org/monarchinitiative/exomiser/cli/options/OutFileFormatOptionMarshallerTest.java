/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Settings;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jj8
 */
public class OutFileFormatOptionMarshallerTest {

    private OutFileFormatOptionMarshaller instance;
    private Option option;
    private Settings.SettingsBuilder settingsBuilder;

    private static final Set<OutputFormat> OUTFILE_FORMATS = EnumSet.allOf(OutputFormat.class);
    private static final String LONG_OPTION = "out-format";
    private static final String SHORT_OPTION = "f";

    @Before
    public void setUp() {
        instance = new OutFileFormatOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = new Settings.SettingsBuilder();
        settingsBuilder.vcfFilePath(Paths.get("user/analysis/vcf/test.vcf"));
    }

    @Test
    public void testGetCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo(LONG_OPTION));
    }

    @Test
    public void testApplyValuesToSettingsBuilder() {
        String[] inputValues = {"HTML", "VCF", "TSV-GENE", "TSV-VARIANT", "PHENOGRID"};
        instance.applyValuesToSettingsBuilder(inputValues, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTFILE_FORMATS));
    }

    @Test
    public void testApplyValuesToSettingsBuilderWithAlternateNames() {
        String[] inputValues = {"HTML", "VCF", "TAB-GENE", "TAB-VARIANT", "PHENOGRID"};
        instance.applyValuesToSettingsBuilder(inputValues, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTFILE_FORMATS));
    }

    @Test
    public void testApplyValuesToSettingsBuilderWithEnumSrings() {
        String[] inputValues = {"HTML", "VCF", "TSV_GENE", "TSV_VARIANT", "PHENOGRID"};
        instance.applyValuesToSettingsBuilder(inputValues, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTFILE_FORMATS));
    }

    @Test
    public void testOptionIsSingleValueOption() {
        assertThat(option.hasArg(), is(true));
        assertThat(option.hasArgs(), is(true));
    }

    @Test
    public void testOptionHasLongOption() {
        assertThat(option.hasLongOpt(), is(true));
    }

    @Test
    public void testOptionTakesCommaSeperatedValues() {
        assertThat(option.getValueSeparator(), equalTo(','));
    }

    @Test
    public void testLongOption() {
        assertThat(option.getLongOpt(), equalTo(LONG_OPTION));
    }

    @Test
    public void testShortOption() {
        assertThat(option.getOpt(), equalTo(SHORT_OPTION));
    }

}
