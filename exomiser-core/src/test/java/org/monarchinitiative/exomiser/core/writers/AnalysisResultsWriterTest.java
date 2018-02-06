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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisResultsWriterTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private AnalysisResults analysisResults;
    private Analysis analysis = Analysis.builder().build();

    @Before
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        Gene rbm8a = TestFactory.newGeneRBM8A();
        analysisResults = AnalysisResults.builder().genes(Arrays.asList(fgfr2, rbm8a)).build();
    }

    @Test
    public void testWriteToFileOutputsAllmodesOfinheritanceForEachFormat() throws Exception {
        String outputPrefix = tmpFolder.newFile("test").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        AnalysisResultsWriter.writeToFile(analysis, analysisResults, settings);

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
    public void testWriteToFileOutputsAllmodesOfinheritanceForEachFormatWhenInheritanceModeIsUndefined() throws Exception {
        String outputPrefix = tmpFolder.newFile("test").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.HTML, OutputFormat.VCF))
                .build();

        Analysis analysis = Analysis.builder().modeOfInheritance(EnumSet.noneOf(ModeOfInheritance.class)).build();
        AnalysisResultsWriter.writeToFile(analysis, analysisResults, settings);

        for (OutputFormat outputFormat : Arrays.asList(OutputFormat.HTML, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF)) {
            Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, outputFormat.getFileExtension()));
            assertThat(outputPath.toFile().exists(), is(true));
            assertThat(outputPath.toFile().delete(), is(true));
        }
    }

    @Test
    public void testWriteToFileOutputsSingleHtmlFileIfPresentInSettings() throws Exception {
        String outputPrefix = tmpFolder.newFile("test").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.of(OutputFormat.HTML))
                .build();

        AnalysisResultsWriter.writeToFile(analysis, analysisResults, settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(true));
    }

    @Test
    public void testWriteToFileDoesNotOutputsSingleHtmlFileIfAbsentFromSettings() throws Exception {
        String outputPrefix = tmpFolder.newFile("test").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .outputFormats(EnumSet.noneOf(OutputFormat.class))
                .build();

        AnalysisResultsWriter.writeToFile(analysis, analysisResults, settings);

        Path outputPath = Paths.get(String.format("%s.%s", outputPrefix, OutputFormat.HTML.getFileExtension()));
        assertThat(outputPath.toFile().exists(), is(false));
    }
}