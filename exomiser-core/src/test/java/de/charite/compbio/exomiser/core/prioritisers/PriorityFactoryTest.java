/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import java.nio.file.Paths;
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
@Sql(scripts = {"file:src/test/resources/sql/create_disease.sql"})
public class PriorityFactoryTest {

    @Autowired
    private PriorityFactory instance;
    private SettingsBuilder settingsBuilder;

    @Before
    public void setUp() {
        settingsBuilder = new SettingsBuilder();
    }

    @Test
    public void testmakePrioritisersForNotSetPriorityReturnsJustOmimPrioritiser() {
        PriorityType type = PriorityType.NOT_SET;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testmakePrioritisersForOmimPriorityReturnsOneOmimPrioritiser() {
        PriorityType type = PriorityType.OMIM_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testmakePrioritisersForExomeWalkerPriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.EXOMEWALKER_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test
    public void testmakePrioritisersForExomiserAllSpeciesPriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.EXOMISER_ALLSPECIES_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test
    public void testmakePrioritisersForExomiserMousePriorityReturnsThatAndOmimPrioritisers() {
        PriorityType type = PriorityType.EXOMISER_MOUSE_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertOmimAndOtherSpecifiedPrioritiserPresent(prioritisers, type);
    }

    @Test(expected = RuntimeException.class)
    public void testmakePrioritisersForPhenixPriorityThrowsRuntimeExceptionDueToMissingPhenixData() {
        PriorityType type = PriorityType.PHENIX_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    @Test
    public void testmakePrioritisersForUberPhenoPriorityReturnsJustOmimPrioritiser() {
        PriorityType type = PriorityType.UBERPHENO_PRIORITY;
        ExomiserSettings settings = buildValidSettingsWithPrioritiser(type);

        List<Priority> prioritisers = instance.makePrioritisers(settings);
        assertJustOmimPrioritiserPresent(prioritisers);
    }

    private void assertJustOmimPrioritiserPresent(List<Priority> prioritisers) {
        assertThat(prioritisers.size(), equalTo(1));
        assertThat(prioritisers.get(0).getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    private void assertOmimAndOtherSpecifiedPrioritiserPresent(List<Priority> prioritisers, PriorityType type) {
        assertThat(prioritisers.size(), equalTo(2));
        assertThat(prioritisers.get(0).getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(prioritisers.get(1).getPriorityType(), equalTo(type));
    }

    private ExomiserSettings buildValidSettingsWithPrioritiser(PriorityType priorityType) {
        settingsBuilder.vcfFilePath(Paths.get("stubFilePath"));
        settingsBuilder.usePrioritiser(priorityType);
        return settingsBuilder.build();
    }

}
