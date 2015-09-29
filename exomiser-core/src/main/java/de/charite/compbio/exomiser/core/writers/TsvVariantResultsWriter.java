package de.charite.compbio.exomiser.core.writers;

import com.google.common.base.Joiner;
import de.charite.compbio.exomiser.core.analysis.Analysis;

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


import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;

import de.charite.compbio.exomiser.core.model.pathogenicity.BasePathogenicityScore;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationLocation;
import htsjdk.variant.variantcontext.VariantContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import java.util.Locale;

import org.thymeleaf.util.StringUtils;

/**
 * @author Max Schubach <max.schubach@charite.de>
 */
public class TsvVariantResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvGeneResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_VARIANT;

    private final CSVFormat format = CSVFormat
            .newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#CHROM", "POS", "REF", "ALT", "QUAL", "FILTER", "GENOTYPE", "COVERAGE", "FUNCTIONAL_CLASS", "HGVS", "EXOMISER_GENE",
                    "CADD(>0.483)", "POLYPHEN(>0.956|>0.446)", "MUTATIONTASTER(>0.94)", "SIFT(<0.06)", "MNCDS",
                    "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
                    "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
                    "EXOMISER_VARIANT_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_GENE_COMBINED_SCORE");

    private final DecimalFormat formatter = new DecimalFormat(".##");

    public TsvVariantResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(Analysis analysis, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), format)) {
            writeData(analysis, settings.outputPassVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, OutputSettings settings) {
        StringBuilder output = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(output, format)) {
            writeData(analysis, settings.outputPassVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to string {}.", output, ex);
        }
        return output.toString();
    }

    private void writeData(Analysis analysis, boolean writeOnlyPassVariants, CSVPrinter printer) throws IOException {
        SampleData sampleData = analysis.getSampleData();
        if (writeOnlyPassVariants) {
            logger.info("Writing out only PASS variants");
            for (Gene gene : sampleData.getGenes()) {
                writeOnlyPassVariantsOfGene(gene, printer);
            }
        } else {
            for (Gene gene : sampleData.getGenes()) {
                writeAllVariantsOfGene(gene, printer);
            }
        }
    }

    private void writeOnlyPassVariantsOfGene(Gene gene, CSVPrinter printer) throws IOException {
        for (VariantEvaluation ve : gene.getPassedVariantEvaluations()) {
            List<Object> record = getRecordOfVariant(ve, gene);
            printer.printRecord(record);
        }
    }

    private void writeAllVariantsOfGene(Gene gene, CSVPrinter printer) throws IOException {
        for (VariantEvaluation ve : gene.getVariantEvaluations()) {
            List<Object> record = getRecordOfVariant(ve, gene);
            printer.printRecord(record);
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
        record.add(variantContext.getAlternateAllele(ve.getAltAlleleId()).getDisplayString());
        // QUAL
        record.add(formatter.format(ve.getPhredScore()));
        // FILTER
        record.add(makeFiltersField(ve));
        // GENOTYPE
        record.add(ve.getGenotypeAsString());
        // COVERAGE
        record.add(variantContext.getCommonInfo().getAttributeAsString("DP", "0"));
        // FUNCTIONAL_CLASS
        // FIXME: use new terms (use .toSequenceOntologyTerm() instead)!
        //record.add(ve.getVariantEffect().getLegacyTerm());
        record.add(ve.getVariantEffect().getSequenceOntologyTerm());
        // HGVS
        record.add(getRepresentativeAnnotation(ve.getAnnotations()));
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
        //MNCDS
        record.add(getPatScore(ve.getPathogenicityData().getNcdsScore()));
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
                EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN, EXAC_EAST_ASIAN, EXAC_FINNISH, EXAC_NON_FINNISH_EUROPEAN, EXAC_SOUTH_ASIAN, EXAC_OTHER};
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

    private Object getPatScore(BasePathogenicityScore score) {
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

    /**
     * @return An annotation for a single transcript, representing one of the
     * annotations with the most pathogenic annotation.
     */
    private String getRepresentativeAnnotation(List<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return "?";
        }

        Annotation anno = annotations.get(0);

        String exonIntron = null;
        AnnotationLocation annotationLocation = anno.getAnnoLoc();
        if (annotationLocation != null) {
            AnnotationLocation.RankType rankType = annotationLocation.getRankType();
            if (rankType == AnnotationLocation.RankType.EXON) {
                exonIntron = StringUtils.concat("exon", annotationLocation.getRank() + 1);
            } else if (rankType == AnnotationLocation.RankType.INTRON) {
                exonIntron = StringUtils.concat("intron", annotationLocation.getRank() + 1);
            }
        }

        final Joiner joiner = Joiner.on(":").skipNulls();
        return joiner.join(anno.getGeneSymbol(), anno.getTranscript().getAccession(), exonIntron, anno.getCDSNTChangeStr(),
                anno.getProteinChangeStr());
    }

}
