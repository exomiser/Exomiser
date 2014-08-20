/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli.config;

import static de.charite.compbio.exomiser.core.model.ExomiserSettings.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for the command line options.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CommandLineOptionsConfig.class)
public class CommandLineOptionsConfigTest {
    
    @Autowired
    private Options options;

    @Test
    public void optionsSpecifyOneVcfFile() {
        String longOption = VCF_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    
    @Test
    public void optionsSpecifyOnePrioritiser() {
        String longOption = PRIORITISER_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    
    @Test
    public void prioritiserOptionsHaveADecentDescription() {
        String description = options.getOption(PRIORITISER_OPTION).getDescription();
        System.out.println(description);
        assertThat(description.isEmpty(), is(false));
    }
    
    @Test
    public void optionsContainsSettingsFile() {
        assertThat(options.hasOption(SETTINGS_FILE_OPTION), is(true));
    }
    
    @Test
    public void outputFormatOptionsCanHaveMultipleCommaSeparatedValues() {
        String longOption = OUT_FORMAT_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void seedGenesOptionsCanHaveMultipleCommaSeparatedValues() {
        String longOption = SEED_GENES_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void hpoIdsOptionsCanHaveMultipleCommaSeparatedValues() {
        String longOption = HPO_IDS_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
}
