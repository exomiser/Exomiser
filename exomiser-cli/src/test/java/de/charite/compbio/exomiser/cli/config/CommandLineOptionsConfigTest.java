/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.CommandLineOption;
import de.charite.compbio.exomiser.cli.config.CommandLineOptionsConfig;
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
    public void options_specify_a_vcf_file() {
        String longOption = CommandLineOption.VCF_OPTION.getLongOption();
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    
    @Test
    public void options_specify_a_prioritiser() {
        String longOption = CommandLineOption.PRIORITISER_OPTION.getLongOption();
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    
    @Test
    public void prioritiserOptionsHaveADecentDescription() {
        String description = options.getOption(CommandLineOption.PRIORITISER_OPTION.getLongOption()).getDescription();
        System.out.println(description);
        assertThat(description.isEmpty(), is(false));
    }
    
    @Test
    public void optionsContainsSettingsFile() {
        assertThat(options.hasOption(CommandLineOption.SETTINGS_FILE_OPTION.getLongOption()), is(true));
    }
}
