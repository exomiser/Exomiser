/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2023 Queen Mary University of London.
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgAssignment;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgCriterion;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgEvidence;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.*;

/**
 * @since 13.1.0
 */
public class TsvVariantResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvVariantResultsWriter.class);
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_VARIANT;
    public static final CSVFormat EXOMISER_VARIANTS_TSV_FORMAT = CSVFormat.newFormat('\t').builder()
            .setRecordSeparator("\n")
            .setIgnoreSurroundingSpaces(true)
            .setHeader("#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE", "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_VARIANT_SCORE", "CONTRIBUTING_VARIANT", "WHITELIST_VARIANT", "VCF_ID", "RS_ID", "CONTIG", "START", "END", "REF", "ALT", "CHANGE_LENGTH", "QUAL", "FILTER", "GENOTYPE", "FUNCTIONAL_CLASS", "HGVS", "EXOMISER_ACMG_CLASSIFICATION", "EXOMISER_ACMG_EVIDENCE", "EXOMISER_ACMG_DISEASE_ID", "EXOMISER_ACMG_DISEASE_NAME", "CLINVAR_VARIATION_ID", "CLINVAR_PRIMARY_INTERPRETATION", "CLINVAR_STAR_RATING", "GENE_CONSTRAINT_LOEUF", "GENE_CONSTRAINT_LOEUF_LOWER", "GENE_CONSTRAINT_LOEUF_UPPER", "MAX_FREQ_SOURCE", "MAX_FREQ", "ALL_FREQ", "MAX_PATH_SOURCE", "MAX_PATH", "ALL_PATH")
            .build();

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    public TsvVariantResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.sample();
        Path outFile = outputSettings.makeOutputFilePath(sample.vcfPath(), OUTPUT_FORMAT);

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), EXOMISER_VARIANTS_TSV_FORMAT)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFile, ex);
        }

        logger.debug("{} results written to file {}", OUTPUT_FORMAT, outFile);
    }

    @Override
    public String writeString(AnalysisResults analysisResults, OutputSettings outputSettings) {
        StringBuilder output = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(output, EXOMISER_VARIANTS_TSV_FORMAT)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (Exception var10) {
            logger.error("Unable to write results to string {}", output, var10);
        }

        return output.toString();
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings outputSettings, CSVPrinter printer) {
        GeneScoreRanker geneScoreRanker = new GeneScoreRanker(analysisResults, outputSettings);
        geneScoreRanker.rankedVariants()
                .map(rankedVariant -> buildVariantRecord(rankedVariant.rank(), rankedVariant.variantEvaluation(), rankedVariant.geneScore()))
                .forEach(printRecord(printer));
    }

    Consumer<Iterable<Object>> printRecord(CSVPrinter printer) {
        return list -> {
            try {
                printer.printRecord(list);
            } catch (IOException e) {
                // cross fingers and swallow?
                throw new IllegalStateException(e);
            }
        };
    }

    private List<Object> buildVariantRecord(int rank, VariantEvaluation ve, GeneScore geneScore) {
        List<Object> fields = new ArrayList<>(EXOMISER_VARIANTS_TSV_FORMAT.getHeader().length);
        GeneIdentifier geneIdentifier = geneScore.geneIdentifier();
        ModeOfInheritance modeOfInheritance = geneScore.modeOfInheritance();
        String moiAbbreviation = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        List<AcmgAssignment> acmgAssignments = geneScore.acmgAssignments();
        Optional<AcmgAssignment> assignment = acmgAssignments.stream().filter(acmgAssignment -> acmgAssignment.variantEvaluation().equals(ve)).findFirst();
        fields.add(rank);
        String gnomadString = ve.toGnomad();
        fields.add(gnomadString + "_" + moiAbbreviation);
        fields.add(geneIdentifier.geneSymbol());
        fields.add(geneIdentifier.entrezId());
        fields.add(moiAbbreviation);
        fields.add(decimalFormat.format(geneScore.pValue()));
        fields.add(decimalFormat.format(geneScore.combinedScore()));
        fields.add(decimalFormat.format(geneScore.phenotypeScore()));
        fields.add(decimalFormat.format(geneScore.variantScore()));
        fields.add(decimalFormat.format(ve.variantScore()));
        fields.add(ve.contributesToGeneScoreUnderMode(modeOfInheritance) ? "1" : "0");
        fields.add(ve.isWhiteListed() ? "1" : "0");
        fields.add(ve.id());
        FrequencyData frequencyData = ve.frequencyData();
        fields.add(frequencyData.getRsId());
        fields.add(ve.contigName());
        fields.add(ve.start());
        fields.add(ve.end());
        fields.add(ve.ref());
        fields.add(ve.alt());
        fields.add(ve.changeLength());
        fields.add(this.decimalFormat.format(ve.phredScore()));
        fields.add(this.makeFiltersField(modeOfInheritance, ve));
        fields.add(ve.genotypeString());
        fields.add(ve.variantEffect().getSequenceOntologyTerm());
        fields.add(this.getRepresentativeAnnotation(ve.transcriptAnnotations()));
        fields.add(assignment.map(AcmgAssignment::acmgClassification).orElse(AcmgClassification.NOT_AVAILABLE));
        fields.add(assignment.map(acmgAssignment -> toVcfAcmgInfo(acmgAssignment.acmgEvidence())).orElse(""));
        fields.add(assignment.map(acmgAssignment -> acmgAssignment.disease().diseaseId()).orElse(""));
        fields.add(assignment.map(acmgAssignment -> acmgAssignment.disease().diseaseName()).orElse(""));
        PathogenicityData pathogenicityData = ve.pathogenicityData();
        ClinVarData clinVarData = pathogenicityData.clinVarData();
        fields.add(clinVarData.variationId());
        fields.add(clinVarData.primaryInterpretation());
        fields.add(clinVarData.starRating());
        GeneConstraint geneConstraint = GeneConstraints.geneConstraint(geneIdentifier.geneSymbol());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeuf());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufLower());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufUpper());
        Frequency maxFreq = frequencyData.maxFrequency();
        fields.add(maxFreq == null ? "" : maxFreq.source());
        fields.add(maxFreq == null ? "" : maxFreq.frequency());
        fields.add(toVcfFreqInfo(frequencyData.frequencies()));
        PathogenicityScore maxPath = pathogenicityData.mostPathogenicScore();
        fields.add(maxPath == null ? "" : maxPath.source());
        fields.add(maxPath == null ? "" : maxPath.score());
        fields.add(toVcfPathInfo(pathogenicityData.pathogenicityScores()));
        return fields;
    }

    private String toVcfAcmgInfo(AcmgEvidence acmgEvidence) {
        return acmgEvidence.evidence().entrySet().stream()
                .map(entry -> {
                    AcmgCriterion acmgCriterion = entry.getKey();
                    AcmgCriterion.Evidence evidence = entry.getValue();
                    return (acmgCriterion.evidence() == evidence) ? acmgCriterion.toString() : acmgCriterion + "_" + evidence.displayString();
                })
                .collect(joining(","));
    }
    private String toVcfFreqInfo(List<Frequency> frequencies) {
        return frequencies.stream()
                .map(frequency -> frequency.source() + "=" + frequency.frequency())
                .collect(joining(","));
    }

    private String toVcfPathInfo(List<PathogenicityScore> predictedPathogenicityScores) {
        return predictedPathogenicityScores.stream()
                .map(pathScore -> pathScore.source() + "=" + pathScore.score())
                .collect(joining(","));
    }

    private String makeFiltersField(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        //under some modes a variant should not pass, but others it will, so we need to check this here
        //otherwise when running FULL or SPARSE modes alleles will be reported as having passed under the wrong MOI
        return switch (variantEvaluation.filterStatusForMode(modeOfInheritance)) {
            case FAILED -> {
                Set<FilterType> failedFilterTypes = variantEvaluation.failedFilterTypesForMode(modeOfInheritance);
                yield formatFailedFilters(failedFilterTypes);
            }
            case PASSED -> "PASS";
            default -> ".";
        };
    }

    private String formatFailedFilters(Set<FilterType> failedFilters) {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (FilterType filterType : failedFilters) {
            stringJoiner.add(filterType.vcfValue());
        }
        return stringJoiner.toString();
    }

    /**
     * @return An annotation for a single transcript, representing one of the
     * annotations with the most pathogenic annotation.
     */
    private String getRepresentativeAnnotation(List<TranscriptAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return "";
        }
        TranscriptAnnotation anno = annotations.getFirst();

        StringJoiner stringJoiner = new StringJoiner(":");
        stringJoiner.add(anno.geneSymbol());
        stringJoiner.add(anno.accession());
        stringJoiner.add(anno.hgvsCdna());
        stringJoiner.add(anno.hgvsProtein());
        return stringJoiner.toString();
    }

}
