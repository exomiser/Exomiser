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
import de.charite.compbio.exomiser.cli.options.OutFileOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.OutFormatOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.PedFileOptionMarshaller;
import de.charite.compbio.exomiser.cli.options.VcfFileOptionMarshaller;
import static de.charite.compbio.exomiser.core.model.ExomiserSettings.*;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    private void testOptionMarshallersForCommandLineParameter(OptionMarshaller expectedOptionMarshaller) {
        String commandLineParameter = expectedOptionMarshaller.getCommandLineParameter();

        OptionMarshaller optionMarshaller = optionMarshallers.get(commandLineParameter);
        
        assertThat(optionMarshallers.containsKey(commandLineParameter), is(true));
        assertThat(optionMarshaller, equalTo(expectedOptionMarshaller));
    }
    
    @Test
    public void optionsSpecifyOneVcfFile() {
        String longOption = VCF_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }   
    @Test
    public void optionMarshallersContainVcfFileOptionMarshaller() {
        OptionMarshaller expectedOptionMarshaller = new VcfFileOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    @Test
    public void optionsSpecifyPedFile() {
        String longOption = PED_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    @Test
    public void optionMarshallersContainPedFileOptionMarshaller() {       
        OptionMarshaller expectedOptionMarshaller = new PedFileOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
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
    public void optionMarshallersContainSettingsFileOptionMarshaller() {        
        OptionMarshaller expectedOptionMarshaller = new SettingsFileOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    @Test
    public void outputFormatOptionsCanHaveMultipleCommaSeparatedValues() {
        String longOption = OUT_FORMAT_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
  @Test
    public void optionMarshallersContainOutFormatOptionMarshaller() {        
        OptionMarshaller expectedOptionMarshaller = new OutFormatOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
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
    @Test
    public void optionMarshallersContainsHpoIdsOptionMarshaller() {       
        OptionMarshaller expectedOptionMarshaller = new HpoIdsOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    @Test
    public void inheritanceModeOptionIsPresent() {
        String longOption = MODE_OF_INHERITANCE_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArg(), is(true));
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    @Test
    public void optionMarshallersContainsInheritanceModeOptionMarshaller() {       
        OptionMarshaller expectedOptionMarshaller = new InheritanceModeOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    @Test
    public void hasBatchModeOption() {
        String longOption = "batch-file";
        Option option = options.getOption(longOption);
        assertThat(option.hasArg(), is(true));
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    @Test
    public void optionMarshallersContainsBatchFileOptionMarshaller() {       
        OptionMarshaller expectedOptionMarshaller = new BatchFileOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    
    @Test
    public void hasFullAnalysisOption() {
        String longOption = RUN_FULL_ANALYSIS_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArg(), is(true));
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    @Test
    public void optionMarshallersContainFullAnalysisOptionMarshaller() {        
        OptionMarshaller expectedOptionMarshaller = new FullAnalysisOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
        
    @Test
    public void hasNumGenesOption() {
        String longOption = NUM_GENES_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArg(), is(true));
        assertThat(option.getLongOpt(), equalTo(longOption));
    }
    @Test
    public void optionMarshallersContainsNumGenesOptionMarshaller() {
        OptionMarshaller expectedOptionMarshaller = new NumGenesOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
    
    @Test
    public void hasOutFileOption() {
        String longOption = OUT_FILE_OPTION;
        Option option = options.getOption(longOption);
        assertThat(option.hasArg(), is(true));
        assertThat(option.getLongOpt(), equalTo(longOption));
    } 
    @Test
    public void optionMarshallersContainsOutFileOptionMarshaller() {
        OptionMarshaller expectedOptionMarshaller = new OutFileOptionMarshaller();
        testOptionMarshallersForCommandLineParameter(expectedOptionMarshaller);
    }
}
