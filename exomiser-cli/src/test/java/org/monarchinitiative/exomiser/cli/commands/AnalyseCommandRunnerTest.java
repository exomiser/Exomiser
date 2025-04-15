package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.commands.batch.BatchFileReader;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.JobParser;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

class AnalyseCommandRunnerTest {

    @Test
    void testRunAnalyse(@TempDir Path tempDir) throws IOException {
        Exomiser exomiser = Mockito.mock(Exomiser.class);
        AnalyseCommandRunner instance = new AnalyseCommandRunner(exomiser);
        AnalyseCommand analyseCommand = new AnalyseCommand();
        // --sample src/test/resources/pfeiffer-phenopacket.yml
        analyseCommand.sampleOptions.samplePath = Path.of("src/test/resources/pfeiffer-phenopacket.yml");
        analyseCommand.analysisGroup.preset = AnalysisProto.Preset.EXOME;
        var vcfParameters = new AnalyseCommand.VcfParameters();
        // --vcf src/test/resources/Pfeiffer.vcf
        vcfParameters.vcfPath = Path.of("src/test/resources/Pfeiffer.vcf");
        // --assembly hg19
        vcfParameters.assembly = GenomeAssembly.HG19;
        analyseCommand.vcfParameters = vcfParameters;
        // --output-directory " + resultsDir.toAbsolutePath()
        Path resultsDir = tempDir.resolve("results");
        analyseCommand.outputOptions.outputDirectory = resultsDir;
        // --output-format HTML,TSV_VARIANT"
        analyseCommand.outputOptions.outputFormats = List.of(OutputFormat.HTML, OutputFormat.TSV_VARIANT);
        when(exomiser.run(analyseCommand.readJob())).thenReturn(AnalysisResults.builder().build());
        Integer exitCode = instance.run(analyseCommand);
        assertThat(exitCode, equalTo(0));
        assertThat(resultsDir.toFile().listFiles().length, equalTo(2));
        assertThat(Files.exists(resultsDir.resolve("exomiser.html")), equalTo(true));
        assertThat(Files.exists(resultsDir.resolve("exomiser.variants.tsv")), equalTo(true));
    }
}