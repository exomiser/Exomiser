package org.monarchinitiative.exomiser.data.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.ClinVarWhiteListReader;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ClinVarBuildRunnerTest {

    @Test
    void run(@TempDir Path tempDir) throws Exception {
        BuildInfo buildInfo = BuildInfo.of(GenomeAssembly.HG19, "2307");

        Path testResourcePath = Path.of("src/test/resources/clinvar-test.vcf.gz");
        ClinVarAlleleResource clinVarAlleleResource = new ClinVarAlleleResource("clinvar", new URL("https://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh37/clinvar.vcf.gz"), testResourcePath);
        ResourceDownloader.download(clinVarAlleleResource);

        Path testJannovarFilePath = Path.of("src/test/resources/clinvar-test-transcript-data.ser");
        JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(testJannovarFilePath);

        ClinVarBuildRunner instance = new ClinVarBuildRunner(buildInfo, tempDir, clinVarAlleleResource, jannovarData);
        instance.run();

        Path outputFile = instance.getOutFile();
        assertThat(Files.exists(outputFile), is(true));

        try (MVStore clinvarStore = new MVStore.Builder().fileName(outputFile.toString()).readOnly().open()) {
            // The ClinVar data is used for the ClinVarDao and for building the WhiteList (along with optional user data)
            MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinvar = MvStoreUtil.openClinVarMVMap(clinvarStore);
            assertThat(clinvar.size(), equalTo(2000));
            clinvar.values().forEach(clinvarProto -> assertThat(clinvarProto.getVariantEffect() != AlleleProto.VariantEffect.SEQUENCE_VARIANT, is(true)));
            Set<AlleleProto.AlleleKey> whiteListAlleleKeys = ClinVarWhiteListReader.readVariantWhiteList(clinvarStore);
            assertThat(whiteListAlleleKeys.size(), equalTo(23));
        }
    }

}