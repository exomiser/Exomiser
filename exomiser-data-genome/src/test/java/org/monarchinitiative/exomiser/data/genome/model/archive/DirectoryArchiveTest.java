package org.monarchinitiative.exomiser.data.genome.model.archive;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class DirectoryArchiveTest {

    private final Path gnomadTestDir = Path.of("src/test/resources/gnomad-test/");

    @Test
    void throwsExceptionWhenNoDirectoryFound() {
        Path filePath = gnomadTestDir.resolve("/wibble");
        assertThrows(IllegalArgumentException.class,
                () -> new DirectoryArchive(filePath, "", ""),
                "Directory not found: " + filePath);
    }

    @Test
    void throwsExceptionWhenProvidedFilePathInConstructor() {
        Path filePath = gnomadTestDir.resolve("chr1.vcf.bgz");
        assertThrows(IllegalArgumentException.class,
                () -> new DirectoryArchive(filePath, "", ""),
                "archivePath must be a directory but a file was provided: " + filePath);
    }

    @Test
    void lines() {
        DirectoryArchive instance = new DirectoryArchive(gnomadTestDir, "", "bgz");
        List<String> lines = instance.lines()
//                .peek(System.out::println)
                .toList();
        assertThat(lines.get(0), equalTo("##fileformat=VCFv4.0"));
        assertThat(lines.get(lines.size() -1), equalTo("2\t10109\trs376007522\tA\tT\t.\t.\tRS=376007522;RSPOS=10109;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050100020005000002000100;GENEINFO=DDX11L1:100287102;WGT=1;VC=SNV;SLO;R5;ASP"));

    }
}