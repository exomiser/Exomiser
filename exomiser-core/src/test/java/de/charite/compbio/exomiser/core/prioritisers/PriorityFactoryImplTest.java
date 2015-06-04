/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PriorityFactoryTestConfig.class)
@Sql(scripts = {"file:src/test/resources/sql/create_disease.sql", "file:src/test/resources/sql/create_disease_hp.sql", "file:src/test/resources/sql/diseaseDaoTestData.sql"})
public class PriorityFactoryImplTest {

    @Autowired
    private PriorityFactoryImpl instance;
    private SettingsBuilder settingsBuilder;

    @Before
    public void setUp() {
        settingsBuilder = new SettingsBuilder();
    }

    private PrioritiserSettings buildValidSettingsWithPrioritiser(PriorityType priorityType) {
        settingsBuilder.vcfFilePath(Paths.get("stubFilePath"));
        settingsBuilder.usePrioritiser(priorityType);
        return settingsBuilder.build();
    }

    private SettingsBuilder getValidSettingsWithPrioritiser(PriorityType priorityType) {
        settingsBuilder.vcfFilePath(Paths.get("stubFilePath"));
        settingsBuilder.usePrioritiser(priorityType);
        return settingsBuilder;
    }

    @Test
    public void testmakePrioritiserForNotSetPriorityReturnsNoneType() {
        PriorityType type = PriorityType.NOT_SET;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }

    @Test
    public void testCanGetOmimPrioritizerByType() {
        PriorityType type = PriorityType.OMIM_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);
        
        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForExomeWalkerPriority() {
        PriorityType type = PriorityType.EXOMEWALKER_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForHiPhivePriority() {
        PriorityType type = PriorityType.HI_PHIVE_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakeHiPhivePrioritiserWithDiseaseIdAndEmptyHpoList() {
        PriorityType type = PriorityType.HI_PHIVE_PRIORITY;
        List<String> emptyStringList = Collections.emptyList();
        PrioritiserSettings settings = getValidSettingsWithPrioritiser(type)
                .diseaseId("OMIM:101600")
                .hpoIdList(emptyStringList)
                .build();

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForPhivePriority() {
        PriorityType type = PriorityType.PHIVE_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test(expected = RuntimeException.class)
    public void testmakePrioritiserForPhenixPriorityThrowsRuntimeExceptionDueToMissingPhenixData() {
        PriorityType type = PriorityType.PHENIX_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForUberPhenoPriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.UBERPHENO_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }

    @Test
    public void testmakePrioritiserNonePriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.NONE;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }
    
    @Test
    public void testmakePrioritiserNotSetPriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.NOT_SET;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }
}
