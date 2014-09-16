/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.filter.FilterType;
import jannovar.exome.Variant;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.VCF;

    private static final String VCF_FILE_FORMAT_4_1_LINE = "##fileformat=VCFv4.1\n";
    private static final String VCF_COLUMN_LINE = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tGENOTYPE\n";
    private static final String VCF_COLUMN_DELIMITER = "\t";
    private static final String NEWLINE = System.lineSeparator();

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            writer.write(writeString(sampleData, settings, priorityList));
            writer.close();

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(VCF_FILE_FORMAT_4_1_LINE);
        //TODO: Add in the settings used and other data for Will and Orion here
        //(Issue #26 https://bitbucket.org/exomiser/exomiser/issue/26/vcf-output-format-requirements)
        stringBuilder.append(VCF_COLUMN_LINE);
        //write in the results for each variant in each gene
        for (Gene gene : sampleData.getGenes()) {
            stringBuilder.append(buildGeneVariantsString(gene));
        }
        return stringBuilder.toString();
    }

    protected String buildGeneVariantsString(Gene gene) {
        StringBuilder sb = new StringBuilder();
        for (VariantEvaluation ve : gene.getVariantEvaluations()) {
            Variant v = ve.getVariant();
            //CHROM\t
            addColumnField(sb, v.get_chromosome_as_string());
            //POS\t
            addColumnField(sb, String.valueOf(v.get_position()));
            //ID\t
            addColumnField(sb, ".");
            //REF\t
            addColumnField(sb, v.get_ref());
            //ALT\t
            addColumnField(sb, v.get_alt());
            //QUAL\t
            addColumnField(sb, String.valueOf(v.getVariantPhredScore()));
            //FILTER\t
            if (ve.passesFilters()) {
                addColumnField(sb, "PASS");            
            } else {
                addColumnField(sb, formatFailedFilters(ve.getFailedFilters()));
            }
            //INFO\t
            String infoField = String.format("%s;EXOMISER_GENE=%s;EXOMISER_VARIANT_SCORE=%s;EXOMISER_GENE_PHENO_SCORE=%s;EXOMISER_GENE_VARIANT_SCORE=%s;EXOMISER_GENE_COMBINED_SCORE=%s", ve.getVariant().get_info(), gene.getGeneSymbol(), ve.getFilterScore(), gene.getPriorityScore(), gene.getFilterScore(), gene.getCombinedScore());
            addColumnField(sb, infoField);
            //FORMAT\t
            addColumnField(sb, "GT");
            //GENOTYPE\n
            sb.append(v.getGenotypeAsString()).append("\n");
        }
        return sb.toString();
    }

    protected void addColumnField(StringBuilder sb, String value) {
        sb.append(value).append(VCF_COLUMN_DELIMITER);
    }

    protected String formatFailedFilters(Set<FilterType> failedFilters) {
        StringBuilder stringBuilder = new StringBuilder();
        for (FilterType filterType : failedFilters) {
            stringBuilder.append(filterType.toString()).append(";");
        }
        //remove the final semi-colon
        int sbLength = stringBuilder.length();
        return stringBuilder.substring(0, sbLength - 1);
    }

}
