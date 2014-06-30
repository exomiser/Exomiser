/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.common.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.util.ExomiserSettings;
import jannovar.exome.Variant;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final String VCF_FILE_FORMAT_4_1_LINE = "##fileformat=VCFv4.1\n";
    private static final String VCF_COLUMN_LINE = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tGENOTYPE\n";
    private static final String VCF_COLUMN_DELIMITER = "\t";
    private static final String NEWLINE = System.lineSeparator();

    @Override
    public void write(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList) {
        String outFileName = settings.getOutFileName();
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            logger.info("Writing {} file to: {}", settings.getOutputFormat(), outFileName);

            writer.write(VCF_FILE_FORMAT_4_1_LINE);
            //TODO: Add in the settings used and other data for Will and Orion here
            //(Issue #26 https://bitbucket.org/exomiser/exomiser/issue/26/vcf-output-format-requirements)
            writer.write(VCF_COLUMN_LINE);

            for (Gene gene : sampleData.getGeneList()) {
                writer.write(buildGeneVariantsString(gene));
            }
            writer.close();

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("Results written to file {}.", outFileName);
    }

    protected String buildGeneVariantsString(Gene gene) {
        StringBuilder sb = new StringBuilder();
        for (VariantEvaluation ve : gene.getVariantList()) {
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
            addColumnField(sb, "PASS");
            //INFO\t
            String infoField = String.format("GENE=%s;PHENO_SCORE=%s;VARIANT_SCORE=%s;COMBINED_SCORE=%s", gene.getGeneSymbol(), gene.getPriorityScore(), gene.getFilterScore(), gene.getCombinedScore());
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

}
