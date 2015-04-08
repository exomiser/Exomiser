/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
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
public class OutputPassOnlyVariantsOptionMarshallerTest {
    
    private OutputPassOnlyVariantsOptionMarshaller instance;
    private Option option;
    private ExomiserSettings.SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new OutputPassOnlyVariantsOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = new ExomiserSettings.SettingsBuilder();
        settingsBuilder.vcfFilePath(Paths.get("test.vcf"));
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("output-pass-variants-only"));
    }
    
    @Test
    public void testThatOptionHasOptionalArgument() {
        assertThat(option.hasOptionalArg(), is(true));
    }
    
    @Test
    public void testSettingsOutputPassVariantsOnlyIsFalseByDefault() {
        ExomiserSettings settings = settingsBuilder.build();        
        assertThat(settings.outputPassVariantsOnly(), is(false));
    }

    @Test
    public void testSettingsBuilderAppliesTrueWhenSetWithNullValue() {
        instance.applyValuesToSettingsBuilder(null, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.outputPassVariantsOnly(), is(true));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgFalse() {
        String[] args = {"false"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.outputPassVariantsOnly(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgTrue() {
        String[] args = {"true"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.outputPassVariantsOnly(), is(true));
    }
    
}
