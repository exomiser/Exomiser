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
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.monarchinitiative.exomiser.core.model.FilterStatus;
import org.monarchinitiative.exomiser.core.model.Gene;
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

public class TsvVariantAllMoiResultsWriter implements ResultsWriter {
    private static final Logger logger = LoggerFactory.getLogger(TsvVariantAllMoiResultsWriter.class);
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_VARIANT;
    private final CSVFormat format = CSVFormat.newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE", "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "EXOMISER_VARIANT_SCORE", "CONTRIBUTING_VARIANT", "WHITELIST_VARIANT", "VCF_ID", "RS_ID", "CONTIG", "START", "END", "REF", "ALT", "CHANGE_LENGTH", "QUAL", "FILTER", "GENOTYPE", "FUNCTIONAL_CLASS", "HGVS", "EXOMISER_ACMG_CLASSIFICATION", "EXOMISER_ACMG_EVIDENCE", "EXOMISER_ACMG_DISEASE_ID", "EXOMISER_ACMG_DISEASE_NAME", "CLINVAR_ALLELE_ID", "CLINVAR_PRIMARY_INTERPRETATION", "CLINVAR_STAR_RATING", "GENE_CONSTRAINT_LOEUF", "GENE_CONSTRAINT_LOEUF_LOWER", "GENE_CONSTRAINT_LOEUF_UPPER", "MAX_FREQ_SOURCE", "MAX_FREQ", "ALL_FREQ", "MAX_PATH_SOURCE", "MAX_PATH", "ALL_PATH");
    private final DecimalFormat formatter = new DecimalFormat(".####");

    public TsvVariantAllMoiResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    public void writeFile(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.getSample();
        String outFileName = ResultsWriterUtils.makeOutputFilename(sample.getVcfPath(), outputSettings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), this.format)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (IOException var12) {
            logger.error("Unable to write results to file {}", outFileName, var12);
        }

        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, modeOfInheritance.getAbbreviation(), outFileName);
    }

    public String writeString(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        StringBuilder output = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(output, this.format)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (IOException var10) {
            logger.error("Unable to write results to string {}", output, var10);
        }

        return output.toString();
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings outputSettings, CSVPrinter printer) throws IOException {
        List<Gene> filteredGenesForOutput = outputSettings.filterGenesForOutput(analysisResults.getGenes());
        Map<GeneIdentifier, Gene> genesById = filteredGenesForOutput.stream().collect(Collectors.toMap(Gene::getGeneIdentifier, Function.identity()));
        List<GeneScore> rankedGeneScores = this.calculateRankedGeneScores(outputSettings, filteredGenesForOutput);
        boolean contributingVariantsOnly = outputSettings.outputContributingVariantsOnly();
        int rank = 0;
        for (GeneScore geneScore : rankedGeneScores) {
            ++rank;
            ModeOfInheritance modeOfInheritance = geneScore.getModeOfInheritance();
            logger.debug("{} {} {} {} {} {}", rank, geneScore.getGeneIdentifier().getGeneSymbol(), modeOfInheritance.getAbbreviation(), geneScore.getCombinedScore(), geneScore.getPhenotypeScore(), geneScore.getVariantScore());
            // a GeneScore only contains the contributing variants so can't be used directly to get the variants involved, hence the requirement for the Gene.
            List<VariantEvaluation> rankedVariants = genesById.get(geneScore.getGeneIdentifier())
                    .getVariantEvaluations().stream()
                    .filter(variantEvaluation -> !contributingVariantsOnly || variantEvaluation.contributesToGeneScoreUnderMode(modeOfInheritance))
                    .filter(variantEvaluation -> variantEvaluation.isCompatibleWith(modeOfInheritance))
                    .filter(variantEvaluation -> (geneScore.getCombinedScore() == 0) != variantEvaluation.passedFilters())
                    .sorted(VariantEvaluation::compareByRank)
                    .collect(Collectors.toList());

            for (VariantEvaluation ve : rankedVariants) {
                List<Object> fields = this.buildVariantRecord(rank, ve, geneScore);
                printer.printRecord(fields);
            }
        }

    }

    private List<GeneScore> calculateRankedGeneScores(OutputSettings outputSettings, List<Gene> filteredGenesForOutput) {
        Map<Boolean, List<GeneScore>> rankedAndUnrankedGeneScores = filteredGenesForOutput.stream()
                .flatMap(gene -> {
                    List<GeneScore> compatibleGeneScores = new ArrayList<>(gene.getCompatibleGeneScores());
                    if (gene.getVariantEvaluations().stream().anyMatch(ve -> ve.getFilterStatus() == FilterStatus.FAILED)) {
                        // create a failed gene score placeholder for when run in FULL mode
                        GeneScore geneScore = GeneScore.builder()
                                .geneIdentifier(gene.getGeneIdentifier())
                                .modeOfInheritance(ModeOfInheritance.ANY)
                                .combinedScore(0)
                                .phenotypeScore(gene.getPriorityScore())
                                .variantScore(0)
                                .build();
                        compatibleGeneScores.add(geneScore);
                    }
                    return compatibleGeneScores.stream();
                })
                .sorted()
                .collect(Collectors.partitioningBy(o -> o.getCombinedScore() != 0));
        if (outputSettings.outputContributingVariantsOnly()) {
            logger.debug("Writing out only CONTRIBUTING variants");
            return rankedAndUnrankedGeneScores.get(true);
        } else {
            List<GeneScore> rankedGeneScores = new ArrayList<>(rankedAndUnrankedGeneScores.get(true).size() + rankedAndUnrankedGeneScores.get(false).size());
            rankedGeneScores.addAll(rankedAndUnrankedGeneScores.get(true));
            rankedGeneScores.addAll(rankedAndUnrankedGeneScores.get(false));
            return List.copyOf(rankedGeneScores);
        }
    }

    private List<Object> buildVariantRecord(int rank, VariantEvaluation ve, GeneScore geneScore) {
        List<Object> fields = new ArrayList<>();
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
        fields.add(geneScore.pValue());
        fields.add(geneScore.getCombinedScore());
        fields.add(geneScore.getPhenotypeScore());
        fields.add(geneScore.getVariantScore());
        fields.add((double) ve.getVariantScore());
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
        fields.add(this.formatter.format(ve.getPhredScore()));
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
                .collect(Collectors.joining(","));
    }
    private String toVcfFreqInfo(List<Frequency> frequencies) {
        return frequencies.stream()
                .map(frequency -> frequency.getSource() + "=" + frequency.getFrequency())
                .collect(Collectors.joining(","));
    }

    private String toVcfPathInfo(List<PathogenicityScore> predictedPathogenicityScores) {
        return predictedPathogenicityScores.stream()
                .map(pathScore -> pathScore.getSource() + "=" + pathScore.getScore())
                .collect(Collectors.joining(","));
    }

    private String makeFiltersField(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        switch (variantEvaluation.getFilterStatusForMode(modeOfInheritance)) {
            case FAILED:
                Set<FilterType> failedFilterTypes = variantEvaluation.getFailedFilterTypesForMode(modeOfInheritance);
                return this.formatFailedFilters(failedFilterTypes);
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
