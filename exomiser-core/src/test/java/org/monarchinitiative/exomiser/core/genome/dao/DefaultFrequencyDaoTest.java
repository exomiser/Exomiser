/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.genome.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, DefaultFrequencyDao.class})
@Sql(scripts = {
        "file:src/test/resources/sql/create_frequency.sql",
        "file:src/test/resources/sql/frequencyDaoTestData.sql"
})
public class DefaultFrequencyDaoTest {

    @Autowired
    private DefaultFrequencyDao instance;

    private Variant variantNotInDatabase;
    private Variant variantInDatabaseWithRsId;

    private RsId rsId = RsId.valueOf(121918506);
    private Frequency dbSnp = Frequency.valueOf(0.01f, FrequencySource.THOUSAND_GENOMES);
    private Frequency espAll = Frequency.valueOf(0.02f, FrequencySource.ESP_ALL);
    private Frequency espAa = Frequency.valueOf(0.03f, FrequencySource.ESP_AFRICAN_AMERICAN);
    private Frequency espEa = Frequency.valueOf(0.04f, FrequencySource.ESP_EUROPEAN_AMERICAN);

    private static final FrequencyData NO_DATA = FrequencyData.empty();

    @Before
    public void setUp() {
        variantNotInDatabase = VariantEvaluation.builder(1, 124, "T", "G").build();
        //Exomiser currently uses ONE_BASED numbering - be wary....
        variantInDatabaseWithRsId = VariantEvaluation.builder(10, 123256215, "T", "G").build();
    }

    @Test
    public void testVariantNotInDatabaseReturnsAnEmptyFrequencyData() {
        FrequencyData result = instance.getFrequencyData(variantNotInDatabase);

        assertThat(result, equalTo(NO_DATA));
        assertThat(result.isRepresentedInDatabase(), is(false));
    }

    @Test
    public void testVariantInDatabaseReturnsFrequencyData() {
        FrequencyData result = instance.getFrequencyData(variantInDatabaseWithRsId);
        FrequencyData expected = FrequencyData.of(rsId, dbSnp, espAa, espAll, espEa);
        assertThat(result, equalTo(expected));
        assertThat(result.isRepresentedInDatabase(), is(true));
    }
       
}
