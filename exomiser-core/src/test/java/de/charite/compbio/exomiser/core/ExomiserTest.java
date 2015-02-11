/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.filters.FilterFactory;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.SampleData;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.springframework.util.Assert.isTrue;

/**
* Tests for Exomiser class.
*
* @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
*/
@RunWith(MockitoJUnitRunner.class)
public class ExomiserTest {

    private SettingsBuilder settingsBuilder;
    private Exomiser instance;
    private SampleData sampleData;

    @Mock
    private FilterFactory filterFactory;

    public ExomiserTest() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);


        settingsBuilder = new ExomiserSettings.SettingsBuilder();
        instance = new Exomiser();
        sampleData = new SampleData();
    }

    @Ignore
    @Test
    public void testFullAnalysis() {
        //given
        sampleData = new SampleData();
        ExomiserSettings exomiserSettings = settingsBuilder.runFullAnalysis(true).build();
        //when
        instance.analyse(sampleData, exomiserSettings);
        //then
    }

    @Ignore
    @Test
    public void testQuickAnalysis() {
        sampleData = new SampleData();
        ExomiserSettings exomiserSettings = settingsBuilder.runFullAnalysis(false).build();
        instance.analyse(sampleData, exomiserSettings);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
