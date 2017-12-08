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
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultFrequencyDaoMvStoreTest {

    public static final String FREQ_MAP_NAME = "alleles";

    private DefaultFrequencyDaoMvStore getInstance(String mapName, Map<String, String> value) {
        MVStore mvStore = buildMvStore(mapName, value);
        return new DefaultFrequencyDaoMvStore(mvStore);
    }

    private MVStore buildMvStore(String mapName, Map<String, String> value) {
        MVStore mvStore = new MVStore.Builder().open();

        MVMap<String, String> map = mvStore.openMap(mapName);
        map.putAll(value);
        return mvStore;
    }

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        DefaultFrequencyDaoMvStore instance = getInstance("wibble", ImmutableMap.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataNoData() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        DefaultFrequencyDaoMvStore instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataJustRsId() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        DefaultFrequencyDaoMvStore instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of("1-12345-A-T", "RS=rs54321"));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.of(RsId.valueOf("rs54321"))));
    }

    @Test
    public void getFrequencyDataWithFrequencies() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        DefaultFrequencyDaoMvStore instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of("1-12345-A-T", "RS=rs54321;KG=0.04;ESP_AA=0.003"));
        assertThat(instance.getFrequencyData(variant),
                equalTo(FrequencyData.of(RsId.valueOf("rs54321"), Frequency.valueOf(0.04f, FrequencySource.THOUSAND_GENOMES), Frequency
                        .valueOf(0.003f, FrequencySource.ESP_AFRICAN_AMERICAN))));
    }
}