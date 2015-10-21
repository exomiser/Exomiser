package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.jannovar.htsjdk.InfoFields;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void writeFile(Analysis analysis, OutputSettings settings) {
        // create a VariantContextWriter writing to the output file path
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        SampleData sampleData = analysis.getSampleData();
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(sampleData.getVcfHeader(),
                outFile.toString(), InfoFields.BOTH, getAdditionalHeaderLines())) {
            writeData(sampleData, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(Analysis analysis, OutputSettings settings) {
        // create a VariantContextWriter writing to a buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleData sampleData = analysis.getSampleData();
        try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(sampleData.getVcfHeader(), baos,
                InfoFields.BOTH, getAdditionalHeaderLines())) {
            writeData(sampleData, settings.outputPassVariantsOnly(), writer);
        }
        logger.info("{} results written to string buffer", OUTPUT_FORMAT);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private void writeData(SampleData sampleData, boolean writeOnlyPassVariants, VariantContextWriter writer) {
        // actually write the data and close writer again
        if (writeOnlyPassVariants) {
            logger.info("Writing out only PASS variants");
            writeOnlyPassSampleData(sampleData, writer);
        } else {
            writeAllSampleData(sampleData, writer);
        }
    }

    private void writeOnlyPassSampleData(SampleData sampleData, VariantContextWriter writer) {
        writeUnannotatedVariants(sampleData, writer);
        for (Gene gene : sampleData.getGenes()) {
            for (VariantEvaluation variant : gene.getPassedVariantEvaluations()) {
                writeRecord(variant, writer, gene);
            }
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
        writeUnannotatedVariants(sampleData, writer);
        for (Gene gene : sampleData.getGenes()) {
            for (VariantEvaluation variant : gene.getVariantEvaluations()) {
                writeRecord(variant, writer, gene);
            }
        }
    }

    private void writeUnannotatedVariants(SampleData sampleData, VariantContextWriter writer) {
        for (VariantEvaluation variant : sampleData.getUnAnnotatedVariantEvaluations()) {
            writeRecord(variant, writer, null);
        }
    }

    /**
     * Write out <code>ve</code> as one record into <code>write</code>.
     *
     * @param ve record to write out
     * @param writer writer to write to
     * @param gene the {@link Gene} to use when writing out, <code>null</code>
     * for unannotated variants.
     */
    private void writeRecord(VariantEvaluation ve, VariantContextWriter writer, Gene gene) {
        // create a new VariantContextBuilder, based on the original line
        // n.b. variantContexts with alternative alleles will be shared between 
        // the alternative allele variant objects - Exomiser works on a 1 Variant = 1 Allele principle
        VariantContext variantContext = ve.getVariantContext();
        VariantContextBuilder builder = new VariantContextBuilder(variantContext);
//        Allele refAllele = variantContext.getReference();
//        logger.info("Genotypes: {}", variantContext.getGenotypes());
//        for (int i = 0; i < variantContext.getAlternateAlleles().size(); i++) {
//            Allele altAllele = variantContext.getAlternateAllele(i);
//            Genotype genotype = variantContext.getGenotype(0);
//            logger.info("{} {} {} {}", refAllele, altAllele, genotype.getType(), genotype.getAlleles());
//        }
        // update filter and info fields and write out to writer.
        updateFilterField(builder, ve);
        updateInfoField(builder, ve, gene);
        writer.add(builder.make());
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
        Set<String> set = new HashSet<>();
        for (FilterType ft : failedFilterTypes) {
            set.add(ft.toString());
        }
        builder.filters(set);
    }

    /**
     * Update the INFO field of <code>builder</code> given the
     * {@link VariantEvaluation} and <code>gene</code>.
     */
    private void updateInfoField(VariantContextBuilder builder, VariantEvaluation ve, Gene gene) {
        if (ve.hasAnnotations() && gene != null) {
            builder.attribute("EXOMISER_GENE", gene.getGeneSymbol());
            builder.attribute("EXOMISER_VARIANT_SCORE", ve.getVariantScore());
            builder.attribute("EXOMISER_GENE_PHENO_SCORE", gene.getPriorityScore());
            builder.attribute("EXOMISER_GENE_VARIANT_SCORE", gene.getFilterScore());
            builder.attribute("EXOMISER_GENE_COMBINED_SCORE", gene.getCombinedScore());
        } else {
            builder.attribute("EXOMISER_WARNING", "VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS");
        }
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
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_PHENO_SCORE", 1, VCFHeaderLineType.Float,
                "Exomiser gene phenotype score"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_VARIANT_SCORE", 1, VCFHeaderLineType.Float,
                "Exomiser gene variant score"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_GENE_COMBINED_SCORE", 1, VCFHeaderLineType.Float,
                "Exomiser gene combined"));
        lines.add(new VCFInfoHeaderLine("EXOMISER_WARNING", 1, VCFHeaderLineType.String, "Exomiser gene"));

        // add FILTER descriptions
        for (FilterType ft : FilterType.values()) {
            lines.add(new VCFFilterHeaderLine(ft.name(), ft.toString()));
        }

        return lines;
    }

}
