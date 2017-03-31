/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlResultsWriter.class);

    private final TemplateEngine templateEngine;
    
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.HTML;
    
    public HtmlResultsWriter(TemplateEngine templateEngine) {
        Locale.setDefault(Locale.UK);
        this.templateEngine = templateEngine;
    }

    @Override
    public void writeFile(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {

        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            writer.write(writeString(analysis, analysisResults, settings));

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        Context context = new Context();
        //write the settings
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        //avoids issues where there are oddities in the analysisSteps - none of these properly de/serialise at present
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        String jsonSettings = "";
        try {
            jsonSettings = mapper.writeValueAsString(analysis);
            jsonSettings += mapper.writeValueAsString(settings);
        } catch (JsonProcessingException ex) {
            logger.error("Unable to process JSON settings", ex);
        }
        context.setVariable("settings", jsonSettings);
        
        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = analysisResults.getUnAnnotatedVariantEvaluations();
        context.setVariable("unAnalysedVarEvals", unAnalysedVarEvals);
        
        //write out the analysis reports section
        List<FilterReport> analysisStepReports = makeAnalysisStepReports(analysis, analysisResults);
        context.setVariable("filterReports", analysisStepReports);
        //write out the variant type counters
        List<VariantEffectCount> variantTypeCounters = makeVariantEffectCounters(analysisResults.getVariantEvaluations());
        List<String> sampleNames= analysisResults.getSampleNames();
        String sampleName = "Anonymous";
        if(!analysisResults.getProbandSampleName().isEmpty()) {
            sampleName = analysisResults.getProbandSampleName();
        }
        context.setVariable("sampleName", sampleName);
        context.setVariable("sampleNames", sampleNames);
        context.setVariable("variantTypeCounters", variantTypeCounters);
                 
        List<Gene> passedGenes = ResultsWriterUtils.getMaxPassedGenes(analysisResults.getGenes(), settings.getNumberOfGenesToShow());
        context.setVariable("genes", passedGenes);

        //this will change the links to the relevant resource.
        // For the time being we're going to maintain the original behaviour (UCSC)
        // Need to wire it up through the system or it might be easiest to autodetect this from the transcripts of passed variants.
        // One of UCSC, ENSEMBL or REFSEQ
        context.setVariable("transcriptDb", "UCSC");
        context.setVariable("variantRankComparator", new VariantEvaluation.RankBasedComparator());
        return templateEngine.process("results", context);
    }

    protected List<VariantEffectCount> makeVariantEffectCounters(List<VariantEvaluation> variantEvaluations) {
        return ResultsWriterUtils.makeVariantEffectCounters(variantEvaluations);
    }
    
    protected List<FilterReport> makeAnalysisStepReports(Analysis analysis, AnalysisResults analysisResults) {
        return ResultsWriterUtils.makeFilterReports(analysis, analysisResults);
    }

    //TODO:
//    /**
//     * This function writes out a table representing the PED file of the family
//     * being analysed (if a multisample VCF file is being analysed) or the name
//     * of the sample (for a single-sample VCF file).
//     * <P>
//     * For multisample VCF files, a color code is used to mark the following
//     * kinds of samples (individuals):
//     * <ul>
//     * <li>Unaffected parent: white</li>
//     * <li>Affected (whether parent or not): dark grey</li>
//     * <li>Unaffected sibling: light blue</li>
//     * </ul>
//     * The same color code will be used for showing the genotypes of the
//     * individual variants, which hopefully will help in their interpretation.
//     *
//     * @param out An open file handle (can come from the command line or server
//     * versions of Exomiser).
//     */
//    public void writePedigreeTable(Writer out) throws IOException {
//        int n = this.pedigree.getNumberOfIndividualsInPedigree();
//        if (n == 1) {
//            String sampleName = this.pedigree.getSingleSampleName();
//            out.write("<table class=\"pedigree\">\n");
//            out.write(String.format("<tr><td>Sample name: %s</td></tr>\n", sampleName));
//            out.write("</table>\n");
//        } else { /* multiple samples */
//
//            out.write("<h2>Analyzed samples</h2>\n");
//            out.write("<p>affected: red, parent of affected: light blue, unaffected: white</p>\n");
//            out.write("<table class=\"pedigree\">\n");
//            for (int i = 0; i < n; ++i) {
//                List<String> lst = this.pedigree.getPEDFileDatForNthPerson(i);
//                String fam = lst.get(0);
//                String id = lst.get(1);
//                String fathID = lst.get(2);
//                String mothID = lst.get(3);
//                String sex = lst.get(4);
//                String disease = lst.get(5);
//                out.write("<tr><td>" + fam + "</td>");
//                if (this.pedigree.isNthPersonAffected(i)) {
//                    out.write("<td id=\"g\">" + id + "</td>");
//                } else if (this.pedigree.isNthPersonParentOfAffected(i)) {
//                    out.write("<td id=\"b\">" + id + "</td>");
//                } else {
//                    out.write("<td id=\"w\">" + id + "</td>");
//                }
//                out.write("<td>" + fathID + "</td><td>" + mothID + "</td><td>"
//                        + sex + "</td><td>" + disease + "</td></tr>\n");
//            }
//            out.write("</table>\n");
//            out.write("<br/>\n");
//        }
//    }
    
}
