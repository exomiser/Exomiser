/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.writers;

import com.google.common.base.Joiner;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

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
                    "CADD(>0.483)", "POLYPHEN(>0.956|>0.446)", "MUTATIONTASTER(>0.94)", "SIFT(<0.06)", "REMM",
                    "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
                    "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
                    "EXOMISER_VARIANT_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_GENE_COMBINED_SCORE", "CONTRIBUTING_VARIANT");

    private final DecimalFormat formatter = new DecimalFormat(".##");

    public TsvVariantResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), format)) {
            writeData(analysis, analysisResults, settings.outputPassVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        StringBuilder output = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(output, format)) {
            writeData(analysis, analysisResults, settings.outputPassVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to string {}.", output, ex);
        }
        return output.toString();
    }

    private void writeData(Analysis analysis, AnalysisResults analysisResults, boolean writeOnlyPassVariants, CSVPrinter printer) throws IOException {
        if (writeOnlyPassVariants) {
            logger.info("Writing out only PASS variants");
            for (Gene gene : analysisResults.getGenes()) {
                writeOnlyPassVariantsOfGene(gene, printer);
            }
        } else {
            for (Gene gene : analysisResults.getGenes()) {
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
        record.add(variantContext.getContig());
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
        record.add(ve.getGenotypeString());
        // COVERAGE
        record.add(variantContext.getCommonInfo().getAttributeAsString("DP", "0"));
        // FUNCTIONAL_CLASS
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
        record.add(getPatScore(ve.getPathogenicityData().getRemmScore()));
        // "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
        // "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
        addFrequencyData(ve.getFrequencyData(), record);
        // EXOMISER_VARIANT_SCORE
        record.add(dotIfNull(ve.getVariantScore()));
        // EXOMISER_GENE_PHENO_SCORE
        record.add(dotIfNull(gene.getPriorityScore()));
        // EXOMISER_GENE_VARIANT_SCORE
        record.add(dotIfNull(gene.getVariantScore()));
        // EXOMISER_GENE_COMBINED_SCORE
        record.add(dotIfNull(gene.getCombinedScore()));
        // EXOMISER_CONTRIBUTES_TO_SCORE
        record.add(ve.contributesToGeneScore() ? "CONTRIBUTING_VARIANT" : ".");
        return record;
    }

    private void addFrequencyData(FrequencyData frequencyData, List<Object> record) {
        // DBSNP_ID
        record.add(dotIfNull(frequencyData.getRsId()));
        // MAX_FREQUENCY
        record.add(dotIfNull(frequencyData.getMaxFreq()));
        // Don't change the order of these - it's necessary for the data to end up in the correct column
        FrequencySource[] experimentalFrequencySources = {
                // "DBSNP_FREQUENCY",
                FrequencySource.THOUSAND_GENOMES,
                // "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
                FrequencySource.ESP_EUROPEAN_AMERICAN, FrequencySource.ESP_AFRICAN_AMERICAN,
                // "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
                FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN, FrequencySource.EXAC_AMERICAN, FrequencySource.EXAC_EAST_ASIAN, FrequencySource.EXAC_FINNISH, FrequencySource.EXAC_NON_FINNISH_EUROPEAN, FrequencySource.EXAC_SOUTH_ASIAN, FrequencySource.EXAC_OTHER};
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

    private Object getPatScore(PathogenicityScore score) {
        if (score == null) {
            return ".";
        } else {
            return score.getScore();
        }
    }

    private String makeFiltersField(VariantEvaluation variantEvaluation) {
        switch (variantEvaluation.getFilterStatus()) {
            case FAILED:
                return formatFailedFilters(variantEvaluation.getFailedFilterTypes());
            case PASSED:
                return "PASS";
            case UNFILTERED:
            default:
                return ".";
        }

    }

    private String formatFailedFilters(Set<FilterType> failedFilters) {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (FilterType filterType : failedFilters) {
            stringJoiner.add(filterType.toVcfValue());
        }
        return stringJoiner.toString();
    }

    /**
     * @return An annotation for a single transcript, representing one of the
     * annotations with the most pathogenic annotation.
     */
    private String getRepresentativeAnnotation(List<TranscriptAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return "?";
        }

        TranscriptAnnotation anno = annotations.get(0);

        final Joiner joiner = Joiner.on(":").skipNulls();
        return joiner.join(anno.getGeneSymbol(),
                anno.getAccession(),
                anno.getHgvsCdna(),
                anno.getHgvsProtein());
    }

}
