/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import org.apache.commons.cli.Option;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
public class TargetFilterOptionMarshallerTest {
    
    private TargetFilterOptionMarshaller instance;
    private ExomiserSettings.SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new TargetFilterOptionMarshaller();
        settingsBuilder = new ExomiserSettings.SettingsBuilder();
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("keep-off-target"));
    }
    
    @Test
    public void testThatOptionTakesNoArguments() {
        Option option = instance.getOption();
        assertThat(option.hasArg(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesFalseWhenSet() {
        Option option = instance.getOption();
        instance.applyValuesToSettingsBuilder(option.getValues(), settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.removeOffTargetVariants(), is(false));
    }
    
    @Test
    public void testSettingsRemoveOffTargetVariantsIsTrueByDefault() {
        ExomiserSettings settings = settingsBuilder.build();        
        assertThat(settings.removeOffTargetVariants(), is(true));
    }
    
}
