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
public class DiseaseIdOptionMarshallerTest {
    
    private DiseaseIdOptionMarshaller instance;
    private Option option;
    private SettingsBuilder settingsBuilder;
            
    @Before
    public void setUp() {
        instance = new DiseaseIdOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = Settings.builder();
        settingsBuilder.vcfFilePath(Paths.get("test.vcf"));
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("disease-id"));
    }
    
    @Test
    public void testThatOptionHasNoOptionalArgument() {
        assertThat(option.hasOptionalArg(), is(false));
    }
    
    @Test
    public void testSettingsgetDiseaseIdIsEmptyByDefault() {
        Settings settings = settingsBuilder.build();        
        assertThat(settings.getDiseaseId().isEmpty(), is(true));
    }
    
    @Test
    public void testSettingsBuilderAppliesEmptySetValue() {
        String[] args = {""};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getDiseaseId().isEmpty(), is(true));
    }
    
    @Test
    public void testSettingsBuilderAppliesValue() {
        String[] args = {"OMIM:101600"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.getDiseaseId(), equalTo("OMIM:101600"));
    }     
    
}
