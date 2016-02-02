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
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

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
@ContextConfiguration(classes = DaoTestConfig.class)
@Sql(scripts = { "file:src/test/resources/sql/create_frequency.sql",
        "file:src/test/resources/sql/frequencyDaoTestData.sql" })
public class DefaultFrequencyDaoTest {

    @Autowired
    private DefaultFrequencyDao instance;

    Variant variantNotInDatabase;
    Variant variantInDatabaseWithRsId;

    RsId rsId = new RsId(121918506);
    Frequency dbSnp = new Frequency(0.01f, FrequencySource.THOUSAND_GENOMES);
    Frequency espAll = new Frequency(0.02f, FrequencySource.ESP_ALL);
    Frequency espAa = new Frequency(0.03f, FrequencySource.ESP_AFRICAN_AMERICAN);
    Frequency espEa = new Frequency(0.04f, FrequencySource.ESP_EUROPEAN_AMERICAN);

    private static final FrequencyData NO_DATA = FrequencyData.EMPTY_DATA;

    @Before
    public void setUp() {
        variantNotInDatabase = new VariantEvaluation.VariantBuilder(1, 124, "T", "G").build();
        //Exomiser currently uses ONE_BASED numbering - be wary....
        variantInDatabaseWithRsId = new VariantEvaluation.VariantBuilder(10, 123256215, "T", "G").build();
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
        FrequencyData expected = new FrequencyData(rsId, dbSnp, espAa, espAll, espEa);
        assertThat(result, equalTo(expected));
        assertThat(result.isRepresentedInDatabase(), is(true));
    }
       
}
