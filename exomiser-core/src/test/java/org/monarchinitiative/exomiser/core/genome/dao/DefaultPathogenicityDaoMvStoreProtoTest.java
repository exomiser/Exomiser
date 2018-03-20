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
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.AlleleKeyDataType;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.AllelePropertiesDataType;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.MutationTasterScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.SiftScore;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultPathogenicityDaoMvStoreProtoTest {

    public static final String PATH_MAP_NAME = "alleles";

    private DefaultPathogenicityDaoMvStoreProto getInstance(String mapName, Map<AlleleKey, AlleleProperties> value) {
        MVStore mvStore = buildMvStore(mapName, value);
        return new DefaultPathogenicityDaoMvStoreProto(mvStore);
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

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        DefaultPathogenicityDaoMvStoreProto instance = getInstance("wibble", ImmutableMap.of());
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNoData() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of());
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNonMissenseVariant() throws Exception {
        Variant frameShiftVariant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .build();
        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of());
        assertThat(instance.getPathogenicityData(frameShiftVariant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNoInfo() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.getDefaultInstance();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNonPathogenicityInfo() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .putProperties("KG", 0.04f)
                .build();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataJustSift() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .putProperties("KG", 0.04f)
                .putProperties("SIFT", 0.0f)
                .build();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(SiftScore.valueOf(0f))));
    }

    @Test
    public void getPathogenicityDataJustPolyphen() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .putProperties("POLYPHEN", 1.0f)
                .build();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(PolyPhenScore.valueOf(1f))));
    }

    @Test
    public void getPathogenicityDataJustMutationTaster() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .putProperties("MUT_TASTER", 1.0f)
                .build();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(MutationTasterScore.valueOf(1f))));
    }

    @Test
    public void getPathogenicityDataAll() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .putProperties("POLYPHEN", 1.0f)
                .putProperties("MUT_TASTER", 1.0f)
                .putProperties("SIFT", 0.0f)
                .build();

        DefaultPathogenicityDaoMvStoreProto instance = getInstance(PATH_MAP_NAME, ImmutableMap.of(key, properties));

        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(SiftScore.valueOf(0f), PolyPhenScore
                .valueOf(1f), MutationTasterScore.valueOf(1f))));
    }
}