package org.monarchinitiative.exomiser.autoconfigure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class TestDataDirectoriesTest {
    @Test
    void testDirectoryStructureAndFiles(@TempDir Path dataDir) {
        String dataVersion = "2402";
        TestDataDirectories.setupDataDirectories(dataDir, dataVersion);
        Set<Path> testDirPaths;
        try (Stream<Path> paths = Files.walk(dataDir, FileVisitOption.FOLLOW_LINKS)) {
            testDirPaths = paths.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // setup expected directory structure and containing files
        String hg19Version = dataVersion + "_hg19";
        Path hg19Dir = dataDir.resolve(hg19Version);

        String hg38Version = dataVersion + "_hg38";
        Path hg38Dir = dataDir.resolve(hg38Version);

        String phenoVersion = dataVersion + "_phenotype";
        Path phenoDir = dataDir.resolve(phenoVersion);

        Path remmDir = dataDir.resolve("remm");

        Set<Path> expected = Set.of(
                dataDir,
                // hg19
                hg19Dir,
                hg19Dir.resolve(hg19Version + "_clinvar.mv.db"),
                hg19Dir.resolve(hg19Version + "_genome.mv.db"),
                hg19Dir.resolve(hg19Version + "_transcripts_ensembl.ser"),
                hg19Dir.resolve(hg19Version + "_transcripts_refseq.ser"),
                hg19Dir.resolve(hg19Version + "_transcripts_ucsc.ser"),
                hg19Dir.resolve(hg19Version + "_variants.mv.db"),
                // hg38
                hg38Dir,
                hg38Dir.resolve(hg38Version + "_clinvar.mv.db"),
                hg38Dir.resolve(hg38Version + "_genome.mv.db"),
                hg38Dir.resolve(hg38Version + "_transcripts_ensembl.ser"),
                hg38Dir.resolve(hg38Version + "_transcripts_refseq.ser"),
                hg38Dir.resolve(hg38Version + "_transcripts_ucsc.ser"),
                hg38Dir.resolve(hg38Version + "_variants.mv.db"),
                // phenotype
                phenoDir,
                phenoDir.resolve("rw_string_10.mv"),
                phenoDir.resolve(dataVersion + "_phenotype.mv.db"),
                // remm
                remmDir,
                remmDir.resolve("remmData.tsv.gz"),
                remmDir.resolve("remmData.tsv.gz.tbi")
        );
        assertThat(testDirPaths, equalTo(expected));
    }
}