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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterReport;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlResultsWriter.class);

    private final TemplateEngine templateEngine;

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.HTML;

    public HtmlResultsWriter() {
        Locale.setDefault(Locale.UK);
        this.templateEngine = ThymeleafConfig.coreTemplateEngine();
    }

    @Override
    public void writeFile(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        logger.info("Writing HTML results");
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            Context context = buildContext(modeOfInheritance, analysis, analysisResults, settings);
            templateEngine.process("results", context, writer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFileName, ex);
        }
        logger.info("{} ALL results written to file {}", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        logger.info("Writing HTML results");
        Context context = buildContext(modeOfInheritance, analysis, analysisResults, settings);
        return templateEngine.process("results", context);
    }

    private Context buildContext(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        Context context = new Context();
        //write the settings
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        //avoids issues where there are oddities in the analysisSteps - none of these properly de/serialise at present
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        StringBuilder jsonSettings = new StringBuilder();
        try {
            jsonSettings.append(mapper.writeValueAsString(analysis));
            jsonSettings.append(mapper.writeValueAsString(settings));
        } catch (JsonProcessingException ex) {
            logger.error("Unable to process JSON settings", ex);
        }
        context.setVariable("settings", jsonSettings.toString());

        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = analysisResults.getUnAnnotatedVariantEvaluations();
        context.setVariable("unAnalysedVarEvals", unAnalysedVarEvals);

        //write out the analysis reports section
        List<FilterReport> analysisStepReports = ResultsWriterUtils.makeFilterReports(analysis, analysisResults);
        context.setVariable("filterReports", analysisStepReports);
        //write out the variant type counters
        List<String> sampleNames = analysisResults.getSampleNames();
        List<VariantEffectCount> variantTypeCounters = ResultsWriterUtils.makeVariantEffectCounters(sampleNames, analysisResults
                .getVariantEvaluations());
        String sampleName = "Anonymous";
        if (!analysis.getProbandSampleName().isEmpty()) {
            sampleName = analysis.getProbandSampleName();
        }
        context.setVariable("sampleName", sampleName);
        context.setVariable("sampleNames", sampleNames);
        context.setVariable("variantTypeCounters", variantTypeCounters);

        context.setVariable("modeOfInheritance", modeOfInheritance);
        List<Gene> passedGenes = ResultsWriterUtils.getMaxPassedGenes(analysisResults.getGenes(), settings.getNumberOfGenesToShow());
        context.setVariable("genes", passedGenes);

        //this will change the links to the relevant resource.
        // For the time being we're going to maintain the original behaviour (UCSC)
        // Need to wire it up through the system or it might be easiest to autodetect this from the transcripts of passed variants.
        // One of UCSC, ENSEMBL or REFSEQ
        context.setVariable("transcriptDb", "ENSEMBL");
        context.setVariable("variantRankComparator", new VariantEvaluation.RankBasedComparator());
        return context;
    }

}
