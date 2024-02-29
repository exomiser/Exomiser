package org.monarchinitiative.exomiser.data.genome.model.resource.sv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.genome.ResourceDownloader;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

class NoOpSvResourceTest {

    @Test
    void testDownloaderDownloadsNothing(@TempDir Path tempDir) {
        Path downloadedFile = tempDir.resolve("gnomad-sv.vcf");
        NoOpSvResource instance = new NoOpSvResource("hg38.gnomad-sv", new FileArchive(downloadedFile), Path.of(""));

        assertThat(Files.exists(downloadedFile), is(false));
        ResourceDownloader.download(instance);
        assertThat(Files.exists(downloadedFile), is(true));
    }

    @Test
    void testOutputFileIsCreatedOnIndexResource(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("gnomad-sv.pg");
        NoOpSvResource instance = new NoOpSvResource("hg38.gnomad-sv", new FileArchive(Path.of("")), outputFile);

        assertThat(Files.exists(outputFile), is(false));
        instance.indexResource();
        assertThat(Files.exists(outputFile), is(true));
        try (Stream<String> lines = Files.lines(outputFile)) {
            assertThat(lines.count(), equalTo(0L));
        }
    }
}