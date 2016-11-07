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
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutFilePrefixOptionMarshallerTest {
    
    private OutFilePrefixOptionMarshaller instance;
    private Option option;
    private SettingsBuilder settingsBuilder;
    
    private static final String OUTFILE_PREFIX = "/users/jules/vcf/analysis/exome";
    private static final String LONG_OPTION = "out-prefix";
    private static final String SHORT_OPTION = "o";
    
    @Before
    public void setUp() {
        instance = new OutFilePrefixOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = Settings.builder();
        settingsBuilder.vcfFilePath(Paths.get("user/analysis/vcf/test.vcf"));
    }


    @Test
    public void testGetCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo(LONG_OPTION));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder() {
        instance.applyValuesToSettingsBuilder(new String[]{OUTFILE_PREFIX}, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getOutputPrefix(), equalTo(OUTFILE_PREFIX));
    }
    
    @Test
    public void testOptionIsSingleValueOption() {
        assertThat(option.hasArg(), is(true));
        assertThat(option.hasArgs(), is(false));
    }
    
    @Test
    public void testOptionHasLongOption() {
        assertThat(option.hasLongOpt(), is(true));
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
