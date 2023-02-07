/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.Collections;

/**
 * Facade for handling writing out {@link org.monarchinitiative.exomiser.core.analysis.AnalysisResults}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultsWriter.class);
    private static final Set<OutputFormat> DEFAULT_OUTPUT_FORMATS = Collections.unmodifiableSet(EnumSet.of(OutputFormat.HTML, OutputFormat.JSON));

    private AnalysisResultsWriter() {
    }

    /**
     * @param analysisResults
     * @param outputOptions
     * @since 13.0.0
     */
    public static void writeToFile(AnalysisResults analysisResults, OutputProto.OutputOptions outputOptions) {
        OutputSettings outputSettings = new OutputSettingsProtoConverter().toDomain(outputOptions);
        writeToFile(analysisResults, outputSettings);
    }

    public static void writeToFile(AnalysisResults analysisResults, OutputSettings outputSettings) {
        Path outputDir = createOutputDirectoriesIfNotExists(outputSettings);
        logger.debug("Writing results to directory {}", outputDir);

        var outputFormats = outputSettings.getOutputFormats().isEmpty()
                ? DEFAULT_OUTPUT_FORMATS
                : outputSettings.getOutputFormats();

        for (OutputFormat outputFormat : outputFormats) {
            var resultsWriter = ResultsWriterFactory.getResultsWriter(outputFormat);
            logger.debug("Writing {} results", outputFormat);
            resultsWriter.writeFile(analysisResults, outputSettings);
        }
    }

    private static Path createOutputDirectoriesIfNotExists(OutputSettings outputSettings) {
        Path outputDir = outputSettings.getOutputDirectory();
        if (Files.notExists(outputDir)) {
            try {
                return Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create Exomiser output path due to " + e.getMessage());
            }
        }
        return outputDir;
    }

}
