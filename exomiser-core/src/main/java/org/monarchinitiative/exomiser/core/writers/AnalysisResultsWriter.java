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
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for handling writing out {@link org.monarchinitiative.exomiser.core.analysis.AnalysisResults}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultsWriter.class);

    private AnalysisResultsWriter() {
    }

    public static void writeToFile(Analysis analysis, AnalysisResults analysisResults, OutputSettings outputSettings) {
        ResultsWriterFactory resultsWriterFactory = new ResultsWriterFactory();
        logger.info("Writing results...");
        writeResultsToHtmlFile(analysis, analysisResults, outputSettings);

        InheritanceModeOptions inheritanceModeOptions = analysis.getInheritanceModeOptions();
        if (inheritanceModeOptions.isEmpty()) {
            writeForInheritanceMode(ModeOfInheritance.ANY, analysis, outputSettings, resultsWriterFactory, analysisResults);
        } else {
            for (ModeOfInheritance modeOfInheritance : inheritanceModeOptions.getDefinedModes()) {
                logger.info("Writing {} results:", modeOfInheritance);
                // Can't do this in parallel because theses are mutated each time for a different mode here.
                // AnalysisResults could return a view for a ModeOfInheritance which can be called by the Writer
                // without interfering with other writes for different modes. Check RAM requirements.
                // Will only save a few seconds, so is not a rate-limiting step.
                analysisResults.getGenes().sort(Gene.comparingScoreForInheritanceMode(modeOfInheritance));

                writeForInheritanceMode(modeOfInheritance, analysis, outputSettings, resultsWriterFactory, analysisResults);
            }
        }
    }

    private static void writeForInheritanceMode(ModeOfInheritance modeOfInheritance, Analysis analysis, OutputSettings outputSettings, ResultsWriterFactory resultsWriterFactory, AnalysisResults analysisResults) {
        for (OutputFormat outFormat : outputSettings.getOutputFormats()) {
            if (outFormat != OutputFormat.HTML) {
                ResultsWriter resultsWriter = resultsWriterFactory.getResultsWriter(outFormat);
                resultsWriter.writeFile(modeOfInheritance, analysis, analysisResults, outputSettings);
            }
        }
    }

    private static void writeResultsToHtmlFile(Analysis analysis, AnalysisResults analysisResults, OutputSettings outputSettings) {
        if (outputSettings.getOutputFormats().contains(OutputFormat.HTML)) {
            ResultsWriter htmlResults = new HtmlResultsWriter();
            htmlResults.writeFile(ModeOfInheritance.ANY, analysis, analysisResults, outputSettings);
        }
    }
}
