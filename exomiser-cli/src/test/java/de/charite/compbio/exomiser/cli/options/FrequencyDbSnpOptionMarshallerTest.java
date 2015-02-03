/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
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
public class FrequencyDbSnpOptionMarshallerTest {
    
    private FrequencyDbSnpOptionMarshaller instance;
    private Option option;
    private ExomiserSettings.SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new FrequencyDbSnpOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = new ExomiserSettings.SettingsBuilder();
        settingsBuilder.vcfFilePath(Paths.get("test.vcf"));
        settingsBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("remove-dbsnp"));
    }
    
    @Test
    public void testThatOptionHasOptionalArgument() {
        assertThat(option.hasOptionalArg(), is(true));
    }
    
    @Test
    public void testSettingsRemoveDbSnpIsFalseByDefault() {
        ExomiserSettings settings = settingsBuilder.build();        
        assertThat(settings.removeDbSnp(), is(false));
    }

    @Test
    public void testSettingsBuilderAppliesTrueWhenSetWithNullValue() {
        instance.applyValuesToSettingsBuilder(null, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.removeDbSnp(), is(true));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgFalse() {
        String[] args = {"false"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.removeDbSnp(), is(false));
    }
    
    @Test
    public void testSettingsBuilderAppliesArgTrue() {
        String[] args = {"true"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        
        assertThat(settings.removeDbSnp(), is(true));
    }

}
