/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgAssignment;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgEvidence;
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
public class TsvVariantAllMoiResultsWriter implements ResultsWriter {
    private static final Logger logger = LoggerFactory.getLogger(TsvVariantAllMoiResultsWriter.class);
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_VARIANT;
    private final CSVFormat csvFormat = CSVFormat.newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE", "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_VARIANT_SCORE", "CONTRIBUTING_VARIANT", "WHITELIST_VARIANT", "VCF_ID", "RS_ID", "CONTIG", "START", "END", "REF", "ALT", "CHANGE_LENGTH", "QUAL", "FILTER", "GENOTYPE", "FUNCTIONAL_CLASS", "HGVS", "EXOMISER_ACMG_CLASSIFICATION", "EXOMISER_ACMG_EVIDENCE", "EXOMISER_ACMG_DISEASE_ID", "EXOMISER_ACMG_DISEASE_NAME", "CLINVAR_ALLELE_ID", "CLINVAR_PRIMARY_INTERPRETATION", "CLINVAR_STAR_RATING", "GENE_CONSTRAINT_LOEUF", "GENE_CONSTRAINT_LOEUF_LOWER", "GENE_CONSTRAINT_LOEUF_UPPER", "MAX_FREQ_SOURCE", "MAX_FREQ", "ALL_FREQ", "MAX_PATH_SOURCE", "MAX_PATH", "ALL_PATH");
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    public TsvVariantAllMoiResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    public void writeFile(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.getSample();
        String outFileName = ResultsWriterUtils.makeOutputFilename(sample.getVcfPath(), outputSettings.getOutputPrefix(), OUTPUT_FORMAT, ModeOfInheritance.ANY);
        Path outFile = Path.of(outFileName);

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), this.csvFormat)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (Exception var12) {
            logger.error("Unable to write results to file {}", outFileName, var12);
        }

        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, ModeOfInheritance.ANY.getAbbreviation(), outFileName);
    }

    public String writeString(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        StringBuilder output = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(output, this.csvFormat)) {
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
        List<Object> fields = new ArrayList<>(csvFormat.getHeader().length);
        GeneIdentifier geneIdentifier = geneScore.getGeneIdentifier();
        ModeOfInheritance modeOfInheritance = geneScore.getModeOfInheritance();
        String moiAbbreviation = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        List<AcmgAssignment> acmgAssignments = geneScore.getAcmgAssignments();
        Optional<AcmgAssignment> assignment = acmgAssignments.stream().filter(acmgAssignment -> acmgAssignment.variantEvaluation().equals(ve)).findFirst();
        fields.add(rank);
        String gnomadString = ve.toGnomad();
        fields.add(gnomadString + "_" + moiAbbreviation);
        fields.add(geneIdentifier.getGeneSymbol());
        fields.add(geneIdentifier.getEntrezId());
        fields.add(moiAbbreviation);
        fields.add(decimalFormat.format(geneScore.pValue()));
        fields.add(decimalFormat.format(geneScore.getCombinedScore()));
        fields.add(decimalFormat.format(geneScore.getPhenotypeScore()));
        fields.add(decimalFormat.format(geneScore.getVariantScore()));
        fields.add(decimalFormat.format(ve.getVariantScore()));
        fields.add(ve.contributesToGeneScoreUnderMode(modeOfInheritance) ? "1" : "0");
        fields.add(ve.isWhiteListed() ? "1" : "0");
        fields.add(ve.id());
        FrequencyData frequencyData = ve.getFrequencyData();
        fields.add(frequencyData.getRsId());
        fields.add(ve.contigName());
        fields.add(ve.start());
        fields.add(ve.end());
        fields.add(ve.ref());
        fields.add(ve.alt());
        fields.add(ve.changeLength());
        fields.add(this.decimalFormat.format(ve.getPhredScore()));
        fields.add(this.makeFiltersField(modeOfInheritance, ve));
        fields.add(ve.getGenotypeString());
        fields.add(ve.getVariantEffect().getSequenceOntologyTerm());
        fields.add(this.getRepresentativeAnnotation(ve.getTranscriptAnnotations()));
        fields.add(assignment.map(AcmgAssignment::acmgClassification).orElse(AcmgClassification.NOT_AVAILABLE));
        fields.add(assignment.map(acmgAssignment -> toVcfAcmgInfo(acmgAssignment.acmgEvidence())).orElse(""));
        fields.add(assignment.map(acmgAssignment -> acmgAssignment.disease().getDiseaseId()).orElse(""));
        fields.add(assignment.map(acmgAssignment -> acmgAssignment.disease().getDiseaseName()).orElse(""));
        PathogenicityData pathogenicityData = ve.getPathogenicityData();
        ClinVarData clinVarData = pathogenicityData.getClinVarData();
        fields.add(clinVarData.getAlleleId());
        fields.add(clinVarData.getPrimaryInterpretation());
        fields.add(clinVarData.starRating());
        GeneConstraint geneConstraint = GeneConstraints.geneContraint(geneIdentifier.getGeneSymbol());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeuf());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufLower());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufUpper());
        Frequency maxFreq = frequencyData.getMaxFrequency();
        fields.add(maxFreq == null ? "" : maxFreq.getSource());
        fields.add(maxFreq == null ? "" : maxFreq.getFrequency());
        fields.add(toVcfFreqInfo(frequencyData.getKnownFrequencies()));
        PathogenicityScore maxPath = pathogenicityData.getMostPathogenicScore();
        fields.add(maxPath == null ? "" : maxPath.getSource());
        fields.add(maxPath == null ? "" : maxPath.getScore());
        fields.add(toVcfPathInfo(pathogenicityData.getPredictedPathogenicityScores()));
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
                .map(frequency -> frequency.getSource() + "=" + frequency.getFrequency())
                .collect(joining(","));
    }

    private String toVcfPathInfo(List<PathogenicityScore> predictedPathogenicityScores) {
        return predictedPathogenicityScores.stream()
                .map(pathScore -> pathScore.getSource() + "=" + pathScore.getScore())
                .collect(joining(","));
    }

    private String makeFiltersField(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        return switch (variantEvaluation.getFilterStatusForMode(modeOfInheritance)) {
            case FAILED -> formatFailedFilters(variantEvaluation.getFailedFilterTypesForMode(modeOfInheritance));
            case PASSED -> "PASS";
            case UNFILTERED -> ".";
        };
    }

    private String formatFailedFilters(Set<FilterType> failedFilters) {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (FilterType filterType : failedFilters) {
            stringJoiner.add(filterType.vcfValue());
        }
        return stringJoiner.toString();
    }

    private String getRepresentativeAnnotation(List<TranscriptAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return "";
        } else {
            TranscriptAnnotation anno = annotations.get(0);
            StringJoiner stringJoiner = new StringJoiner(":");
            stringJoiner.add(anno.getGeneSymbol());
            stringJoiner.add(anno.getAccession());
            stringJoiner.add(anno.getHgvsCdna());
            stringJoiner.add(anno.getHgvsProtein());
            return stringJoiner.toString();
        }
    }

}
