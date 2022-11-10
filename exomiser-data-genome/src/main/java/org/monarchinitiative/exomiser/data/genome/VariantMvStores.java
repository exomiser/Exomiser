package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreTool;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProtoFormatter;
import org.monarchinitiative.exomiser.data.genome.indexers.AlleleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class to help with opening and merging MvStores when building the Exomiser variant data
 * @since 14.0.0
 */
public class VariantMvStores {

    private static final Logger logger = LoggerFactory.getLogger(VariantMvStores.class);

    private VariantMvStores() {
    }

    public static MVStore openMvStore(Path processedResource) {
        MVStore mvStore = new MVStore.Builder()
                .fileName(processedResource.toString())
                .compress()
                // https://github.com/h2database/h2database/issues/373
                // clutching at straws here...
                .autoCompactFillRate(0)
                .open();
        // this is key to keep the size of the store down when building otherwise it gets enormous
        mvStore.setVersionsToKeep(0);
        return mvStore;
    }

    public static void mergeStores(Path target, List<Path> toMerge) {
        Path temp = target.getParent().resolve(target.getFileName().toString() + ".temp");
        if (toMerge.isEmpty()) {
            return;
        }
        // delete existing stores otherwise they will be merged multiple times leading to duplicates and overly-large file sizes
        try {
            Files.deleteIfExists(target);
            Files.deleteIfExists(temp);
        } catch (Exception e) {
            throw new IllegalStateException("Error trying to delete existing temp and target variant stores", e);
        }

        if (Files.notExists(temp)) {
            // find the largest file to merge and copy it to the target
            Path largest = toMerge.stream().max(new FileSizeComparator()).orElse(toMerge.get(0));
            // then merge in the remaining files
            try {
                // do a dummy merge...
                logger.info("Merging {}", largest.getFileName());
                Files.copy(largest, temp);
                try (MVStore mvStore = new MVStore.Builder().fileName(largest.toString()).readOnly().open()) {
                    MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);
                    logger.info("Merged {} alleles from {}", map.size(), largest.getFileName());
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            toMerge = toMerge.stream().filter(path -> !largest.equals(path)).toList();
        }
        // merge in remaining stores...
        try (MVStore tempStore = openMvStore(temp)) {
            MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> finalMap = MvStoreUtil.openAlleleMVMap(tempStore);
            for (Path path : toMerge) {
                mergeIntoMap(finalMap, path);
                // don't compact here as this can result in 'org.h2.mvstore.MVStoreException: Chunk 1234 not found' errors
            }
        }
        logger.info("Finished merge");
        // what the hell are we doing here? Well, it used to be the case that using mvStore.setVersionsToKeep(0) would
        // make a nice compact store, but now it just creates a massive store, even if using compactMoveChunks() at the
        // end, so we're reverting to the old behavior of copying all the data into a new file which will be
        // significantly smaller at the end of the copy than the original file >=(
        logger.info("Compacting data into final store...");
        MVStoreTool.compact(temp.toString(), target.toString(), true);
        logger.info("Deleting temp store {}", temp);
        try {
            Files.deleteIfExists(temp);
        } catch (IOException e) {
            logger.error("Unable to delete temp store {}", temp, e);
        }
    }

    private static void mergeIntoMap(MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> finalMap, Path path) {
        logger.info("Merging {}", path.getFileName());
        try (MVStore mvStore = new MVStore.Builder().fileName(path.toString()).readOnly().open()) {
            MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);
            long i = 0;
            for (Map.Entry<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> entry : map.entrySet()) {
                AlleleProto.AlleleKey alleleKey = entry.getKey();
                AlleleProto.AlleleProperties properties = entry.getValue();
                try {
                    finalMap.merge(alleleKey, properties, AlleleConverter::mergeProperties);
                    i++;
                    if (i % 10_000_000 == 0) {
                        var finalProperties = finalMap.get(alleleKey);
                        String key = AlleleProtoFormatter.format(alleleKey);
                        String props = AlleleProtoFormatter.format(finalProperties);
                        logger.info("Merged {} {} {}", i, key, props);
                    }
                } catch (Exception e) {
                    var existingProps = finalMap.get(alleleKey);
                    throw new IllegalStateException("Error trying to merge " + AlleleProtoFormatter.format(alleleKey) + ": " + AlleleProtoFormatter.format(properties) + " key already linked to " + AlleleProtoFormatter.format(existingProps), e);
                }
            }
            logger.info("Merged {} alleles from {}", map.size(), path.getFileName());
        }
    }

    private static final class FileSizeComparator implements Comparator<Path> {

        @Override
        public int compare(Path o1, Path o2) {
            long o1Size;
            long o2Size;
            try {
                o1Size = Files.size(o1);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            try {
                o2Size = Files.size(o2);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return Long.compare(o1Size, o2Size);
        }
    }
}