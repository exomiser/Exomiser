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
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterOptionMarshallerTest {
    
    private TargetFilterOptionMarshaller instance;
    private Option option;
    private Settings.SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new TargetFilterOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = Settings.builder();
        settingsBuilder.vcfFilePath(Paths.get("test.vcf"));
        settingsBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("keep-off-target"));
    }
    
    @Test
    public void testThatOptionHasOptionalArgument() {
        assertThat(option.hasOptionalArg(), is(true));
    }

    @Test
    public void testSettingsKeepOffTargetVariantsIsFalseByDefault() {
        Settings settings = settingsBuilder.build();        
        assertThat(settings.keepOffTargetVariants(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesFalseWhenSetWithNullValue() {
        instance.applyValuesToSettingsBuilder(null, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.keepOffTargetVariants(), is(true));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgFalse() {
        String[] args = {"false"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.keepOffTargetVariants(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgTrue() {
        String[] args = {"true"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.keepOffTargetVariants(), is(true));
    }
    
}
