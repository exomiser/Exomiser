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

package org.monarchinitiative.exomiser.core.genome.dao;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantSerialiserTest {

    @Test
    public void generateKey() throws Exception {
        VariantAnnotation variant = VariantAnnotation.builder().chromosome(25).position(1234).ref("A").alt("T").build();
        assertThat(VariantSerialiser.generateKey(variant), equalTo("25-1234-A-T"));
    }

    @Test
    public void infoFieldToMapInfoEmptyString() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap(""), equalTo(Collections.emptyMap()));
    }

    @Test
    public void infoFieldToMapVcfEmpty() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap("."), equalTo(Collections.emptyMap()));
    }

    @Test
    public void infoFieldToMapInfoSeparatorOnly() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap(";"), equalTo(Collections.emptyMap()));
    }

    @Test
    public void infoFieldToMapNonKeyValuePair() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap("sass;that;hoopy;frood;ford;prefect"), equalTo(Collections.emptyMap()));
    }

    @Test
    public void infoFieldToMapOneKeyValuePair() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap("KEY=VALUE"), equalTo(ImmutableMap.of("KEY", "VALUE")));
    }

    @Test
    public void infoFieldToMapMultipleKeyValuePair() throws Exception {
        assertThat(VariantSerialiser.infoFieldToMap("RS=rs12345;KG=0.004"),
                equalTo(ImmutableMap.of("RS", "rs12345", "KG", "0.004")));
    }
}