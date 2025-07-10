package org.monarchinitiative.exomiser.core.writers;

import com.jerolba.carpet.CarpetWriter;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
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
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;


public class ParquetVariantResultsWriter implements ResultsWriter {


    @Override
    public void writeFile(AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.sample();
        String outFile = outputSettings.makeOutputFilePath(sample.vcfPath(), OutputFormat.TSV_VARIANT).toString().replace("tsv", "parquet");

        try (OutputStream outputStream = Files.newOutputStream(Path.of(outFile));
             CarpetWriter<VariantResult> writer = new CarpetWriter.Builder<>(outputStream, VariantResult.class)
                     .enableDictionaryEncoding()
                     .withCompressionCodec(CompressionCodecName.ZSTD)
                     .build()) {
            GeneScoreRanker geneScoreRanker = new GeneScoreRanker(analysisResults, outputSettings);
            geneScoreRanker.rankedVariants()
                    .map(rankedVariant -> buildVariantRecord(rankedVariant.rank(), rankedVariant.variantEvaluation(), rankedVariant.geneScore()))
                    .forEach(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private VariantResult buildVariantRecord(int rank, VariantEvaluation ve, GeneScore geneScore) {
        GeneIdentifier geneIdentifier = geneScore.geneIdentifier();
        ModeOfInheritance modeOfInheritance = geneScore.modeOfInheritance();
        String moi = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        List<AcmgAssignment> acmgAssignments = geneScore.acmgAssignments();
        Optional<AcmgAssignment> acmgAssignment = acmgAssignments.stream().filter(assignment -> assignment.variantEvaluation().equals(ve)).findFirst();
        String gnomadString = ve.toGnomad();
        Optional<GeneConstraint> geneConstraint = Optional.ofNullable(GeneConstraints.geneConstraint(geneIdentifier.geneSymbol()));
        FrequencyData frequencyData = ve.frequencyData();
        Optional<Frequency> maxFrequency = Optional.ofNullable(frequencyData.maxFrequency());
        PathogenicityData pathogenicityData = ve.pathogenicityData();
        ClinVarData clinVarData = pathogenicityData.clinVarData();
        Optional<PathogenicityScore> maxPath = Optional.ofNullable(pathogenicityData.mostPathogenicScore());
        return new VariantResult(rank,
                gnomadString + "_" + moi,
                geneIdentifier.geneSymbol(),
                geneIdentifier.hgncId(),
                geneIdentifier.ensemblId(),
                geneIdentifier.entrezId(),
                moi,
                geneScore.pValue(),
                geneScore.combinedScore(),
                geneScore.phenotypeScore(),
                geneScore.variantScore(),
                ve.variantScore(),
                ve.contributesToGeneScoreUnderMode(modeOfInheritance),
                ve.isWhiteListed(),
                ve.id(),
                frequencyData.rsId(),
                ve.contigName(), ve.contigId(), ve.start(), ve.end(), ve.ref(), ve.alt(), ve.changeLength(),
                ve.phredScore(),
                makeFiltersField(modeOfInheritance, ve),
                ve.genotypeString(),
                ve.variantEffect(),
                buildRepresentativeAnnotationHgvs(ve.transcriptAnnotations()),

                acmgAssignment.map(AcmgAssignment::acmgClassification).orElse(AcmgClassification.NOT_AVAILABLE),
                acmgAssignment.map(assignment -> toAcmgInfo(assignment.acmgEvidence())).orElse(List.of()),
                acmgAssignment.map(assignment -> assignment.acmgEvidence().postProbPath()).orElse(0.1),
                acmgAssignment.map(assignment -> assignment.acmgEvidence().points()).orElse(0),
                acmgAssignment.map(assignment -> assignment.acmgEvidence().pathPoints()).orElse(0),
                acmgAssignment.map(assignment -> assignment.acmgEvidence().benignPoints()).orElse(0),

                clinVarData.variationId(), clinVarData.primaryInterpretation(), clinVarData.starRating(),

                geneConstraint.map(GeneConstraint::isLossOfFunctionIntolerant).orElse(false),
                geneConstraint.map(GeneConstraint::pLI).orElse(0.0),
                geneConstraint.map(GeneConstraint::loeuf).orElse(0.0),
                geneConstraint.map(GeneConstraint::loeufLower).orElse(0.0),
                geneConstraint.map(GeneConstraint::loeufUpper).orElse(0.0),
                geneConstraint.map(GeneConstraint::missenseZ).orElse(0.0),
                geneConstraint.map(GeneConstraint::synonymousZ).orElse(0.0),

                maxFrequency.map(Frequency::source).orElse(null), maxFrequency.map(Frequency::frequency).orElse(0f),
                maxFrequency.map(Frequency::ac).orElse(0), maxFrequency.map(Frequency::an).orElse(0),
                maxFrequency.map(Frequency::homs).orElse(0), frequencyData.frequencies(),

                maxPath.map(PathogenicityScore::source).orElse(null), maxPath.map(PathogenicityScore::score).orElse(0f),
                maxPath.map(PathogenicityScore::rawScore).orElse(0f),
                pathogenicityData.pathogenicityScores().stream().map(RawPathogenicityScore::from).toList()
        );
    }

    record RawPathogenicityScore(PathogenicitySource source, double score, double rawScore) {
        static RawPathogenicityScore from(PathogenicityScore pathScore) {
            return new RawPathogenicityScore(pathScore.source(), pathScore.score(), pathScore.rawScore());
        }
    }

    record VariantResult(int rank, String variant,
                         String geneSymbol, String hgncId, String ensemblId, String entrezId,
                         String moiAbbreviation, double pValue,
                         double combinedScore, double phenotypeScore, double variantScore, float score, boolean isContributingVariant,
                         boolean isWhiteListVariant,
                         String vcfId, String rsId, String contigName, int contigId, int start, int end, String ref, String alt, int changeLength,
                         double qual, String filter, String genotype, VariantEffect functionalClass, String hgvs,
                         AcmgClassification acmgClassification, List<String> acmgEvidence,
                         double acmgProbability, int acmgPoints, int pathPoints, int benignPoints,
                         String clinVarVariationId, ClinVarData.ClinSig clinVarPrimaryInterpretation,
                         int clinVarStarRating,
                         boolean isLofIntolerant, double pLI, double loeuf, double loeufLower, double loeufUpper,
                         double missenseZ, double synonymousZ,
                         FrequencySource maxFreqSource, double maxFreq, int maxFreqAC, int maxFreqAN, int maxFreqHoms,
                         List<Frequency> frequencies,
                         PathogenicitySource maxPathSource, double maxPathScore, double maxPathRawScore,
                         List<RawPathogenicityScore> pathogenicityScores) {
    }

    @Override
    public String writeString(AnalysisResults analysisResults, OutputSettings outputSettings) {
        return "";
    }

    private List<String> toAcmgInfo(AcmgEvidence acmgEvidence) {
        return acmgEvidence.evidence().entrySet().stream()
                .map(entry -> {
                    AcmgCriterion acmgCriterion = entry.getKey();
                    AcmgCriterion.Evidence evidence = entry.getValue();
                    return (acmgCriterion.evidence() == evidence) ? acmgCriterion.toString() : acmgCriterion + "_" + evidence.displayString();
                })
                .toList();
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
    private String buildRepresentativeAnnotationHgvs(List<TranscriptAnnotation> annotations) {
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
