package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class VariantMvStoresTest {

    @Test
    void testOpenMvStore(@TempDir Path tempDir) {
        try (MVStore mvStore = VariantMvStores.openMvStore(tempDir.resolve("test-mv"))) {
            var allelesMap = MvStoreUtil.openAlleleMVMap(mvStore);
            assertNotNull(allelesMap);
        }
    }

    @Test
    void testMergeStores(@TempDir Path tempDir) {
        AlleleProto.AlleleKey key = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(12345).setRef("A").setAlt("T").build();
        AlleleProto.AlleleProperties properties = AlleleProto.AlleleProperties.newBuilder()
                .setRsId("rs12345")
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_AFR, 1, 12345))
                .build();

//        AlleleProto.AlleleProperties properties2 = AlleleProto.AlleleProperties.newBuilder()
//                .setRsId("rs12345")
//                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_G_NFE, 1, 54321))
//                .build();

        Path store1Path = tempDir.resolve("store1");
        Path store2Path = tempDir.resolve("store2");
        Path store3Path = tempDir.resolve("store3");
        try (MVStore store1 = VariantMvStores.openMvStore(store1Path);
             MVStore store2 = VariantMvStores.openMvStore(store2Path);
             MVStore store3 = VariantMvStores.openMvStore(store3Path)) {
            var allelesMap1 = MvStoreUtil.openAlleleMVMap(store1);
            allelesMap1.put(key, properties);
            var allelesMap2 = MvStoreUtil.openAlleleMVMap(store2);
            allelesMap2.put(key, properties);
            var allelesMap3 = MvStoreUtil.openAlleleMVMap(store3);
            allelesMap3.put(key, properties);
        }


//        Map<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> expected = Map.of();
        Path mergedStorePath = tempDir.resolve("merged-mv");
        VariantMvStores.mergeStores(mergedStorePath, List.of(store1Path, store2Path, store3Path));
        try (MVStore mergedStore = openMvStore(mergedStorePath)) {
            var mergedAlleles = MvStoreUtil.openAlleleMVMap(mergedStore);
            assertThat(Map.copyOf(mergedAlleles), equalTo(Map.of(key, properties)));
        }
    }

    @Disabled("Do this properly!")
    @Test
    void testMergeStores() {
        Path stores = Path.of("/home/hhx640/Documents/exomiser-build/test/hg19/variants/processed");
        VariantMvStores.mergeStores(Path.of("/home/hhx640/Documents/exomiser-build/test/2210_hg19_merged.mv.db"), mvStoresToMerge(stores, "*.mv.db"));
    }

    private List<Path> mvStoresToMerge(Path processedDir, String glob) {
        List<Path> found = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(processedDir, glob)) {
            for (Path path : directoryStream) {
                found.add(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return List.copyOf(found);
    }

    public static MVStore openMvStore(Path mvStoreAbsolutePath) {
        return new MVStore.Builder()
                .fileName(mvStoreAbsolutePath.toString())
                .readOnly()
                .open();
    }

}