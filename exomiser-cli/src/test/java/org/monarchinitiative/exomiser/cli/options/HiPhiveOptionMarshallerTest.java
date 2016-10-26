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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptionMarshallerTest {
    
    private HiPhiveOptionMarshaller instance;
    
    @Before
    public void setUp() {
        instance = new HiPhiveOptionMarshaller();
    }
    
    @Test
    public void testCommandLineValue() {
        assertThat(instance.getCommandLineParameter(), equalTo("hiphive-params"));
    }
    
    @Test
    public void testThatOptionAcceptsMultipleValues() {
        Option option = instance.option;
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder() {
        String[] values = {"human","mouse","ppi"};
        
        Settings.SettingsBuilder settingsBuilder = new Settings.SettingsBuilder();
        instance.applyValuesToSettingsBuilder(values, settingsBuilder);
        Settings settings = settingsBuilder.build();
        
        assertThat(settings.getHiPhiveParams(), equalTo("human,mouse,ppi"));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilderWithNoInput() {
        String[] values = {};
        
        Settings.SettingsBuilder settingsBuilder = new Settings.SettingsBuilder();
        instance.applyValuesToSettingsBuilder(values, settingsBuilder);
        Settings settings = settingsBuilder.build();
        
        assertThat(settings.getHiPhiveParams(), equalTo(""));
    }
    
}
