/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisResultsWriterTest {

    private Path tempFile;

    @BeforeEach
    private void getTempFile() throws IOException {
        tempFile = Files.createTempFile("exomiser_test", "");
    }

    @AfterEach
    private void deleteTempFile() throws Exception {
        Files.delete(tempFile);
    }

    private AnalysisResults newAnalysisResults() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        Gene rbm8a = TestFactory.newGeneRBM8A();
        return AnalysisResults.builder().genes(Arrays.asList(fgfr2, rbm8a)).build();
    }

    @Test
    public void testWriteToFileOutputsAllModesOfinheritanceForEachFormat() throws Exception {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        Analysis analysis = Analysis.builder()
                .vcfPath(Paths.get("src/test/resources/smallTest.vcf"))
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

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
    public void testWriteToFileOutputsAllModesOfinheritanceForEachFormatWhenInheritanceModeIsUndefined() throws Exception {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        Analysis analysis = Analysis.builder()
                .vcfPath(Paths.get("src/test/resources/smallTest.vcf"))
                .inheritanceModeOptions(InheritanceModeOptions.empty())
                .build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

        for (OutputFormat outputFormat : Arrays.asList(OutputFormat.HTML, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF)) {
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, outputFormat.getFileExtension()));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }

    @Test
    public void testWriteToFileOutputsSingleHtmlFileIfPresentInSettings() throws Exception {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.HTML))
                .build();

        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(true));
        assertThat(outputPath.toFile().delete(), is(true));
    }

    @Test
    public void testWriteToFileOutputsSingleJsonFileIfPresentInSettings() throws Exception {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.JSON))
                .build();

        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.JSON.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(true));
        assertThat(outputPath.toFile().delete(), is(true));
    }

    @Test
    public void testWriteToFileOutputsSingleJsonOrHtmlFileIfPresentInSettings() throws Exception {
        String outputPrefix = tempFile.toString();

        Set<OutputFormat> singleFileFormats = EnumSet.of(OutputFormat.JSON, OutputFormat.HTML);
        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(singleFileFormats)
                .build();

        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

        for (OutputFormat outputFormat : singleFileFormats) {
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, outputFormat.getFileExtension()));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }

    @Test
    public void testWriteToFileDoesNotOutputsSingleHtmlFileIfAbsentFromSettings() throws Exception {
        String outputPrefix = tempFile.toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.noneOf(OutputFormat.class))
                .build();

        Analysis analysis = Analysis.builder().build();
        AnalysisResultsWriter.writeToFile(analysis, newAnalysisResults(), settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(false));
    }
}