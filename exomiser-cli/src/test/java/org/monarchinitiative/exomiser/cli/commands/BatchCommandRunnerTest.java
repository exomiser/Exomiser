package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.commands.batch.BatchFileReader;
import org.monarchinitiative.exomiser.cli.commands.batch.SampleValidationError;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.JobParser;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

class BatchCommandRunnerTest {

    @Test
    void testRunBatch(@TempDir Path tempDir) throws IOException {
        Exomiser exomiser = Mockito.mock(Exomiser.class);
        BatchCommandRunner instance = new BatchCommandRunner(Mockito.mock(JobParser.class), exomiser);
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        Path resultsDir = tempDir.resolve("results");
        writeToBatchFile(batchCommand.batchFilePath, "--sample src/test/resources/pfeiffer-phenopacket.yml --vcf src/test/resources/Pfeiffer.vcf --assembly hg19 --output-directory " + resultsDir.toAbsolutePath() + " --output-format HTML,TSV_VARIANT,PARQUET");
        List<JobProto.Job> jobs = BatchFileReader.readJobsFromBatchFile(batchCommand.batchFilePath);
        when(exomiser.run(jobs.getFirst())).thenReturn(AnalysisResults.builder().build());
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(0));
        assertThat(resultsDir.toFile().listFiles().length, equalTo(3));
        assertThat(Files.exists(resultsDir.resolve("exomiser.html")), equalTo(true));
        assertThat(Files.exists(resultsDir.resolve("exomiser.variants.tsv")), equalTo(true));
        assertThat(Files.exists(resultsDir.resolve("exomiser.parquet")), equalTo(true));
    }

    @Test
    void testDryRunNoSuchInputFile(@TempDir Path tempDir) {
        BatchCommandRunner instance = new BatchCommandRunner(new JobParser(null, null, null), new Exomiser(null));
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = tempDir.resolve("batch.txt");
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(1));
    }

    @Test
    void testDryRunEmptyInputFile(@TempDir Path tempDir) throws IOException {
        BatchCommandRunner instance = new BatchCommandRunner(new JobParser(null, null, null), new Exomiser(null));
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(1));
    }

    @Test
    void testDryRunSingleValidAnalysis(@TempDir Path tempDir) throws IOException {
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = Mockito.mock(GenomeAnalysisServiceProvider.class);
        when(genomeAnalysisServiceProvider.hasServiceFor(GenomeAssembly.HG19)).thenReturn(true);
        OntologyService ontologyService = Mockito.mock(OntologyService.class);
        when(ontologyService.getCurrentHpoIds(List.of())).thenReturn(List.of());
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, null, ontologyService);
        Exomiser exomiser = Mockito.mock(Exomiser.class);
        BatchCommandRunner instance = new BatchCommandRunner(jobParser, exomiser);
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        writeToBatchFile(batchCommand.batchFilePath, "--sample src/test/resources/pfeiffer-phenopacket.yml --vcf src/test/resources/Pfeiffer.vcf --assembly hg19");
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(0));
    }

    @Test
    void testDryRunNonAnalyseCommands(@TempDir Path tempDir) throws IOException {
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = Mockito.mock(GenomeAnalysisServiceProvider.class);
        when(genomeAnalysisServiceProvider.hasServiceFor(GenomeAssembly.HG19)).thenReturn(true);
        OntologyService ontologyService = Mockito.mock(OntologyService.class);
        when(ontologyService.getCurrentHpoIds(List.of())).thenReturn(List.of());
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, null, ontologyService);
        Exomiser exomiser = Mockito.mock(Exomiser.class);
        BatchCommandRunner instance = new BatchCommandRunner(jobParser, exomiser);
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        String errorLine = "--wibble src/test/resources/pfeiffer-phenopacket.yml --vcf src/test/resources/Pfeiffer.vcf --assembly hg19";
        writeToBatchFile(batchCommand.batchFilePath, errorLine);
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(1));
        List<SampleValidationError> sampleValidationErrors = readValidationErrors(tempDir.resolve("batch.txt.errors"));
        assertThat(sampleValidationErrors, equalTo(List.of(new SampleValidationError(errorLine, "Unknown options: '--wibble', 'src/test/resources/pfeiffer-phenopacket.yml'"))));
    }

    @Test
    void testDryRunSingleNonValidAnalysis(@TempDir Path tempDir) throws IOException {
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = Mockito.mock(GenomeAnalysisServiceProvider.class);
        when(genomeAnalysisServiceProvider.getProvidedAssemblies()).thenReturn(Set.of(GenomeAssembly.HG19));
        OntologyService ontologyService = Mockito.mock(OntologyService.class);
        when(ontologyService.getCurrentHpoIds(List.of())).thenReturn(List.of());
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, null, ontologyService);
        BatchCommandRunner instance = new BatchCommandRunner(jobParser, Mockito.mock(Exomiser.class));
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        String errorLine = "--sample src/test/resources/pfeiffer-phenopacket.yml --vcf src/test/resources/Pfeiffer.vcf --assembly hg38";
        writeToBatchFile(batchCommand.batchFilePath, errorLine);
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(1));
        List<SampleValidationError> sampleValidationErrors = readValidationErrors(tempDir.resolve("batch.txt.errors"));
        assertThat(sampleValidationErrors, equalTo(List.of(new SampleValidationError(errorLine, "Assembly hg38 not supported in this instance. Supported assemblies are: [hg19]"))));
    }

    @Test
    void testDryRunSingleMismatchedVcfSampleAnalysis(@TempDir Path tempDir) throws IOException {
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = Mockito.mock(GenomeAnalysisServiceProvider.class);
        when(genomeAnalysisServiceProvider.hasServiceFor(GenomeAssembly.HG19)).thenReturn(true);
        OntologyService ontologyService = Mockito.mock(OntologyService.class);
        when(ontologyService.getCurrentHpoIds(List.of())).thenReturn(List.of());
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, null, ontologyService);
        BatchCommandRunner instance = new BatchCommandRunner(jobParser, Mockito.mock(Exomiser.class));
        BatchCommand batchCommand = new BatchCommand();
        batchCommand.dryRun = true;
        batchCommand.batchFilePath = Files.createFile(tempDir.resolve("batch.txt"));
        String errorLine = "--sample src/test/resources/pfeiffer-phenopacket.yml --vcf src/main/resources/vcf/Pfeiffer-quartet.vcf.gz --assembly hg19";
        writeToBatchFile(batchCommand.batchFilePath, errorLine);
        Integer exitCode = instance.run(batchCommand);
        assertThat(exitCode, equalTo(1));
        List<SampleValidationError> sampleValidationErrors = readValidationErrors(tempDir.resolve("batch.txt.errors"));
        assertThat(sampleValidationErrors, equalTo(List.of(new SampleValidationError(errorLine, "Proband sample name 'manuel' is not found in the VCF sample. Expected one of [ISDBM322015, ISDBM322016, ISDBM322017, ISDBM322018]. Please check your sample and analysis files match."))));
    }

    private static void writeToBatchFile(Path batchFilePath, String... commandLines) throws IOException {
        try(var writer = Files.newBufferedWriter(batchFilePath)) {
            writer.write("# this is a test batch file");
            writer.newLine();
            for (String commandLine : commandLines) {
                writer.write(commandLine);
                writer.newLine();
            }
        }
    }

    private static List<SampleValidationError> readValidationErrors(Path errorFile) throws IOException {
        List<SampleValidationError> errors = new ArrayList<>();
        try(var reader = Files.newBufferedReader(errorFile)) {
            String command = "";
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("ERROR:")) {
                    command = "";
                    continue;
                } if (line.startsWith("  line:")) {
                    command = line.substring(line.indexOf(':') + 2);
                } if (line.startsWith("  cause:")) {
                    var cause = line.substring(line.indexOf(':') + 2);
                    errors.add(new SampleValidationError(command, cause));
                }
            }
        }
        return List.copyOf(errors);
    }
}