/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;
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
public class PriorityFactoryTest {

    @Autowired
    private PriorityFactory instance;
    private SettingsBuilder settingsBuilder;

    @Before
    public void setUp() {
        settingsBuilder = new SettingsBuilder();
    }

    private ExomiserSettings buildValidSettingsWithPrioritiser(PriorityType priorityType) {
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
    public void testmakePrioritisersForNonePriorityReturnsEmptyList() {
        PriorityType type = PriorityType.NONE;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertThat(prioritisers.isEmpty(), is(true));
    }

    @Test
    public void testmakePrioritisersForNotSetPriorityReturnsEmptyList() {
        PriorityType type = PriorityType.NOT_SET;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertThat(prioritisers.isEmpty(), is(true));
    }

    @Test
    public void testmakePrioritisersForOmimPriorityReturnsOneOmimPrioritiser() {
        PriorityType type = PriorityType.OMIM_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testCanGetOmimPrioritizerByType() {
        PriorityType type = PriorityType.OMIM_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);
        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritisersForExomeWalkerPriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.EXOMEWALKER_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test
    public void testmakePrioritisersForHiPhivePriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.HI_PHIVE_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test
    public void testmakeHiPhivePrioritiserWithDiseaseIdAndEmptyHpoListReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.HI_PHIVE_PRIORITY;
        List<String> emptyStringList = Collections.emptyList();
        ExomiserSettings settings = getValidSettingsWithPrioritiser(type)
                .diseaseId("OMIM:101600")
                .hpoIdList(emptyStringList)
                .build();

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test
    public void testmakePrioritisersForPhivePriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.PHIVE_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test(expected = RuntimeException.class)
    public void testmakePrioritisersForPhenixPriorityThrowsRuntimeExceptionDueToMissingPhenixData() {
        PriorityType type = PriorityType.PHENIX_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testmakePrioritisersForUberPhenoPriorityReturnsJustOmimPrioritiser() {
        PriorityType type = PriorityType.UBERPHENO_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Prioritiser> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testmakePrioritiserForUberPhenoPriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.UBERPHENO_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }

    private void assertJustOmimPrioritiserPresent(List<Prioritiser> prioritisers) {
        assertThat(prioritisers.size(), equalTo(1));
        assertThat(prioritisers.get(0).getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    private void assertOmimAndOtherSpecifiedPrioritiserPresent(List<Prioritiser> prioritisers, PriorityType type) {
        assertThat(prioritisers.size(), equalTo(2));
        assertThat(prioritisers.get(0).getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(prioritisers.get(1).getPriorityType(), equalTo(type));
    }
    
    @Test
    public void testmakePrioritiserNonePriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.NONE;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }
    
    @Test
    public void testmakePrioritiserNotSetPriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.NOT_SET;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(type, settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }
}
