/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.analysis.Settings;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
        settingsBuilder = new Settings.SettingsBuilder();
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
