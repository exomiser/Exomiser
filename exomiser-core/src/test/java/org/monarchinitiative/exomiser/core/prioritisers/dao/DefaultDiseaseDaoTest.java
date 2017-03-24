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
package org.monarchinitiative.exomiser.core.prioritisers.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, DefaultDiseaseDao.class})
@Sql(scripts = {
        "file:src/test/resources/sql/create_disease.sql",
        "file:src/test/resources/sql/create_disease_hp.sql",
        "file:src/test/resources/sql/create_entrez2sym.sql",
        "file:src/test/resources/sql/diseaseDaoTestData.sql"})
public class DefaultDiseaseDaoTest {
    
    @Autowired
    DefaultDiseaseDao instance;

    private final Disease disease = Disease.builder()
            .diseaseId("OMIM:101600")
                .diseaseName("Craniofacial-skeletal-dermatologic dysplasia")
                .associatedGeneId(2263)
                .associatedGeneSymbol("FGFR2")
                .diseaseType(Disease.DiseaseType.DISEASE)
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .phenotypeIds(Arrays.asList("HP:0000174, HP:0000194, HP:0000218, HP:0000238, HP:0000244, HP:0000272, HP:0000303, HP:0000316, HP:0000322, HP:0000324, HP:0000327, HP:0000348, HP:0000431, HP:0000452, HP:0000453, HP:0000470, HP:0000486, HP:0000494, HP:0000508, HP:0000586, HP:0000678, HP:0001156, HP:0001249, HP:0002308, HP:0002676, HP:0002780, HP:0003041, HP:0003070, HP:0003196, HP:0003272, HP:0003307, HP:0003795, HP:0004209, HP:0004322, HP:0004440, HP:0005048, HP:0005280, HP:0005347, HP:0006101, HP:0006110, HP:0009602, HP:0009773, HP:0010055, HP:0010669, HP:0011304".split(", ")))
            .build();

    @Test
    public void testGetHpoIdsForDiseaseId() {
        Set<String> omim101600HpoIds = Sets.newTreeSet(disease.getPhenotypeIds());
        assertThat(instance.getHpoIdsForDiseaseId("OMIM:101600"), equalTo(omim101600HpoIds));
    }

    @Test
    public void testGetDiseaseDataAssociatedWithGeneId() {
        List<Disease> expected = Lists.newArrayList(disease) ;
        assertThat(instance.getDiseaseDataAssociatedWithGeneId(2263), equalTo(expected));
    }
}
