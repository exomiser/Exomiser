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

package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.RegulatoryFeature;

import static de.charite.compbio.exomiser.core.model.RegulatoryFeature.FeatureType.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DaoTestConfig.class)
@Sql(scripts = {"file:src/test/resources/sql/create_regulatory_features.sql", "file:src/test/resources/sql/regulatoryFeatureTestData.sql"})
public class RegulatoryFeatureDaoTest {

    @Autowired
    private RegulatoryFeatureDao instance;

    @Test
    public void testGetAllRegulatoryFeatures() {
        List<RegulatoryFeature> regulatoryFeatures = new ArrayList<>();

//        (11,	96762600,	96763399,	'Enhancer'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96762600, 96763399, ENHANCER));
//        (11,	96777007,	96777375,	'Open chromatin'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96777007, 96777375, OPEN_CHROMATIN));
//        (11,	96781023,	96781500,	'TF binding site'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96781023, 96781500, TF_BINDING_SITE));
//        (11,	96781600,	96781799,	'Promoter'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96781600, 96781799, PROMOTER));
//        (11,	96794200,	96794599,	'CTCF Binding Site'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96794200, 96794599, CTCF_BINDING_SITE));
//        (11,	96798354,	96798827,	'FANTOM permissive'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96798354, 96798827, FANTOM_PERMISSIVE));
//        (11,	96817000,	96818399,	'Promoter Flanking Region'),
        regulatoryFeatures.add(new RegulatoryFeature(11, 96817000, 96818399, PROMOTER_FLANKING_REGION));
//        (11,	96820460,	96821548,	'unrecognised type');
        regulatoryFeatures.add(new RegulatoryFeature(11, 96820460, 96821548, UNKNOWN));

        List<RegulatoryFeature> results = instance.getRegulatoryFeatures();

        assertThat(results.size(), equalTo(2));// now added restriction to DAO

        for (RegulatoryFeature result : results) {
            assertThat(regulatoryFeatures, hasItem(result));
        }
    }

}