/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.cli.CommandLineParseError;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PrioritiserOptionMarshallerTest {
    
    private PrioritiserOptionMarshaller instance;
    private SettingsBuilder settingsBuilder;
    
    @Before
    public void setUp() {
        instance = new PrioritiserOptionMarshaller();
        settingsBuilder = new SettingsBuilder();
    }

    private ExomiserSettings applyValueAndBuildSettings(String arg) {
        String[] args = {arg};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        ExomiserSettings settings = settingsBuilder.build();
        return settings;
    }
    
    @Test(expected = CommandLineParseError.class)
    public void testApplyValuesToSettingsBuilder_throwsException_EmptyValue() {
        String[] args = {""};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
    }
    
    @Test(expected = CommandLineParseError.class)
    public void testApplyValuesToSettingsBuilder_throwsException_UnrecognisedValue() {
        String[] args = {"wibble"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_isCaseInsensitive() {
        ExomiserSettings settings = applyValueAndBuildSettings("HiPhive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Test
    public void testApplyValuesToSettingsBuilder_hiphive() {
        ExomiserSettings settings = applyValueAndBuildSettings("hiphive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }
          
    @Test
    public void testApplyValuesToSettingsBuilder_phenix() {
        ExomiserSettings settings = applyValueAndBuildSettings("phenix");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.PHENIX_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_phive() {
        ExomiserSettings settings = applyValueAndBuildSettings("phive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.PHIVE_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_exomewalker() {
        ExomiserSettings settings = applyValueAndBuildSettings("exomewalker");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.EXOMEWALKER_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_omim() {
        ExomiserSettings settings = applyValueAndBuildSettings("omim");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_uberPheno() {
        ExomiserSettings settings = applyValueAndBuildSettings("uber-pheno");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.UBERPHENO_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_none() {
        ExomiserSettings settings = applyValueAndBuildSettings("none");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.NONE));
    }
    
}
