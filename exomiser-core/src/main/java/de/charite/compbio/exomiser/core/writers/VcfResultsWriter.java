/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.filters.FilterType;
import jannovar.exome.Variant;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @see http://samtools.github.io/hts-specs/VCFv4.1.pdf
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.VCF;

    private static final String VCF_FILE_FORMAT_4_1_LINE = "##fileformat=VCFv4.1\n";
    private static final String VCF_COLUMN_LINE = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tGENOTYPE\n";
    private static final String VCF_COLUMN_DELIMITER = "\t";
    private static final String NEWLINE = System.lineSeparator();

    public VcfResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings) {
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            writer.write(writeString(sampleData, settings));
            writer.close();

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings) {
        StringBuilder stringBuilder = new StringBuilder();
        addHeader(stringBuilder);
        addUnAnnotatedVariants(sampleData, stringBuilder);
        addVariantsByGene(sampleData, stringBuilder);
        return stringBuilder.toString();
    }

    private void addHeader(StringBuilder stringBuilder) {
        stringBuilder.append(VCF_FILE_FORMAT_4_1_LINE);
        stringBuilder.append(VCF_COLUMN_LINE);
    }

    private void addUnAnnotatedVariants(SampleData sampleData, StringBuilder stringBuilder) {
        //report any variants without Jannovar annotations first to alert users of the problem
        for (VariantEvaluation variantEval : sampleData.getUnAnnotatedVariantEvaluations()) {
            buildVariantLine(variantEval, stringBuilder, null);
        }
    }

    private void addVariantsByGene(SampleData sampleData, StringBuilder stringBuilder) {
        //write in the results for each variant in each gene
        for (Gene gene : sampleData.getGenes()) {
            stringBuilder.append(buildGeneVariantsString(gene));
        }
    }

    protected String buildGeneVariantsString(Gene gene) {
        StringBuilder stringBuilder = new StringBuilder();
        for (VariantEvaluation variantEval : gene.getVariantEvaluations()) {
            buildVariantLine(variantEval, stringBuilder, gene);
        }
        return stringBuilder.toString();
    }

    private void buildVariantLine(VariantEvaluation varEval, StringBuilder sb, Gene gene) {
        Variant variant = varEval.getVariant();
        //CHROM\t
        addColumnField(sb, variant.get_chromosome_as_string());
        //POS\t
        addColumnField(sb, String.valueOf(variant.get_position()));
        //ID\t
        addColumnField(sb, ".");
        //REF\t
        addColumnField(sb, variant.get_ref());
        //ALT\t
        addColumnField(sb, variant.get_alt());
        //QUAL\t
        addColumnField(sb, String.valueOf(variant.getVariantPhredScore()));
        //FILTER\t
        addColumnField(sb, makeFiltersField(varEval));
        //INFO\t
        addColumnField(sb, makeInfoField(varEval, gene));
        //FORMAT\t
        addColumnField(sb, "GT");
        //GENOTYPE\n
        sb.append(variant.getGenotypeAsString()).append("\n");
    }

    protected void addColumnField(StringBuilder sb, String value) {
        sb.append(value).append(VCF_COLUMN_DELIMITER);
    }

    protected String makeFiltersField(VariantEvaluation variantEvaluation) {
        switch(variantEvaluation.getFilterStatus()) {
            case FAILED:
                return makeFailedFilters(variantEvaluation.getFailedFilterTypes());
            case PASSED:
                return "PASS";
            case UNFILTERED:
                return "";
            default:
                return "";
        }
    }

    protected String makeFailedFilters(Set<FilterType> failedFilterTypes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (FilterType filterType : failedFilterTypes) {
            stringBuilder.append(filterType.toString()).append(";");
        }
        removeTrailingSemiColon(stringBuilder);
        return stringBuilder.toString();
    }

    private void removeTrailingSemiColon(StringBuilder stringBuilder) {
        if (stringBuilder.charAt(stringBuilder.length() - 1) == ';') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
    }

    //TODO: Add in the settings used and other data for Will and Orion here
    //(Issue #26 https://bitbucket.org/exomiser/exomiser/issue/26/vcf-output-format-requirements)
    private String makeInfoField(VariantEvaluation ve, Gene gene) {
        String existingVcfInfoField = ve.getVariant().get_info();
        if (ve.hasAnnotations() && gene != null) {
            return String.format("%s;EXOMISER_GENE=%s;EXOMISER_VARIANT_SCORE=%s;EXOMISER_GENE_PHENO_SCORE=%s;EXOMISER_GENE_VARIANT_SCORE=%s;EXOMISER_GENE_COMBINED_SCORE=%s", existingVcfInfoField, gene.getGeneSymbol(), ve.getVariantScore(), gene.getPriorityScore(), gene.getFilterScore(), gene.getCombinedScore());
        }
        return String.format("%s;VARIANT NOT ANALYSED - NO GENE ANNOTATIONS", existingVcfInfoField);
    }
}
