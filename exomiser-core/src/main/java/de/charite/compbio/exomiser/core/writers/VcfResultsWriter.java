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

package de.charite.compbio.exomiser.core.writers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.htsjdk.InfoFields;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.*;
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

    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.VCF;

    /**
     * Initialize the object, given the original {@link VCFFileReader} from the
     * input.
     *
     * @param vcfHeader original {@link VCFHeader} from the input, used for
     * generating the output header
     */
    public VcfResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(Analysis analysis, SampleData sampleData, OutputSettings settings) {
        // create a VariantContextWriter writing to the output file path
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(sampleData.getVcfHeader(),
                outFile.toString(),
                InfoFields.BOTH,
                getAdditionalHeaderLines())) {
            writeData(sampleData, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(Analysis analysis, SampleData sampleData, OutputSettings settings) {
        // create a VariantContextWriter writing to a buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(sampleData.getVcfHeader(),
                baos,
                InfoFields.BOTH,
                getAdditionalHeaderLines())) {
            writeData(sampleData, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to string buffer", OUTPUT_FORMAT);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private void writeData(SampleData sampleData, boolean writeOnlyPassVariants, VariantContextWriter writer) {
        writeUnannotatedVariants(sampleData, writer);
        // actually write the data and close writer again
        if (writeOnlyPassVariants) {
            logger.info("Writing out only PASS variants");
            writeOnlyPassSampleData(sampleData, writer);
        } else {
            writeAllSampleData(sampleData, writer);
        }
    }

    private void writeUnannotatedVariants(SampleData sampleData, VariantContextWriter writer) {
        List<VariantContext> updatedRecords = updateGeneVariantRecords(null, sampleData.getUnAnnotatedVariantEvaluations());
        updatedRecords.forEach(record-> writer.add(record));
    }

    private void writeOnlyPassSampleData(SampleData sampleData, VariantContextWriter writer) {
        for (Gene gene : sampleData.getGenes()) {
            List<VariantContext> updatedRecords = updateGeneVariantRecords(gene, gene.getPassedVariantEvaluations());
            updatedRecords.forEach(record-> writer.add(record));
        }
    }

    /**
     * Write the <code>sampleData</code> as VCF to <code>writer</code>.
     *
     * <code>writer</code> is already completely initialized, including all
     * headers, so data is written out directly for each
     * {@link VariantEvalution} in <code>sampleData</code>.
     *
     * @param sampleData data set to write out
     * @param writer writer to write to
     */
    private void writeAllSampleData(SampleData sampleData, VariantContextWriter writer) {
        for (Gene gene : sampleData.getGenes()) {
            logger.debug("updating variant records for gene {}", gene);
            List<VariantContext> updatedRecords = updateGeneVariantRecords(gene, gene.getVariantEvaluations());
            updatedRecords.forEach(record-> writer.add(record));
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
        //using ArrayListMultimap is important as the order of the values (alleles) must be preserved so that they match the order listed in the ALT fildl
        ArrayListMultimap<String, VariantEvaluation> geneVariants = ArrayListMultimap.create();
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            geneVariants.put(variantContextKeyValue(variantEvaluation.getVariantContext()), variantEvaluation);
        }
        return geneVariants;
    }

    /**
     * A {@link VariantContext} cannot be used directly as a key in a Map or put into a Set as it does not override equals or hashCode.
     * Also simply using toString isn't an option as the compatible variants returned from the
     * {@link de.charite.compbio.exomiser.core.analysis.util.InheritanceModeAnalyser#inheritanceCompatibilityChecker}
     * are different instances and have had their genotype strings changed. This method solves these problems.
     */
    private String variantContextKeyValue(VariantContext variantContext) {
        //using StringBuilder instead of String.format as the performance is better and we're going to be doing this for every variant in the VCF
        // chr10-123256215-T*-[G, A]
        // chr5-11-AC*-[AT]
        StringBuilder keyValueBuilder = new StringBuilder();
        keyValueBuilder.append(variantContext.getContig()).append('-');
        keyValueBuilder.append(variantContext.getStart()).append('-');
        keyValueBuilder.append(variantContext.getReference()).append('-');
        keyValueBuilder.append(variantContext.getAlternateAlleles());
        return keyValueBuilder.toString();
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
     * Update the INFO field of <code>builder</code> given the
     * {@link VariantEvaluation} and <code>gene</code>.
     */
    private void updateInfoField(VariantContextBuilder builder, List<VariantEvaluation> variantEvaluations, Gene gene) {
        if (!variantEvaluations.isEmpty() && gene != null) {
            builder.attribute("EXOMISER_GENE", gene.getGeneSymbol());
            builder.attribute("EXOMISER_GENE_COMBINED_SCORE", gene.getCombinedScore());
            builder.attribute("EXOMISER_GENE_PHENO_SCORE", gene.getPriorityScore());
            builder.attribute("EXOMISER_GENE_VARIANT_SCORE", gene.getFilterScore());
            builder.attribute("EXOMISER_VARIANT_SCORE", buildVariantScore(variantEvaluations)); //this needs a list of VariantEvaluations to concatenate the fields from in Allele order
//            builder.attribute("EXOMISER_VARIANT_EFFECT", ve.getVariantEffect());
        } else {
            builder.attribute("EXOMISER_WARNING", "VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS");
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

    /**
     * @return list of additional {@link VCFHeaderLine}s to write out,
     * explaining the Jannovar and Exomiser INFO and FILTER fields
     */
    private List<VCFHeaderLine> getAdditionalHeaderLines() {
        List<VCFHeaderLine> lines = new ArrayList<>();

        // add INFO descriptions
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE", 1, VCFHeaderLineType.String, "Exomiser gene"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_VARIANT_SCORE", 1, VCFHeaderLineType.Float, "Exomiser variant score"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_PHENO_SCORE", 1, VCFHeaderLineType.Float, "Exomiser gene phenotype score"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_VARIANT_SCORE", 1, VCFHeaderLineType.Float, "Exomiser gene variant score"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_COMBINED_SCORE", 1, VCFHeaderLineType.Float, "Exomiser gene combined"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_WARNING", 1, VCFHeaderLineType.String, "Exomiser gene"));

        // add FILTER descriptions
        for (FilterType ft : FilterType.values()) {
            lines.add(new VCFFilterHeaderLine(ft.name(), ft.toString()));
        }

        return lines;
    }

}
