/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, RegulatoryFeatureDao.class})
@Sql(scripts = {
        "file:src/test/resources/sql/create_regulatory_features.sql",
        "file:src/test/resources/sql/regulatoryFeatureTestData.sql"
})
public class RegulatoryFeatureDaoTest {

    @Autowired
    private RegulatoryFeatureDao instance;

    @Test
    public void testGetAllRegulatoryFeatures() {
        List<RegulatoryFeature> regulatoryFeatures = new ArrayList<>();

//        (11,	96762600,	96763399,	'Enhancer'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96762600, 96763399, RegulatoryFeature.FeatureType.ENHANCER));
//        (11,	96798354,	96798827,	'FANTOM permissive'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96798354, 96798827, RegulatoryFeature.FeatureType.FANTOM_PERMISSIVE));
//        (11,	96820460,	96821548,	'unrecognised type');
//        regulatoryFeatures.add(new RegulatoryFeature(11, 96820460, 96821548, UNKNOWN));

        List<RegulatoryFeature> results = instance.getRegulatoryFeatures();

        assertThat(results.size(), equalTo(2));

        for (RegulatoryFeature result : results) {
            assertThat(regulatoryFeatures, hasItem(result));
            assertThat(result.getFeatureType(), not(RegulatoryFeature.FeatureType.UNKNOWN));
        }
    }

}