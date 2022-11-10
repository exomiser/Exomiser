package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VariantStoreMergeRunnerTest {

    @Test
    void run(@TempDir Path tempDir) throws Exception {
        BuildInfo buildInfo = BuildInfo.of(GenomeAssembly.HG19, "2211");
        Path buildPath = tempDir.resolve(buildInfo.getVersion());
        Path processedDir = buildPath.resolve("hg19/variants/processed");

        Files.createDirectories(processedDir);
        try (MVStore clinvarStore = new MVStore.Builder().fileName(processedDir.resolve("clinvar.mv.db").toString()).open()){
        }
        try (MVStore gnomadExomesStore = new MVStore.Builder().fileName(processedDir.resolve("gnomad-exomes.mv.db").toString()).open()){
        }
        var instance = new VariantStoreMergeRunner(buildInfo, buildPath, processedDir);
        instance.run();

        Path mergedVariantStore = buildPath.resolve(buildInfo.getBuildString() + "_variants.mv.db");
        assertTrue(Files.exists(mergedVariantStore));
    }
}