package org.monarchinitiative.exomiser.data.genome;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.parsers.DbNsfpColumnIndex;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbNsfp4AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.SpliceAiAlleleResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class VariantDatabaseBuildRunnerTest {

    @Test
    void testBuild(@TempDir Path tempDir) throws IOException {
        BuildInfo buildInfo = BuildInfo.of(GenomeAssembly.HG38, "2404");
        List<AlleleResource> alleleResources = List.of(new DbNsfp4AlleleResource("hg38.dbnsfp", null, Path.of("src/test/resources/dbNSFP4.0_test.zip"), DbNsfpColumnIndex.HG38));
        VariantDatabaseBuildRunner instance = new VariantDatabaseBuildRunner(buildInfo, tempDir, alleleResources);
        instance.run();
        Path variantDatabase = instance.variantDatabasePath();
        assertThat(Files.exists(variantDatabase), is(true));
        assertThat(Files.size(variantDatabase), greaterThan(0L));
    }

    @Disabled("Manual testing")
    @Test
    void manualTestBuild() throws IOException {
        BuildInfo buildInfo = BuildInfo.of(GenomeAssembly.HG19, "2404");
        Path buildPath = Path.of("/data/exomiser-build/");
        List<AlleleResource> alleleResources = List.of(new SpliceAiAlleleResource("hg19.splice-ai", null, buildPath.resolve("spliceai_max_delta_scores_sig_only.masked.snv.hg19.vcf.gz")));
        VariantDatabaseBuildRunner instance = new VariantDatabaseBuildRunner(buildInfo, buildPath, alleleResources);
        instance.run();
        Path variantDatabase = instance.variantDatabasePath();
        assertThat(Files.exists(variantDatabase), is(true));
        assertThat(Files.size(variantDatabase), greaterThan(0L));
    }

}