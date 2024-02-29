package org.monarchinitiative.exomiser.core.genome.dao.serialisers;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ProtobufDataTypeTest {

    private static final AlleleProto.AlleleKey key1 = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(1).setRef("T").setAlt("G").build();
    private static final AlleleProto.AlleleKey key2 = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(2).setRef("A").setAlt("C").build();
    private static final AlleleProto.AlleleKey key3 = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(2).setRef("A").setAlt("T").build();
    private static final AlleleProto.AlleleKey key4 = AlleleProto.AlleleKey.newBuilder().setChr(2).setPosition(2).setRef("A").setAlt("T").build();

    private static final AlleleProto.AlleleProperties entry1 = AlleleProto.AlleleProperties.newBuilder().addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.REVEL, 1f)).build();
    private static final AlleleProto.AlleleProperties entry2 = AlleleProto.AlleleProperties.newBuilder().addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_AFR, 1, 20000)).build();
    private static final AlleleProto.AlleleProperties entry3 = AlleleProto.AlleleProperties.newBuilder().addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MVP, 0.8f)).build();
    private static final AlleleProto.AlleleProperties entry4 = AlleleProto.AlleleProperties.newBuilder().addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_AMR, 2, 20000)).build();

    private static final Map<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> entries = Map.of(key1, entry1, key2, entry2, key3, entry3, key4, entry4);

    private static final MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> mvMap = MvStoreUtil.openAlleleMVMap(new MVStore.Builder().open());

    @BeforeAll
    static void beforeAll() {
        mvMap.putAll(entries);
    }


    @Test
    void testRoundTrip() {
        assertThat(mvMap.get(key1), equalTo(entry1));
        assertThat(mvMap.get(key2), equalTo(entry2));
        assertThat(mvMap.get(key3), equalTo(entry3));
        assertThat(mvMap.get(key4), equalTo(entry4));
    }

    @Test
    void testKeyOrder() {
        var keyList = mvMap.keySet().stream().toList();
        assertThat(keyList, equalTo(List.of(key1, key2, key3, key4)));
    }
}