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
package org.monarchinitiative.exomiser.core.model.frequency;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencySourceTest {

    @Test
    public void testValues() {
        FrequencySource[] values = {
                FrequencySource.UNKNOWN,
                FrequencySource.LOCAL,
                FrequencySource.THOUSAND_GENOMES,
                FrequencySource.ESP_AFRICAN_AMERICAN,
                FrequencySource.ESP_EUROPEAN_AMERICAN,
                FrequencySource.ESP_ALL,
                FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN,
                FrequencySource.EXAC_AMERICAN,
                FrequencySource.EXAC_EAST_ASIAN,
                FrequencySource.EXAC_SOUTH_ASIAN,
                FrequencySource.EXAC_FINNISH,
                FrequencySource.EXAC_NON_FINNISH_EUROPEAN,
                FrequencySource.EXAC_OTHER};

        assertThat(FrequencySource.values(), equalTo(values));
    }

    @Test
    public void testValueOf() {
        assertThat(FrequencySource.valueOf("THOUSAND_GENOMES"), equalTo(FrequencySource.THOUSAND_GENOMES));
    }

    @Test
    public void testGetSource() {
        assertThat(FrequencySource.LOCAL.getSource(), equalTo("Local"));
    }

    @Test
    public void testGetAllExternalFrequencySources(){
        assertThat(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES.size(), equalTo(11));
    }

    @Test
    public void testGetAllEspFrequencySources(){
        assertThat(FrequencySource.ALL_ESP_SOURCES.size(), equalTo(3));
    }

    @Test
    public void testGetAllExacFrequencySources(){
        assertThat(FrequencySource.ALL_EXAC_SOURCES.size(), equalTo(7));
    }
}
