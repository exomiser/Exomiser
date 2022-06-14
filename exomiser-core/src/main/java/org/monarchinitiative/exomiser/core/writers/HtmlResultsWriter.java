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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisProtoConverter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.sample.SampleProtoConverter;
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
import java.text.DecimalFormat;
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
    public void writeFile(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings settings) {
        logger.debug("Writing HTML results");
        Sample sample = analysisResults.getSample();
        String outFileName = ResultsWriterUtils.makeOutputFilename(sample.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            Context context = buildContext(modeOfInheritance, analysisResults, settings);
            templateEngine.process("results", context, writer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFileName, ex);
        }
        logger.debug("{} ALL results written to file {}", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings settings) {
        logger.debug("Writing HTML results");
        Context context = buildContext(modeOfInheritance, analysisResults, settings);
        return templateEngine.process("results", context);
    }

    private Context buildContext(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        Context context = new Context();

        Analysis analysis = analysisResults.getAnalysis();
        Sample sample = analysisResults.getSample();

        String yamlString = toYamlJobString(sample, analysis, outputSettings);
        context.setVariable("settings", yamlString);

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
        if (!sample.getProbandSampleName().isEmpty()) {
            sampleName = sample.getProbandSampleName();
        }
        context.setVariable("sampleName", sampleName);
        context.setVariable("sampleNames", sampleNames);
        context.setVariable("variantTypeCounters", variantTypeCounters);

        context.setVariable("modeOfInheritance", modeOfInheritance);
        List<Gene> filteredGenes = outputSettings.filterPassedGenesForOutput(analysisResults.getGenes());
        context.setVariable("genes", filteredGenes);

        //this will change the links to the relevant resource.
        // For the time being we're going to maintain the original behaviour (UCSC)
        // Need to wire it up through the system or it might be easiest to autodetect this from the transcripts of passed variants.
        // One of UCSC, ENSEMBL or REFSEQ
        context.setVariable("transcriptDb", "ENSEMBL");
        context.setVariable("variantRankComparator", new VariantEvaluation.RankBasedComparator());
        context.setVariable("pValueFormatter", new DecimalFormat("0.0E0"));
        return context;
    }

    String toYamlJobString(Sample sample, Analysis analysis, OutputSettings outputSettings) {
        SampleProto.Sample protoSample = new SampleProtoConverter().toProto(sample);
        AnalysisProto.Analysis protoAnalysis = new AnalysisProtoConverter().toProto(analysis);
        OutputProto.OutputOptions protoOutputOptions = new OutputSettingsProtoConverter().toProto(outputSettings);

        JobProto.Job protoJob = JobProto.Job.newBuilder()
                .setSample(protoSample)
                .setAnalysis(protoAnalysis)
                .setOutputOptions(protoOutputOptions)
                .build();

        try {
            String jsonString = JsonFormat.printer().print(protoJob);
            JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
            return new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            logger.error("Unable to process JSON settings", e);
        }
        return "";
    }

}
