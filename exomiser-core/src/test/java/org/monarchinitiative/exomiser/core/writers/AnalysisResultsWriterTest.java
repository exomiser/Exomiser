/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.writers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AnalysisResultsWriterTest {

    private Path tempFile;

    @BeforeEach
    private void getTempFile() throws IOException {
        tempFile = Files.createTempFile("exomiser_test", "");
    }

    @AfterEach
    private void deleteTempFile() throws Exception {
        Files.delete(tempFile);
    }

    private AnalysisResults newAnalysisResults(Sample sample, Analysis analysis) {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        Gene rbm8a = TestFactory.newGeneRBM8A();
        return AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Arrays.asList(fgfr2, rbm8a))
                .build();
    }

    @Test
    void testWriteToFileOutputsAllModesOfInheritanceForEachFormat() {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        Sample sample = Sample.builder()
                .vcfPath(Paths.get("src/test/resources/Pfeiffer.vcf"))
                .build();

        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        //HTML writer is a special case where it presents a combined view of the results
        Path htmlOutputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(htmlOutputPath.toFile().exists(), is(true));
        assertThat(htmlOutputPath.toFile().delete(), is(true));

        //The other writers only write the MOI compatible genes/variants
        for (OutputFormat outputFormat : Arrays.asList(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF)) {
            for (String moi : Arrays.asList("AD", "AR", "XR", "XD", "MT")) {
                Path outputPath = Paths.get(String.format("%s_%s.%s", outputPrefix, moi, outputFormat.getFileExtension()));
                assertThat(outputPath.toFile().exists(), is(true));
                assertThat(outputPath.toFile().delete(), is(true));
            }
        }
    }

    @Test
    void testWriteToFileOutputsAllModesOfInheritanceForEachFormatWhenInheritanceModeIsUndefined() {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        Sample sample = Sample.builder()
                .vcfPath(Paths.get("src/test/resources/Pfeiffer.vcf"))
                .build();

        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.empty())
                .build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        for (OutputFormat outputFormat : Arrays.asList(OutputFormat.HTML, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF)) {
            String fileExtension = outputFormat.getFileExtension();
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, fileExtension.equals("vcf") ? "vcf.gz" : fileExtension));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }

    @Test
    void testWriteToFileOutputsSingleHtmlFileIfPresentInSettings() {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.HTML))
                .build();

        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(true));
        assertThat(outputPath.toFile().delete(), is(true));
    }

    @Test
    void testWriteToFileOutputsSingleJsonFileIfPresentInSettings() {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.JSON))
                .build();

        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.JSON.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(true));
        assertThat(outputPath.toFile().delete(), is(true));
    }

    @Test
    void testWriteToFileOutputsSingleJsonOrHtmlFileIfPresentInSettings() {
        String outputPrefix = tempFile.toString();

        Set<OutputFormat> singleFileFormats = EnumSet.of(OutputFormat.JSON, OutputFormat.HTML);
        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(singleFileFormats)
                .build();

        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        for (OutputFormat outputFormat : singleFileFormats) {
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, outputFormat.getFileExtension()));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }

    @Test
    void testWriteToFileDoesNotOutputSingleHtmlFileIfAbsentFromSettings() {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.noneOf(OutputFormat.class))
                .build();

        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(false));
    }

    @Test
    void testWriteToFileWithOutputOptions() {
        String outputPrefix = tempFile.toString();

        List<String> outputFormats = Arrays.stream(OutputFormat.values()).map(OutputFormat::toString).collect(Collectors.toList());

        OutputProto.OutputOptions outputOptions = OutputProto.OutputOptions.newBuilder()
                .setOutputPrefix(outputPrefix)
                .addAllOutputFormats(outputFormats)
                .build();

        Sample sample = Sample.builder()
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .pedigree(TestPedigrees.trioChildAffected())
                .genomeAssembly(GenomeAssembly.HG19)
                .vcfPath(Paths.get("src/test/resources/multiSampleWithProbandHomRef.vcf"))
                .build();

        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(newAnalysisResults(sample, analysis), outputOptions);

        for (OutputFormat outputFormat : OutputFormat.values()) {
            String fileExtension = outputFormat.getFileExtension();
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, fileExtension.equals("vcf") ? "vcf.gz" : fileExtension));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }
}