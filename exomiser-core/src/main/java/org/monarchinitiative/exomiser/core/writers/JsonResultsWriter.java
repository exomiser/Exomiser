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
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.GenomicVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
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
        Sample sample = analysisResults.sample();
        Path outFile = settings.makeOutputFilePath(sample.vcfPath(), OUTPUT_FORMAT);
        ObjectMapper objectMapper = new ObjectMapper()
                .addMixIn(Gene.class, JsonGeneMixin.class)
                .addMixIn(GenomicVariant.class, JsonVariantMixin.class)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);
        ObjectWriter objectWriter = objectMapper
                .writer();

        List<Gene> compatibleGenes = analysisResults.genes();
        List<Gene> genes;
        if (settings.outputContributingVariantsOnly()) {
            logger.debug("Writing out only CONTRIBUTING variants");
            List<Gene> passedGenes = makePassedGenes(compatibleGenes);
            genes = settings.filterGenesForOutput(passedGenes);
        } else {
            genes = settings.filterGenesForOutput(compatibleGenes);
        }

//        try (OutputStream outputStream = Files.newOutputStream(Path.of(outFile.toString().replace(".json", ".jsonl")));
//             JsonGenerator jGenerator = objectMapper
//                     .createGenerator(outputStream, JsonEncoding.UTF8)
//                     .setRootValueSeparator(new SerializedString("\n"))) {
//            for (Gene gene : genes) {
//                jGenerator.writeObject(gene);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // see https://cowtowncoder.medium.com/line-delimited-json-with-jackson-69c9e4cb6c00

        try (OutputStream outputStream = Files.newOutputStream(outFile);
             SequenceWriter seq = objectWriter
                     .withRootValueSeparator("\n") // Important! Default value separator is single space
                     .writeValues(objectMapper.createGenerator(outputStream, JsonEncoding.UTF8))) {
            for (Gene gene : genes) {
                seq.write(gene);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        try (Writer bufferedWriter = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
//            writeData(analysisResults, settings, objectWriter, bufferedWriter);
//        } catch (IOException ex) {
//            logger.error("Unable to write results to file {}", outFile, ex);
//        }
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
        List<Gene> compatibleGenes = analysisResults.genes();
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
        Gene contributingOnlyGene = new Gene(gene.geneIdentifier());
        contributingOnlyGene.setCompatibleInheritanceModes(gene.compatibleInheritanceModes());
        gene.priorityResults().values().forEach(contributingOnlyGene::addPriorityResult);
        gene.variantEvaluations().stream()
                .filter(VariantEvaluation::contributesToGeneScore)
                .forEach(contributingOnlyGene::addVariant);
        gene.geneScores()
                .forEach(contributingOnlyGene::addGeneScore);
        return contributingOnlyGene;
    }

}
