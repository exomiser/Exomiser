/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
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

    private static final Logger logger = LoggerFactory.getLogger(TsvVariantResultsWriter.class);

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
    public void writeFile(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), format)) {
            writeData(modeOfInheritance, analysis, analysisResults, settings.outputContributingVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFileName, ex);
        }
        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, modeOfInheritance.getAbbreviation(), outFileName);
    }

    @Override
    public String writeString(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        StringBuilder output = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(output, format)) {
            writeData(modeOfInheritance, analysis, analysisResults, settings.outputContributingVariantsOnly(), printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to string {}", output, ex);
        }
        return output.toString();
    }

    private void writeData(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults,
                           boolean writeOnlyContributingVariants, CSVPrinter printer) throws IOException {
        if (writeOnlyContributingVariants) {
            logger.debug("Writing out only CONTRIBUTING variants");
            for (Gene gene : analysisResults.getGenes()) {
                if (gene.passedFilters() && gene.isCompatibleWith(modeOfInheritance)) {
                    writeOnlyContributingVariantsOfGene(modeOfInheritance, gene, printer);
                }
            }
        } else {
            for (Gene gene : analysisResults.getGenes()) {
                writeAllVariantsOfGene(modeOfInheritance, gene, printer);
            }
        }
    }

    private void writeOnlyContributingVariantsOfGene(ModeOfInheritance modeOfInheritance, Gene gene, CSVPrinter printer) throws IOException {
        GeneScore geneScore = gene.getGeneScoreForMode(modeOfInheritance);
        for (VariantEvaluation ve : geneScore.getContributingVariants()) {
            List<Object> record = buildVariantRecord(modeOfInheritance, ve, gene);
            printer.printRecord(record);
        }
    }

    private void writeAllVariantsOfGene(ModeOfInheritance modeOfInheritance, Gene gene, CSVPrinter printer) throws IOException {
        for (VariantEvaluation ve : gene.getVariantEvaluations()) {
            //don't check that the variant is compatible under a particular mode of inheritance as otherwise a failing variant won't appear in the output.
            List<Object> record = buildVariantRecord(modeOfInheritance, ve, gene);
            printer.printRecord(record);
        }
    }

    private List<Object> buildVariantRecord(ModeOfInheritance modeOfInheritance, VariantEvaluation ve, Gene gene) {
        List<Object> record = new ArrayList<>();
        VariantContext variantContext = ve.getVariantContext();
        // CHROM
        record.add(ve.getChromosomeName());
        // POS
        record.add(ve.getPosition());
        // REF
        record.add(ve.getRef());
        // ALT
        record.add(ve.getAlt());
        // QUAL
        record.add(formatter.format(ve.getPhredScore()));
        // FILTER
        record.add(makeFiltersField(modeOfInheritance, ve));
        // GENOTYPE
        record.add(ve.getGenotypeString());
        // COVERAGE
        record.add(variantContext.getCommonInfo().getAttributeAsString("DP", "0"));
        // FUNCTIONAL_CLASS
        record.add(ve.getVariantEffect().getSequenceOntologyTerm());
        // HGVS
        record.add(getRepresentativeAnnotation(ve.getTranscriptAnnotations()));
        // EXOMISER_GENE
        record.add(ve.getGeneSymbol());
        PathogenicityData pathogenicityData = ve.getPathogenicityData();
        // CADD
        record.add(getPathScore(pathogenicityData.getPredictedScore(PathogenicitySource.CADD)));
        // POLYPHEN
        record.add(getPathScore(pathogenicityData.getPredictedScore(PathogenicitySource.POLYPHEN)));
        // MUTATIONTASTER
        record.add(getPathScore(pathogenicityData.getPredictedScore(PathogenicitySource.MUTATION_TASTER)));
        // SIFT
        record.add(getPathScore(pathogenicityData.getPredictedScore(PathogenicitySource.SIFT)));
        // REMM
        record.add(getPathScore(pathogenicityData.getPredictedScore(PathogenicitySource.REMM)));
        // "DBSNP_ID", "MAX_FREQUENCY", "DBSNP_FREQUENCY", "EVS_EA_FREQUENCY", "EVS_AA_FREQUENCY",
        // "EXAC_AFR_FREQ", "EXAC_AMR_FREQ", "EXAC_EAS_FREQ", "EXAC_FIN_FREQ", "EXAC_NFE_FREQ", "EXAC_SAS_FREQ", "EXAC_OTH_FREQ",
        addFrequencyData(ve.getFrequencyData(), record);
        // EXOMISER_VARIANT_SCORE
        record.add(dotIfNull(ve.getVariantScore()));
        // EXOMISER_GENE_PHENO_SCORE
        record.add(dotIfNull(gene.getPriorityScoreForMode(modeOfInheritance)));
        // EXOMISER_GENE_VARIANT_SCORE
        record.add(dotIfNull(gene.getVariantScoreForMode(modeOfInheritance)));
        // EXOMISER_GENE_COMBINED_SCORE
        record.add(dotIfNull(gene.getCombinedScoreForMode(modeOfInheritance)));
        // EXOMISER_CONTRIBUTES_TO_SCORE
        record.add(ve.contributesToGeneScoreUnderMode(modeOfInheritance) ? "CONTRIBUTING_VARIANT" : ".");
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

    private Object getPathScore(PathogenicityScore score) {
        if (score == null) {
            return ".";
        } else {
            return score.getScore();
        }
    }

    private String makeFiltersField(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        //under some modes a variant should not pass, but others it will, so we need to check this here
        //otherwise when running FULL or SPARSE modes alleles will be reported as having passed under the wrong MOI
        switch (variantEvaluation.getFilterStatusForMode(modeOfInheritance)) {
            case FAILED:
                Set<FilterType> failedFilterTypes = variantEvaluation.getFailedFilterTypesForMode(modeOfInheritance);
                return formatFailedFilters(failedFilterTypes);
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

        StringJoiner stringJoiner = new StringJoiner(":");
        stringJoiner.add(anno.getGeneSymbol());
        stringJoiner.add(anno.getAccession());
        stringJoiner.add(anno.getHgvsCdna());
        stringJoiner.add(anno.getHgvsProtein());
        return stringJoiner.toString();
    }

}
