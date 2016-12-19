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

package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.*;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnalyser;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

// TODO(holtgrew): Write out to sorting VariantContextWriter?
/**
 * Generate results in VCF format using HTS-JDK.
 *
 * @see <a href="http://samtools.github.io/hts-specs/VCFv4.1.pdf">VCF
 * Standard</a>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class VcfResultsWriter implements ResultsWriter {

    private enum ExomiserVcfInfoField {

        GENE_SYMBOL("ExGeneSymbol", VCFHeaderLineType.String, "Exomiser gene symbol"),
        GENE_ID("ExGeneSymbId", VCFHeaderLineType.String, "Exomiser gene id"),
        GENE_COMBINED_SCORE("ExGeneSCombi", VCFHeaderLineType.Float, "Exomiser gene combined score"),
        GENE_PHENO_SCORE("ExGeneSPheno", VCFHeaderLineType.Float, "Exomiser gene phenotype score"),
        GENE_VARIANT_SCORE("ExGeneSVar", VCFHeaderLineType.Float, "Exomiser gene variant score"),
        VARIANT_SCORE("ExVarScore", VCFHeaderLineType.Float, "Exomiser variant score"),
        VARIANT_EFFECT("ExVarEff", VCFHeaderLineType.String, "Exomiser variant effect"),
        VARIANT_HGVS("ExVarHgvs", VCFHeaderLineType.String, "Exomiser variant hgvs"),
        WARNING("ExWarn", VCFHeaderLineType.String, "Exomiser warning");

        private final String id;
        private final VCFHeaderLineType vcfHeaderLineType;
        private final String description;

        ExomiserVcfInfoField(String id, VCFHeaderLineType vcfHeaderLineType, String description) {
            this.id = id;
            this.vcfHeaderLineType = vcfHeaderLineType;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public VCFHeaderLineType getVcfHeaderLineType() {
            return vcfHeaderLineType;
        }

        public String getDescription() {
            return description;
        }

        VCFHeaderLine getVcfHeaderLine() {
            return new VCFInfoHeaderLine(id, VCFHeaderLineCount.A, vcfHeaderLineType, description);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.VCF;

    /**
     * Initialize the object, given the original {@link VCFFileReader} from the
     * input.
     */
    public VcfResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        // create a VariantContextWriter writing to the output file path
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(analysisResults.getVcfHeader(),
                outFile.toString(),
                getAdditionalHeaderLines(),
                false)) {
            writeData(analysisResults, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        // create a VariantContextWriter writing to a buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(analysisResults.getVcfHeader(),
                baos,
                getAdditionalHeaderLines())) {
            writeData(analysisResults, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to string buffer", OUTPUT_FORMAT);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private void writeData(AnalysisResults analysisResults, boolean writeOnlyPassVariants, VariantContextWriter writer) {
        writeUnannotatedVariants(analysisResults, writer);
        // actually write the data and close writer again
        if (writeOnlyPassVariants) {
            logger.info("Writing out only PASS variants");
            writeOnlyPassSampleData(analysisResults, writer);
        } else {
            writeAllSampleData(analysisResults, writer);
        }
    }

    private void writeUnannotatedVariants(AnalysisResults analysisResults, VariantContextWriter writer) {
        List<VariantContext> updatedRecords = updateGeneVariantRecords(null, analysisResults.getUnAnnotatedVariantEvaluations());
        updatedRecords.forEach(writer::add);
    }

    private void writeOnlyPassSampleData(AnalysisResults analysisResults, VariantContextWriter writer) {
        for (Gene gene : analysisResults.getGenes()) {
            List<VariantContext> updatedRecords = updateGeneVariantRecords(gene, gene.getPassedVariantEvaluations());
            updatedRecords.forEach(writer::add);
        }
    }

    /**
     * Write the <code>analysisResults</code> as VCF to <code>writer</code>.
     *
     * <code>writer</code> is already completely initialized, including all
     * headers, so data is written out directly for each
     * {@link VariantEvaluation} in <code>analysisResults</code>.
     *
     * @param analysisResults data set to write out
     * @param writer writer to write to
     */
    private void writeAllSampleData(AnalysisResults analysisResults, VariantContextWriter writer) {
        for (Gene gene : analysisResults.getGenes()) {
            logger.debug("updating variant records for gene {}", gene);
            List<VariantContext> updatedRecords = updateGeneVariantRecords(gene, gene.getVariantEvaluations());
            updatedRecords.forEach(writer::add);
        }
    }

    //this needs a MultiMap<VariantContext, VariantEvaluation> (see InheritanceModeAnalyser for this)
    private List<VariantContext> updateGeneVariantRecords(Gene gene, List<VariantEvaluation> variants) {
        if (variants.isEmpty()) {
            return Collections.emptyList();
        }
//        maybe check if the variant is multi-allelic first?
        Multimap<String, VariantEvaluation> variantContextToEvaluations = mapVariantEvaluationsToVariantContextString(variants);
        return variantContextToEvaluations.asMap()
                .values()
                .stream()
                .map(variantEvaluations -> updateRecord(Lists.newArrayList(variantEvaluations), gene))
                .collect(toList());
    }

    private Multimap<String, VariantEvaluation> mapVariantEvaluationsToVariantContextString(List<VariantEvaluation> variantEvaluations) {
        //using ArrayListMultimap is important as the order of the values (alleles) must be preserved so that they match the order listed in the ALT field
        ArrayListMultimap<String, VariantEvaluation> geneVariants = ArrayListMultimap.create();
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            geneVariants.put(variantContextKeyValue(variantEvaluation.getVariantContext()), variantEvaluation);
        }
        return geneVariants;
    }

    /**
     * A {@link VariantContext} cannot be used directly as a key in a Map or put into a Set as it does not override equals or hashCode.
     * Also simply using toString isn't an option as the compatible variants returned from the
     * {@link InheritanceModeAnalyser#checkInheritanceCompatibilityOfPassedVariants(Gene)}
     * are different instances and have had their genotype strings changed. This method solves these problems.
     */
    private String variantContextKeyValue(VariantContext variantContext) {
        //using StringBuilder instead of String.format as the performance is better and we're going to be doing this for every variant in the VCF
        // chr10-123256215-T*-[G, A]
        // chr5-11-AC*-[AT]
        return variantContext.getContig() + '-' +
                variantContext.getStart() + '-' +
                variantContext.getReference() + '-' +
                variantContext.getAlternateAlleles();
    }

    private VariantContext updateRecord(List<VariantEvaluation> variantEvaluations, Gene gene) {
        // create a new VariantContextBuilder, based on the original line
        // n.b. variantContexts with alternative alleles will be shared between
        // the alternative allele variant objects - Exomiser works on a 1 Variant = 1 Allele principle
        VariantEvaluation variantEvaluation = variantEvaluations.get(0);

        VariantContext variantContext = variantEvaluation.getVariantContext();
        VariantContextBuilder builder = new VariantContextBuilder(variantContext);
        // update filter and info fields and write out to writer.
        updateFilterField(builder, variantEvaluation);
        updateInfoField(builder, variantEvaluations, gene);
        return builder.make();
    }

    /**
     * Update the FILTER field of <code>builder</code> given the
     * {@link VariantEvaluation}.
     */
    private void updateFilterField(VariantContextBuilder builder, VariantEvaluation ve) {
        switch (ve.getFilterStatus()) {
            case FAILED:
                updateFailedFilters(builder, ve.getFailedFilterTypes());
                break;
            case PASSED:
                builder.filter("PASS");
                break;
            case UNFILTERED:
            default:
                builder.filter(".");
                break;
        }
    }

    /**
     * Write all failed filter types from <code>failedFilterTypes</code> into
     * <code>builder</code>.
     */
    private void updateFailedFilters(VariantContextBuilder builder, Set<FilterType> failedFilterTypes) {
        Set<String> failedFilters = failedFilterTypes.stream().map(FilterType::toString).collect(toSet());
        builder.filters(failedFilters);
    }

    /**
     * @return list of additional {@link VCFHeaderLine}s to write out,
     * explaining the Jannovar and Exomiser INFO and FILTER fields
     */
    private List<VCFHeaderLine> getAdditionalHeaderLines() {
        List<VCFHeaderLine> lines = new ArrayList<>();

        // add INFO descriptions
        for (ExomiserVcfInfoField infoField : ExomiserVcfInfoField.values()) {
            lines.add(infoField.getVcfHeaderLine());
        }

        // add FILTER descriptions
        for (FilterType ft : FilterType.values()) {
            lines.add(new VCFFilterHeaderLine(ft.name(), ft.toString()));
        }

        return lines;
    }
    
    /**
     * Update the INFO field of <code>builder</code> given the
     * {@link VariantEvaluation} and <code>gene</code>.
     */
    private void updateInfoField(VariantContextBuilder builder, List<VariantEvaluation> variantEvaluations, Gene gene) {
        if (!variantEvaluations.isEmpty() && gene != null) {
            builder.attribute(ExomiserVcfInfoField.GENE_SYMBOL.getId(), gene.getGeneSymbol());
            builder.attribute(ExomiserVcfInfoField.GENE_ID.getId(), gene.getGeneId());
            builder.attribute(ExomiserVcfInfoField.GENE_COMBINED_SCORE.getId(), gene.getCombinedScore());
            builder.attribute(ExomiserVcfInfoField.GENE_PHENO_SCORE.getId(), gene.getPriorityScore());
            builder.attribute(ExomiserVcfInfoField.GENE_VARIANT_SCORE.getId(), gene.getVariantScore());
            builder.attribute(ExomiserVcfInfoField.VARIANT_SCORE.getId(), buildVariantScore(variantEvaluations)); //this needs a list of VariantEvaluations to concatenate the fields from in Allele order
            builder.attribute(ExomiserVcfInfoField.VARIANT_EFFECT.getId(), buildVariantEffects(variantEvaluations));
            builder.attribute(ExomiserVcfInfoField.VARIANT_HGVS.getId(), buildHgvs(variantEvaluations));
        } else {
            builder.attribute(ExomiserVcfInfoField.WARNING.getId(), "VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS");
        }
    }

    private String buildVariantScore(List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.size() == 1) {
            return String.valueOf(variantEvaluations.get(0).getVariantScore());
        }
        StringBuilder variantScoreBuilder = new StringBuilder();
        variantScoreBuilder.append(variantEvaluations.get(0).getVariantScore());
        for (int i = 1; i < variantEvaluations.size(); i++) {
            variantScoreBuilder.append(',').append(variantEvaluations.get(i).getVariantScore());
        }
        return variantScoreBuilder.toString();
    }

    private String buildVariantEffects(List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.size() == 1) {
            return String.valueOf(variantEvaluations.get(0).getVariantEffect());
        }
        StringBuilder variantEfectBuilder = new StringBuilder();
        variantEfectBuilder.append(variantEvaluations.get(0).getVariantEffect());
        for (int i = 1; i < variantEvaluations.size(); i++) {
            variantEfectBuilder.append(',').append(variantEvaluations.get(i).getVariantEffect());
        }
        return variantEfectBuilder.toString();
    }

    private String buildHgvs(List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.size() == 1) {
            return String.valueOf(variantEvaluations.get(0).getHgvsGenome());
        }
        StringBuilder variantHgvsBuilder = new StringBuilder();
        variantHgvsBuilder.append(variantEvaluations.get(0).getHgvsGenome());
        for (int i = 1; i < variantEvaluations.size(); i++) {
            variantHgvsBuilder.append(',').append(variantEvaluations.get(i).getHgvsGenome());
        }
        return variantHgvsBuilder.toString();
    }

}
