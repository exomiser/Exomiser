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
package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DefaultDiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.service.ModelServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        PriorityFactoryImpl.class,
        PriorityFactoryTestConfig.class,
        PriorityService.class,
        OntologyServiceImpl.class,
        PhenotypeMatchService.class,
        ModelServiceImpl.class,
        DefaultDiseaseDao.class,
        HumanPhenotypeOntologyDao.class,
        MousePhenotypeOntologyDao.class,
        ZebraFishPhenotypeOntologyDao.class
})
@Sql(scripts = {
        "file:src/test/resources/sql/create_disease.sql",
        "file:src/test/resources/sql/create_disease_hp.sql",
        "file:src/test/resources/sql/create_entrez2sym.sql",
        "file:src/test/resources/sql/diseaseDaoTestData.sql"
})
public class PriorityFactoryImplTest {

    @Autowired
    private PriorityFactoryImpl instance;

    private PrioritiserSettings buildValidSettingsWithPrioritiser(PriorityType priorityType) {
        return PrioritiserSettings.builder().usePrioritiser(priorityType).build();
    }

    @Test
    public void testCanGetOmimPrioritizerByType() {
        PriorityType type = PriorityType.OMIM_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForExomeWalkerPriority() {
        PriorityType type = PriorityType.EXOMEWALKER_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForHiPhivePriority() {
        PriorityType type = PriorityType.HIPHIVE_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakeHiPhivePrioritiserWithDiseaseIdAndEmptyHpoList() {
        PriorityType type = PriorityType.HIPHIVE_PRIORITY;
        List<String> emptyStringList = Collections.emptyList();
        PrioritiserSettings settings = PrioritiserSettings.builder()
                .usePrioritiser(type)
                .diseaseId("OMIM:101600")
                .hpoIdList(emptyStringList)
                .build();

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserForPhivePriority() {
        PriorityType type = PriorityType.PHIVE_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test(expected = RuntimeException.class)
    public void testmakePrioritiserForPhenixPriorityThrowsRuntimeExceptionDueToMissingPhenixData() {
        PriorityType type = PriorityType.PHENIX_PRIORITY;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(type));
    }

    @Test
    public void testmakePrioritiserNonePriorityReturnsNoneTypePrioritiser() {
        PriorityType type = PriorityType.NONE;
        PrioritiserSettings settings = buildValidSettingsWithPrioritiser(type);

        Prioritiser prioritiser = instance.makePrioritiser(settings);
        assertThat(prioritiser.getPriorityType(), equalTo(PriorityType.NONE));
    }

}
