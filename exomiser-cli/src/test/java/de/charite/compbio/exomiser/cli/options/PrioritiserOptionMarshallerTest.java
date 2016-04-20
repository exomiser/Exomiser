/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.cli.CommandLineParseError;
import de.charite.compbio.exomiser.core.analysis.Settings;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;

import static org.hamcrest.CoreMatchers.containsString;
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

    private Settings applyValueAndBuildSettings(String arg) {
        String[] args = {arg};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
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
        Settings settings = applyValueAndBuildSettings("HiPhive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Test
    public void testApplyValuesToSettingsBuilder_hiphive() {
        Settings settings = applyValueAndBuildSettings("hiphive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }
          
    @Test
    public void testApplyValuesToSettingsBuilder_phenix() {
        Settings settings = applyValueAndBuildSettings("phenix");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.PHENIX_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_phive() {
        Settings settings = applyValueAndBuildSettings("phive");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.PHIVE_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_exomewalker() {
        Settings settings = applyValueAndBuildSettings("exomewalker");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.EXOMEWALKER_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_omim() {
        Settings settings = applyValueAndBuildSettings("omim");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_uberPheno() {
        Settings settings = applyValueAndBuildSettings("uber-pheno");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.UBERPHENO_PRIORITY));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder_none() {
        Settings settings = applyValueAndBuildSettings("none");
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.NONE));
    }

    @Test
    public void testDescriptionContainsPrioritiserNames() {
        String description = instance.getOption().getDescription();
        System.out.println(description);
        assertThat(description, containsString("hiphive"));
        assertThat(description, containsString("phenix"));
        assertThat(description, containsString("phive"));
        assertThat(description, containsString("exomewalker"));
        assertThat(description, containsString("omim"));
        assertThat(description, containsString("uber-pheno"));
        assertThat(description, containsString("none"));
    }
}
