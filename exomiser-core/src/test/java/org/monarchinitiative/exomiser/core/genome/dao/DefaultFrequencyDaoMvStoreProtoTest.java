/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultFrequencyDaoMvStoreProtoTest {

    private DefaultFrequencyDaoMvStoreProto newInstanceWithData(Map<AlleleKey, AlleleProperties> value) {
        MVStore mvStore = MvAlleleStoreTestUtil.newMvStoreWithData(value);
        return new DefaultFrequencyDaoMvStoreProto(mvStore);
    }

    private Variant buildVariant(int chr, int pos, String ref, String alt) {
        return VariantAnnotation.builder()
                .chromosome(chr)
                .position(pos)
                .ref(ref)
                .alt(alt)
                .build();
    }

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        DefaultFrequencyDaoMvStoreProto instance = newInstanceWithData(ImmutableMap.of());
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataNoData() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        DefaultFrequencyDaoMvStoreProto instance = newInstanceWithData(ImmutableMap.of());
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
        DefaultFrequencyDaoMvStoreProto instance = newInstanceWithData(ImmutableMap.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.empty()));
    }

    @Test
    public void getFrequencyDataJustRsId() throws Exception {
        Variant variant = buildVariant(1, 123245, "A", "T");
        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321").build();
        DefaultFrequencyDaoMvStoreProto instance = newInstanceWithData(ImmutableMap.of(key, properties));
        assertThat(instance.getFrequencyData(variant), equalTo(FrequencyData.of(RsId.valueOf("rs54321"))));
    }

    @Test
    public void getFrequencyDataWithFrequencies() throws Exception {
        Variant variant = buildVariant(1, 12345, "A", "T");
        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder().setRsId("rs54321")
                .putProperties("KG", 0.04f)
                .putProperties("ESP_AA", 0.003f)
                .build();
        DefaultFrequencyDaoMvStoreProto instance = newInstanceWithData(ImmutableMap.of(key, properties));
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

        MVMap<AlleleKey, AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);
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