/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.options.BatchFileOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.SettingsFileOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.FullAnalysisOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.HpoIdsOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.InheritanceModeOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.NumGenesOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.OptionMarshaller;
import de.charite.compbio.exomiser.cli.options.OutFilePrefixOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.OutFileFormatOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.PedFileOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.PrioritiserOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.SeedGenesOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.VcfFileOptionMarshaller;
import java.util.Map;
import javax.annotation.Resource;
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
    
    @Resource
    private Map<String, OptionMarshaller> optionMarshallers;

    private void isCorrectlyConfiguredAndHasOption(OptionMarshaller expected) {
        String commandLineParameter = expected.getCommandLineParameter();

        OptionMarshaller optionMarshaller = optionMarshallers.get(commandLineParameter);
        assertThat(optionMarshallers.containsKey(commandLineParameter), is(true));
        assertThat(expected, equalTo(optionMarshaller));
        
        Option option = options.getOption(commandLineParameter);
        assertThat(option.getLongOpt(), equalTo(commandLineParameter));
    }
    
    @Test
    public void containsVcfFileOptionMarshaller() {
        OptionMarshaller optionMarshaller = new VcfFileOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }

    @Test
    public void containsPedFileOptionMarshaller() {       
        OptionMarshaller optionMarshaller = new PedFileOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }
    
    @Test
    public void containsPrioritiserOptionMarshaller() {
        OptionMarshaller optionMarshaller = new PrioritiserOptionMarshaller();
        Option option = optionMarshaller.getOption();
        String description = option.getDescription();
        System.out.println(description);
        assertThat(description.isEmpty(), is(false));
    }
    
    @Test
    public void containsSettingsFileOptionMarshaller() {        
        OptionMarshaller optionMarshaller = new SettingsFileOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }
    
    @Test
    public void seedGenesOptionsCanHaveMultipleCommaSeparatedValues() {
        OptionMarshaller optionMarshaller = new SeedGenesOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void hpoIdsOptionsCanHaveMultipleCommaSeparatedValues() {
        OptionMarshaller optionMarshaller = new HpoIdsOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void containsInheritanceModeOptionAndTakesArgs() {
        OptionMarshaller optionMarshaller = new InheritanceModeOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArg(), is(true));
    }
    
    @Test
    public void containsBatchFileOptionMarshaller() {       
        OptionMarshaller optionMarshaller = new BatchFileOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }
    
    
    @Test
    public void containsFullAnalysisOptionMarshallerAndTakesArgs() {        
        OptionMarshaller optionMarshaller = new FullAnalysisOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
        
        Option option = optionMarshaller.getOption();
        assertThat(option.hasArg(), is(true));
    }
        
    @Test
    public void containsNumGenesOptionMarshaller() {        
        OptionMarshaller optionMarshaller = new NumGenesOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }
    
    @Test
    public void testOutFilePrefixOptionMarshaller() {
        OptionMarshaller optionMarshaller = new OutFilePrefixOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
        
        Option option = optionMarshaller.getOption();
        assertThat(option.hasArg(), is(true));
    }
    
    @Test
    public void containsOutFormatOptionMarshaller() {        
        OptionMarshaller optionMarshaller = new OutFileFormatOptionMarshaller();
        isCorrectlyConfiguredAndHasOption(optionMarshaller);
    }
    
}
