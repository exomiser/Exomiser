package org.monarchinitiative.exomiser.cli.commands.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.exomiser.cli.commands.BatchCommand;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.JobParser;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

class BatchVcfValidationTest {

    @TempDir
    Path tempDir;

    private JobParser jobParser;
    private Exomiser exomiser;
    private Path sampleFile;

    @BeforeEach
    void setUp() throws IOException {
        // Keep parsing and file I/O real; no annotation database is needed for a dry run.
        GenomeAnalysisServiceProvider genomeServices = mock(GenomeAnalysisServiceProvider.class);
        when(genomeServices.hasServiceFor(GenomeAssembly.HG19)).thenReturn(true);
        OntologyService ontologyService = mock(OntologyService.class);
        when(ontologyService.getCurrentHpoIds(List.of())).thenReturn(List.of());
        jobParser = new JobParser(genomeServices, null, ontologyService);
        exomiser = mock(Exomiser.class);
        sampleFile = Files.writeString(tempDir.resolve("sample.yml"), "genomeAssembly: hg19\nproband: proband\n");
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "malformed", "directory", "empty"})
    void dryRunReportsVcfReadFailures(String kind) throws IOException {
        Path vcf = tempDir.resolve(kind + ".vcf");
        switch (kind) {
            case "malformed" -> Files.writeString(vcf, "this is not a VCF header\n");
            case "directory" -> Files.createDirectory(vcf);
            case "empty" -> Files.createFile(vcf);
        }
        assertReadFailure(vcf, null);
    }

    @Test
    void dryRunReportsPermissionDenied() throws IOException {
        Path vcf = writeVcf("denied.vcf", "proband");
        assumeTrue(Files.getFileStore(vcf).supportsFileAttributeView(PosixFileAttributeView.class),
                "Requires POSIX file permissions");
        var originalPermissions = Files.getPosixFilePermissions(vcf);
        try {
            Files.setPosixFilePermissions(vcf, Set.of());
            assumeTrue(!Files.isReadable(vcf), "Process bypasses POSIX read permissions");
            // Prove a real permission denial, rather than substituting a missing file or mock.
            assertThrows(AccessDeniedException.class, () -> {
                try (var ignored = Files.newInputStream(vcf)) {
                    fail("The test VCF must be unreadable by this process");
                }
            });
            assertReadFailure(vcf, "AccessDeniedException");
        } finally {
            Files.setPosixFilePermissions(vcf, originalPermissions);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"proband", ""})
    void dryRunAcceptsValidHeaderIncludingSitesOnly(String sampleName) throws IOException {
        Path vcf = writeVcf("valid.vcf", sampleName);
        Path batch = writeBatch(command(vcf));
        assertEquals(0, runDryRun(batch));
        assertFalse(Files.exists(Path.of(batch + ".errors")));
    }

    @Test
    void dryRunAcceptsPhenotypeOnlyWithoutVcfOrProband() throws IOException {
        Files.writeString(sampleFile, "genomeAssembly: hg19\n");
        Path batch = writeBatch("--sample " + sampleFile + " --preset PHENOTYPE_ONLY");
        assertEquals(0, runDryRun(batch));
        assertFalse(Files.exists(Path.of(batch + ".errors")));
    }

    @Test
    void validatesEveryLineAndPreservesSampleAndArgumentErrors() throws IOException {
        String missing = command(tempDir.resolve("missing.vcf"));
        String valid = command(writeVcf("valid.vcf", "proband"));
        String mismatch = command(writeVcf("mismatch.vcf", "someone_else"));
        String malformedArgs = "--unknown-option";
        String sitesOnly = command(writeVcf("sites-only.vcf", ""));
        String noVcf = "--sample " + sampleFile;
        Path batch = writeBatch("# ignored comment", "", missing, valid, mismatch, malformedArgs, sitesOnly, noVcf);

        var results = new BatchCommandRunner.BatchValidator(jobParser).validateBatchFile(batch);
        assertEquals(6, results.checkedCount());
        assertEquals(3, results.errorCount());
        assertEquals(List.of(missing, mismatch, malformedArgs), results.sampleValidationErrors().stream()
                .map(SampleValidationError::inputLine).toList());
        assertTrue(results.sampleValidationErrors().get(1).errorMessage().contains("not found in the VCF sample"));
        assertTrue(results.sampleValidationErrors().get(2).errorMessage().contains("Unknown option"));

        assertEquals(1, runDryRun(batch));
        String errors = Files.readString(Path.of(batch + ".errors"));
        assertEquals(3, errors.lines().filter("ERROR:"::equals).count());
        assertTrue(errors.contains("line: " + missing));
        assertTrue(errors.contains("line: " + mismatch));
        assertTrue(errors.contains("line: " + malformedArgs));
        assertFalse(errors.contains("line: " + valid));
        assertFalse(errors.contains("line: " + sitesOnly));
    }

    private void assertReadFailure(Path vcf, String expectedCause) throws IOException {
        String line = command(vcf);
        Path batch = writeBatch(line);
        assertEquals(1, runDryRun(batch));
        String errors = Files.readString(Path.of(batch + ".errors"));
        assertTrue(errors.contains("line: " + line), errors);
        String cause = errors.lines().filter(s -> s.startsWith("  cause: ")).findFirst().orElseThrow();
        assertTrue(cause.contains(vcf.toString()), cause);
        assertTrue(cause.contains("Unable to parse header"), cause);
        if (expectedCause != null) {
            assertTrue(cause.contains(expectedCause), cause);
        }
    }

    private int runDryRun(Path batch) {
        // Exercise the actual --dry-run option and runner, without Spring/database startup.
        BatchCommand command = new BatchCommand();
        new CommandLine(command).parseArgs(batch.toString(), "--dry-run");
        BatchCommandRunner runner = new BatchCommandRunner(jobParser, exomiser);
        int exitCode = runner.run(command);
        assertEquals(exitCode, runner.getExitCode());
        verifyNoInteractions(exomiser);
        return exitCode;
    }

    private Path writeBatch(String... lines) throws IOException {
        return Files.write(tempDir.resolve("batch.txt"), List.of(lines));
    }

    private String command(Path vcf) {
        return "--sample " + sampleFile + " --vcf " + vcf + " --assembly hg19";
    }

    private Path writeVcf(String filename, String sampleName) throws IOException {
        String columns = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO";
        if (!sampleName.isEmpty()) {
            columns += "\tFORMAT\t" + sampleName;
        }
        return Files.writeString(tempDir.resolve(filename), "##fileformat=VCFv4.2\n" + columns + "\n");
    }
}
