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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class JsonResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.JSON;

    @Override
    public void writeFile(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);
        ObjectWriter objectWriter = new ObjectMapper().writer();
        try (Writer bufferedWriter = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            writeData(modeOfInheritance, analysisResults, settings.outputContributingVariantsOnly(), objectWriter, bufferedWriter);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFileName, ex);
        }
        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, (modeOfInheritance.getAbbreviation() == null) ? "ALL" : modeOfInheritance
                .getAbbreviation(), outFileName);
    }

    @Override
    public String writeString(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        //Add prettyPrintJson option to outputSettings?
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try (Writer stringWriter = new StringWriter()) {
            writeData(modeOfInheritance, analysisResults, settings.outputContributingVariantsOnly(), objectWriter, stringWriter);
            stringWriter.flush();
            logger.info("{} {} results written to string", OUTPUT_FORMAT, (modeOfInheritance.getAbbreviation() == null) ? "ALL" : modeOfInheritance
                    .getAbbreviation());
            return stringWriter.toString();
        } catch (IOException ex) {
            logger.error("Unable to write results to json.", ex);
        }
        return "";
    }

    private void writeData(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, boolean writeOnlyContributingVariants, ObjectWriter objectWriter, Writer writer) throws IOException {
        List<Gene> compatibleGenes = getCompatibleGene(modeOfInheritance, analysisResults.getGenes());

        if (writeOnlyContributingVariants) {
            logger.debug("Writing out only CONTRIBUTING variants");
            List<Gene> passedGenes = makePassedGenes(modeOfInheritance, compatibleGenes);
            objectWriter.writeValue(writer, passedGenes);
        } else {
            objectWriter.writeValue(writer, compatibleGenes);
        }
    }

    private List<Gene> getCompatibleGene(ModeOfInheritance modeOfInheritance, List<Gene> genes) {
        if (modeOfInheritance == ModeOfInheritance.ANY) {
            return genes;
        }
        return genes.stream()
                .filter(gene -> gene.isCompatibleWith(modeOfInheritance))
                .collect(toList());
    }

    private List<Gene> makePassedGenes(ModeOfInheritance modeOfInheritance, List<Gene> compatibleGenes) {
        List<Gene> passedGenes = new ArrayList<>();
        for (Gene gene : compatibleGenes) {
            if (gene.passedFilters() && gene.isCompatibleWith(modeOfInheritance)) {
                Gene makePassOnlyGene = makeContributingOnlyGene(modeOfInheritance, gene);
                passedGenes.add(makePassOnlyGene);
            }
        }
        return passedGenes;
    }

    private Gene makeContributingOnlyGene(ModeOfInheritance modeOfInheritance, Gene gene) {
        Gene contributingOnlyGene = new Gene(gene.getGeneIdentifier());
        contributingOnlyGene.setCompatibleInheritanceModes(gene.getCompatibleInheritanceModes());
        gene.getPriorityResults().values().forEach(contributingOnlyGene::addPriorityResult);

        if (modeOfInheritance == ModeOfInheritance.ANY) {
            gene.getVariantEvaluations().stream()
                    .filter(VariantEvaluation::contributesToGeneScore)
                    .forEach(contributingOnlyGene::addVariant);
            gene.getGeneScores()
                    .forEach(contributingOnlyGene::addGeneScore);
            return contributingOnlyGene;
        } else {
            gene.getVariantEvaluations().stream()
                    .filter(ve -> ve.contributesToGeneScoreUnderMode(modeOfInheritance))
                    .forEach(contributingOnlyGene::addVariant);
            gene.getGeneScores().stream()
                    .filter(geneScore -> geneScore.getModeOfInheritance() == modeOfInheritance)
                    .forEach(contributingOnlyGene::addGeneScore);
            return contributingOnlyGene;
        }
    }

}
