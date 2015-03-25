package de.charite.compbio.exomiser.core.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impossibl.postgres.utils.guava.Joiner;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import de.charite.compbio.exomiser.core.model.pathogenicity.AbstractPathogenicityScore;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.Locale;

/**
 *
 *
 * @author Max Schubach <max.schubach@charite.de>
 *
 */
public class TsvVariantResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvGeneResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_VARIANT;

    private final CSVFormat format = CSVFormat
            .newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\r\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#CHROM", "POS", "REF", "ALT", "QUAL", "FILTER", "GENOTYPE", "COVERAGE", "FUNCTIONAL_CLASS", "HGVS", "EXOMISER_GENE",
                    "CADD(>0.483)", "POLYPHEN(>0.956|>0.446)", "MUTATIONTASTER(>0.94)", "SIFT(<0.06)",
                    "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
                    "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
                    "EXOMISER_VARIANT_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_GENE_COMBINED_SCORE");
    private CSVPrinter printer;

    private final DecimalFormat formatter = new DecimalFormat(".##");

    public TsvVariantResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings) {
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        try {
            this.printer = new CSVPrinter(new BufferedWriter(new FileWriter(outFile.toFile())), format);
            write(sampleData, settings);
            this.printer.close();
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    private void write(SampleData sampleData, ExomiserSettings settings) throws IOException {
        for (Gene gene : sampleData.getGenes()) {
            writeVariantsOfGene(gene);
        }

    }

    private void writeVariantsOfGene(Gene gene) throws IOException {
        for (VariantEvaluation ve : gene.getVariantEvaluations()) {
            List<Object> record = getRecordOfVariant(ve, gene);
            this.printer.printRecord(record);
        }
    }

    private List<Object> getRecordOfVariant(VariantEvaluation ve, Gene gene) {
        List<Object> record = new ArrayList<>();
        VariantContext variantContext = ve.getVariantContext();
        // CHROM
        record.add(variantContext.getChr());
        // POS
        record.add(variantContext.getStart());
        // REF
        record.add(variantContext.getReference().getDisplayString());
        // ALT
        record.add(variantContext.getAlternateAllele(ve.getAltAlleleID()).getDisplayString());
        // QUAL
        record.add(formatter.format(ve.getPhredScore()));
        // FILTER
        record.add(makeFiltersField(ve));
        // GENOTYPE
        record.add(ve.getGenotypeAsString());
        // COVERAGE
        record.add(ve.getVariantContext().getCommonInfo().getAttributeAsString("DP", "0"));
        // FUNCTIONAL_CLASS
        // FIXME: use new terms (use .toSequenceOntologyTerm() instead)!
        record.add(ve.getVariantEffect().getLegacyTerm());
        // HGVS
        record.add(ve.getRepresentativeAnnotation());
		// FIXME jannovar has no function to use HGVS stuff alone
        // variantAnnotation like KIAA1751:uc001aim.1:exon18:c.T2287C:p.X763Q
        // String[] variantAnnotation =
        // var.getRepresentativeAnnotation().split(":");
        // TRANSCRIPT
        // record.add(getColumnOfArrayIfExists(variantAnnotation, ));
        // // EXON
        // record.add(getColumnOfArrayIfExists(variantAnnotation, 2));
        // // BASE_CHANGE
        // record.add(getColumnOfArrayIfExists(variantAnnotation, 3));
        // // AA_CHANGE
        // record.add(getColumnOfArrayIfExists(variantAnnotation, 4));
        // EXOMISER_GENE
        record.add(ve.getGeneSymbol());
        // CADD
        record.add(getPatScore(ve.getPathogenicityData().getCaddScore()));
        // POLYPHEN
        record.add(getPatScore(ve.getPathogenicityData().getPolyPhenScore()));
        // MUTATIONTASTER
        record.add(getPatScore(ve.getPathogenicityData().getMutationTasterScore()));
        // SIFT
        record.add(getPatScore(ve.getPathogenicityData().getSiftScore()));
        // "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
        // "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
        addFrequencyData(ve.getFrequencyData(), record);
        // EXOMISER_VARIANT_SCORE
        record.add(dotIfNull(ve.getVariantScore()));
        // EXOMISER_GENE_PHENO_SCORE
        record.add(dotIfNull(gene.getPriorityScore()));
        // EXOMISER_GENE_VARIANT_SCORE
        record.add(dotIfNull(gene.getFilterScore()));
        // EXOMISER_GENE_COMBINED_SCORE
        record.add(dotIfNull(gene.getCombinedScore()));
        return record;
    }

    private void addFrequencyData(FrequencyData frequencyData, List<Object> record) {
        if (frequencyData == null) {
            frequencyData = new FrequencyData(null, Collections.EMPTY_SET);
        }
        // DBSNP_ID
        record.add(dotIfNull(frequencyData.getRsId()));
        // MAX_FREQUENCY
        record.add(dotIfNull(frequencyData.getMaxFreq()));
        // Don't change the order of these - it's necessary for the data to end up in the correct column
        FrequencySource[] experimentalFrequencySources = {
        // "DBSNP_FREQUENCY", 
            THOUSAND_GENOMES, 
        // "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
            ESP_EUROPEAN_AMERICAN, ESP_AFRICAN_AMERICAN, 
        // "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
            EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN, EXAC_EAST_ASIAN, EXAC_FINISH, EXAC_NON_FINISH_EUROPEAN, EXAC_SOUTH_ASIAN, EXAC_OTHER};
        for (FrequencySource source : experimentalFrequencySources) {
            record.add(dotIfFrequencyNull(frequencyData.getFrequencyForSource(source)));
        }
    }

    private Object dotIfNull(Object o) {
        if (o == null) {
            return ".";
        } else {
            return o;
        }
    }

    private Object dotIfFrequencyNull(Frequency frequency) {
        if (frequency == null) {
            return ".";
        } else {
            return frequency.getFrequency();
        }
    }

    private Object getPatScore(AbstractPathogenicityScore score) {
        if (score == null) {
            return ".";
        } else {
            return score.getScore();
        }
    }

    protected String makeFiltersField(VariantEvaluation variantEvaluation) {
        switch (variantEvaluation.getFilterStatus()) {
            case FAILED:
                return formatFailedFilters(variantEvaluation.getFailedFilterTypes());
            case PASSED:
                return "PASS";
            case UNFILTERED:
                return ".";
            default:
                return ".";
        }

    }

    protected String formatFailedFilters(Set<FilterType> failedFilters) {
        StringBuilder stringBuilder = new StringBuilder();
        for (FilterType filterType : failedFilters) {
            stringBuilder.append(filterType.toString()).append(";");
        }
        // remove the final semi-colon
        int sbLength = stringBuilder.length();
        return stringBuilder.substring(0, sbLength - 1);
    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings) {
        StringBuilder output = new StringBuilder(Joiner.on(format.getDelimiter()).join(format.getHeader()));
        for (Gene gene : sampleData.getGenes()) {
            for (VariantEvaluation ve : gene.getVariantEvaluations()) {
                List<Object> record = getRecordOfVariant(ve, gene);
                output.append("\n");
                output.append(Joiner.on(format.getDelimiter()).join(record));
            }
        }
        return output.toString();
    }

}
