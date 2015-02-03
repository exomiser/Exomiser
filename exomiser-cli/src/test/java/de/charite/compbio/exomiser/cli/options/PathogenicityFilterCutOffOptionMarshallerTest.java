/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
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
public class PathogenicityFilterCutOffOptionMarshallerTest {
    
    private PathogenicityFilterCutOffOptionMarshaller instance;
    private SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new PathogenicityFilterCutOffOptionMarshaller();
        settingsBuilder = new ExomiserSettings.SettingsBuilder();
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("keep-non-pathogenic"));
    }
    
    @Test
    public void testThatOptionTakesNoArguments() {
        Option option = instance.getOption();
        assertThat(option.hasArg(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesTrueWhenSet() {
        Option option = instance.getOption();
        instance.applyValuesToSettingsBuilder(option.getValues(), settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.removePathFilterCutOff(), is(true));
    }
    
    @Test
    public void testSettingsRemovePathFilterCutOffIsFalseByDefault() {
        ExomiserSettings settings = settingsBuilder.build();        
        assertThat(settings.removePathFilterCutOff(), is(false));
    }
    
}
