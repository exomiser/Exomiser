package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class to create test data. This is intended to be run manually whenever the data sources change. Ideally,
 * this should happen rarely.
 */
@Disabled
public class TestDataBuilder {

    private static final String DATA_VERSION = "1710";
    private static final List<String> ASSEMBLIES = List.of("hg19", "hg38");

    private static final Path TEST_DATA_DIR = Path.of("src/test/resources/data");

    @Test
    void addClinVarData() {
        ASSEMBLIES.forEach(assembly -> {
                    try {
                        String buildVersion = DATA_VERSION + "_" + assembly;
                        Path buildDir = Files.createDirectories(TEST_DATA_DIR.resolve(buildVersion));
                        try (MVStore clinVarStore = MVStore.open(buildDir.resolve(buildVersion + "_clinvar.mv.db").toString())) {
                            MvStoreUtil.openClinVarMVMap(clinVarStore);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
