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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.GenomicVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class JsonResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.JSON;

    @Override
    public void writeFile(AnalysisResults analysisResults, OutputSettings settings) {
        Sample sample = analysisResults.getSample();
        Path outFile = settings.makeOutputFilePath(sample.getVcfPath(), OUTPUT_FORMAT);
        ObjectWriter objectWriter = new ObjectMapper()
                .addMixIn(GenomicVariant.class, JsonVariantMixin.class)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
                .writer();
        try (Writer bufferedWriter = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            writeData(analysisResults, settings, objectWriter, bufferedWriter);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFile, ex);
        }
        logger.debug("{} results written to file {}", OUTPUT_FORMAT, outFile);
    }

    @Override
    public String writeString(AnalysisResults analysisResults, OutputSettings settings) {
        ObjectWriter objectWriter = new ObjectMapper()
                .addMixIn(GenomicVariant.class, JsonVariantMixin.class)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
                .writerWithDefaultPrettyPrinter();
        try (Writer stringWriter = new StringWriter()) {
            writeData(analysisResults, settings, objectWriter, stringWriter);
            stringWriter.flush();
            logger.info("{} results written to string", OUTPUT_FORMAT);
            return stringWriter.toString();
        } catch (IOException ex) {
            logger.error("Unable to write results to json.", ex);
        }
        return "";
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings settings, ObjectWriter objectWriter, Writer writer) throws IOException {
        List<Gene> compatibleGenes = analysisResults.getGenes();
        if (settings.outputContributingVariantsOnly()) {
            logger.debug("Writing out only CONTRIBUTING variants");
            List<Gene> passedGenes = makePassedGenes(compatibleGenes);
            objectWriter.writeValue(writer, settings.filterGenesForOutput(passedGenes));
        } else {
            objectWriter.writeValue(writer, settings.filterGenesForOutput(compatibleGenes));
        }
    }

    private List<Gene> makePassedGenes(List<Gene> compatibleGenes) {
        List<Gene> passedGenes = new ArrayList<>();
        for (Gene gene : compatibleGenes) {
            if (gene.passedFilters()) {
                Gene makePassOnlyGene = makeContributingOnlyGene(gene);
                passedGenes.add(makePassOnlyGene);
            }
        }
        return passedGenes;
    }

    private Gene makeContributingOnlyGene(Gene gene) {
        Gene contributingOnlyGene = new Gene(gene.getGeneIdentifier());
        contributingOnlyGene.setCompatibleInheritanceModes(gene.getCompatibleInheritanceModes());
        gene.getPriorityResults().values().forEach(contributingOnlyGene::addPriorityResult);
        gene.getVariantEvaluations().stream()
                .filter(VariantEvaluation::contributesToGeneScore)
                .forEach(contributingOnlyGene::addVariant);
        gene.getGeneScores()
                .forEach(contributingOnlyGene::addGeneScore);
        return contributingOnlyGene;
    }

}
