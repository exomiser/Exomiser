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

import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Facade for handling writing out {@link org.monarchinitiative.exomiser.core.analysis.AnalysisResults}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultsWriter.class);

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
        logger.debug("Writing results...");
        createOutputDirectoriesIfNotExists(outputSettings);

        if (outputSettings.getOutputFormats().isEmpty()) {
            ResultsWriter resultsWriter = new HtmlResultsWriter();
            resultsWriter.writeFile(analysisResults, outputSettings);
        }

        for (OutputFormat outputFormat : outputSettings.getOutputFormats()) {
            var resultsWriter = ResultsWriterFactory.getResultsWriter(outputFormat);
            logger.debug("Writing {} results", outputFormat);
            resultsWriter.writeFile(analysisResults, outputSettings);
        }

//        Analysis analysis = analysisResults.getAnalysis();
//        InheritanceModeOptions inheritanceModeOptions = analysis.getInheritanceModeOptions();
//        for (ModeOfInheritance modeOfInheritance : inheritanceModeOptions.getDefinedModes()) {
//            if (modeOfInheritance != ModeOfInheritance.ANY) {
//                logger.debug("Writing {} results:", modeOfInheritance);
//                // Can't do this in parallel because theses are mutated each time for a different mode here.
//                // AnalysisResults could return a view for a ModeOfInheritance which can be called by the Writer
//                // without interfering with other writes for different modes. Check RAM requirements.
//                // Will only save a few seconds, so is not a rate-limiting step.
//                // TODO: For removal in v14.0 - analysisResults.getGenes() will be immutable in v14.0.0
//                analysisResults.getGenes().sort(Gene.comparingScoreForInheritanceMode(modeOfInheritance));
//                writeForInheritanceMode(modeOfInheritance, outputFormatsForAnyMoi, analysisResults, outputSettings);
//            }
//        }
    }

    private static void createOutputDirectoriesIfNotExists(OutputSettings outputSettings) {
        Path outputDir = ResultsWriterUtils.resolveOutputDir(outputSettings.getOutputPrefix());
        if (Files.notExists(outputDir)) {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create Exomiser output path due to " + e.getMessage());
            }
        }
    }
}
