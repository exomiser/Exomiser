/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FrequencyDaoMvStoreTest extends AllelePropertiesDaoAdapterTest {

    private Variant buildVariant(int chr, int pos, String ref, String alt) {
        return TestFactory.variantBuilder(chr, pos, ref, alt).build();
    }

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        FrequencyDao instance = newInstanceWithData(Map.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataNoData() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        FrequencyDao instance = newInstanceWithData(Map.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataKeyMismatchReturnsNoData() throws Exception {
        Variant variant = buildVariant(1, 54321, "C", "G");
        AlleleKey key = AlleleKey.newBuilder().setChr(1).setPosition(12345).setRef("A").setAlt("T").build();
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321")
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 4, 1000))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.ESP_AA, 1, 30000))
                .build();
        FrequencyDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataJustRsId() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321").build();
        FrequencyDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.of("rs54321")));
    }

    @Test
    public void getFrequencyDataWithFrequencies() throws Exception {
        Variant variant = buildVariant(1, 12345, "A", "T");
        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321")
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 4, 1000))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.ESP_AA, 3, 6000, 1))
                .build();
        FrequencyDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getFrequencyData(variant),
                equalTo(FrequencyData.of("rs54321",
                        Frequency.of(FrequencySource.THOUSAND_GENOMES, 4, 1000, 0),
                        Frequency.of(FrequencySource.ESP_AA, 3, 6000, 1))));
    }

}