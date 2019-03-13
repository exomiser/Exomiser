/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.service.HpoIdChecker;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DefaultDiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.service.ModelServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(SpringExtension.class)
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
        ZebraFishPhenotypeOntologyDao.class,
        HpoIdChecker.class
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

    @Test
    public void testMakeOmimPrioritiser() {
        assertThat(instance.makeOmimPrioritiser(), instanceOf(OmimPriority.class));
    }

    @Test
    public void testmakePrioritiserForExomeWalkerPriority() {
        assertThat(instance.makeExomeWalkerPrioritiser(Collections.emptyList()), instanceOf(ExomeWalkerPriority.class));
    }

    @Test
    public void testmakePrioritiserForHiPhivePriority() {
        assertThat(instance.makeHiPhivePrioritiser(HiPhiveOptions.defaults()), instanceOf(HiPhivePriority.class));
    }

    @Test
    public void testmakeHiPhivePrioritiserWithDiseaseIdAndEmptyHpoList() {
        HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder()
                .diseaseId("OMIM:101600")
                .build();
        assertThat(instance.makeHiPhivePrioritiser(hiPhiveOptions), instanceOf(HiPhivePriority.class));
    }

    @Test
    public void testmakePrioritiserForPhivePriority() {
        assertThat(instance.makePhivePrioritiser(), instanceOf(PhivePriority.class));
    }

    @Test
    public void testmakePrioritiserForPhenixPriorityThrowsRuntimeExceptionDueToMissingPhenixData() {
        assertThrows(NullPointerException.class, () -> instance.makePhenixPrioritiser());
    }

}
