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
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.AlleleKeyDataType;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.AllelePropertiesDataType;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultFrequencyDaoMvStoreProtoTest {

    public static final String FREQ_MAP_NAME = "alleles";

    private DefaultFrequencyDaoMvStoreProto getInstance(String mapName, Map<AlleleKey, AlleleProperties> value) {
        MVStore mvStore = buildMvStore(mapName, value);
        return new DefaultFrequencyDaoMvStoreProto(mvStore);
    }

    private MVStore buildMvStore(String mapName, Map<AlleleKey, AlleleProperties> value) {

        MVStore mvStore = new MVStore.Builder().open();

        MVMap.Builder<AlleleKey, AlleleProperties> alleleMapBuilder = new MVMap.Builder<AlleleKey, AlleleProperties>()
                .keyType(AlleleKeyDataType.INSTANCE)
                .valueType(AllelePropertiesDataType.INSTANCE);

        MVMap<AlleleKey, AlleleProperties> map = mvStore.openMap(mapName, alleleMapBuilder);
        map.putAll(value);
        return mvStore;
    }


    private Variant buildVariant(int chr, int pos, String ref, String alt) {
        return VariantAnnotation.builder()
                .chromosome(chr)
                .position(pos)
                .ref(ref)
                .alt(alt)
                .build();
    }


    private AlleleKey buildAlleleKey(Variant variant) {
        return AlleleKey.newBuilder()
                .setChr(variant.getChromosome())
                .setPosition(variant.getPosition())
                .setRef(variant.getRef())
                .setAlt(variant.getAlt())
                .build();
    }

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        DefaultFrequencyDaoMvStoreProto instance = getInstance("wibble", ImmutableMap.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataNoData() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        DefaultFrequencyDaoMvStoreProto instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataKeyMismatchReturnsNoData() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(54321).ref("C").alt("G").build();
        AlleleKey key = AlleleKey.newBuilder().setChr(1).setPosition(12345).setRef("A").setAlt("T").build();
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321")
                .putProperties("KG", 0.04f)
                .putProperties("ESP_AA", 0.003f)
                .build();
        DefaultFrequencyDaoMvStoreProto instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataJustRsId() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        AlleleKey key = buildAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321").build();
        DefaultFrequencyDaoMvStoreProto instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.of(RsId.valueOf("rs54321"))));
    }

    @Test
    public void getFrequencyDataWithFrequencies() throws Exception {
        Variant variant = buildVariant(1, 12345, "A", "T");
        AlleleKey key = buildAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321")
                .putProperties("KG", 0.04f)
                .putProperties("ESP_AA", 0.003f)
                .build();
        DefaultFrequencyDaoMvStoreProto instance = getInstance(FREQ_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getFrequencyData(variant),
                equalTo(FrequencyData.of(RsId.valueOf("rs54321"), Frequency.valueOf(0.04f, FrequencySource.THOUSAND_GENOMES), Frequency
                        .valueOf(0.003f, FrequencySource.ESP_AFRICAN_AMERICAN))));
    }

    @Ignore
    @Test
    public void testRealData() throws Exception {
        MVStore mvStore = new MVStore.Builder()
                .fileName("C:\\Users\\hhx640\\Documents\\exomiser-build\\data\\allele_importer\\alleles_all\\alleles_exac_esp_proto.mv.db")
//                .fileName("C:\\Users\\hhx640\\Documents\\exomiser-cli-dev\\data\\1707_hg19\\1707_hg19_variants.mv.db")
                .open();

        MVMap.Builder<AlleleKey, AlleleProperties> alleleMapBuilder = new MVMap.Builder<AlleleKey, AlleleProperties>()
                .keyType(AlleleKeyDataType.INSTANCE)
                .valueType(AllelePropertiesDataType.INSTANCE);

        MVMap<AlleleKey, AlleleProperties> map = mvStore.openMap("alleles", alleleMapBuilder);
        System.out.println("Map contains " + map.size() + " entries");
        System.out.println("Map contains " + map.keySet().size() + " keys");
//processed 12192520 variants total in 341 sec
//        Set<AlleleKey> extractedKeys = new HashSet<>();
//        int count = 0;
//        for (AlleleKey key : map.keySet()) {
////            while (count < 333_000_000) {
//                count++;
////                System.out.println(count  + " " + key);
//                extractedKeys.add(key);
////            }
////            break;
//        }
//
//        System.out.println("Added " + count + " keys. Extracted  " + extractedKeys.size() + " keys");
//        for (AlleleKey key : extractedKeys) {
//            System.out.println(key);
//        }


//        AlleleKey key = AlleleKey.newBuilder().setChr(1).setPosition(15447).setRef("A").setAlt("G").build();
//        AlleleKey key = AlleleKey.newBuilder().setChr(1).setPosition(909321).setRef("G").setAlt("A").build();
        AlleleKey key = AlleleKey.newBuilder().setChr(23).setPosition(36103454).setRef("A").setAlt("G").build();
        System.out.println(map.get(key));

//        Variant variant = VariantAnnotation.builder().chromosome(1).position(15447).ref("A").alt("G").build();
        Variant variant = buildVariant(1, 909321, "G", "A");

        DefaultFrequencyDaoMvStoreProto instance = new DefaultFrequencyDaoMvStoreProto(mvStore);
        DefaultPathogenicityDaoMvStoreProto pathDao = new DefaultPathogenicityDaoMvStoreProto(mvStore);

        System.out.println(instance.getFrequencyData(variant));

        Variant pfeiffer = buildVariant(10, 123256215, "T", "G");
        System.out.println(instance.getFrequencyData(pfeiffer));
        System.out.println(pathDao.getPathogenicityData(pfeiffer));


    }
}