package org.monarchinitiative.exomiser.core.writers;

import com.jerolba.carpet.CarpetWriter;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.score.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.score.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgAssignment;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgCriterion;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgEvidence;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;


public class ParquetVariantResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(ParquetVariantResultsWriter.class);

    private static final VariantEvaluation EMPTY_VARIANT = VariantEvaluation.builder()
            .variant(GenomicVariant.of(Contig.unknown(), Strand.POSITIVE, Coordinates.empty(), "", ""))
            .build();

    @Override
    public void writeFile(AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.sample();
        String outFile = outputSettings.makeOutputFilePath(sample.vcfPath(), OutputFormat.JSON).toString().replace("json", "parquet");

        try (OutputStream outputStream = Files.newOutputStream(Path.of(outFile));
             CarpetWriter<VariantResult> writer = new CarpetWriter.Builder<>(outputStream, VariantResult.class)
                     .enableDictionaryEncoding()
                     .withExtraMetaData("probandId", analysisResults.sample().probandSampleName())
                     .withExtraMetaData("assembly", analysisResults.sample().genomeAssembly().toGrcString())
                     .withCompressionCodec(CompressionCodecName.ZSTD)
                     .build()) {
            GeneScoreRanker geneScoreRanker = new GeneScoreRanker(analysisResults, outputSettings);
            geneScoreRanker.rankedGenes()
                    .flatMap(toVariantResults(outputSettings.outputContributingVariantsOnly()))
                    .forEach(writer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    Function<GeneScoreRanker.RankedGene, Stream<VariantResult>> toVariantResults(boolean contributingVariantsOnly) {
        return rankedGene -> {
            int rank = rankedGene.rank();
            GeneScore geneScore = rankedGene.geneScore();
            ModeOfInheritance modeOfInheritance = geneScore.modeOfInheritance();
            logger.debug("{} {} {} {} {} {}", rank, geneScore.geneIdentifier().geneSymbol(), modeOfInheritance.getAbbreviation(), geneScore.combinedScore(), geneScore.phenotypeScore(), geneScore.variantScore());
            // a GeneScore only contains the contributing variants so can't be used directly to get the variants involved, hence the requirement for the Gene.
            Gene gene = rankedGene.gene();
            List<Disease> associatedDiseases = gene.associatedDiseases();
            HiPhivePriorityResult hiPhivePriorityResult = gene.getPriorityResult(HiPhivePriorityResult.class);
            if (gene.hasVariants()) {
                return gene.variantEvaluations().stream()
                        .filter(variantEvaluation -> !contributingVariantsOnly || variantEvaluation.contributesToGeneScoreUnderMode(modeOfInheritance))
                        .filter(variantEvaluation -> variantEvaluation.isCompatibleWith(modeOfInheritance))
                        .filter(variantEvaluation -> (geneScore.combinedScore() == 0) != variantEvaluation.passedFilters())
                        .sorted(VariantEvaluation::compareByRank)
                        .map(ve -> buildVariantRecord(rank, ve, geneScore, associatedDiseases, hiPhivePriorityResult));
            } else if (geneScore.combinedScore() != 0) {
                // for phenotype-only cases, only return gene scores with a match
                return Stream.of(buildVariantRecord(rank, EMPTY_VARIANT, geneScore, associatedDiseases, hiPhivePriorityResult));
            }
            return Stream.of();
        };
    }

    private VariantResult buildVariantRecord(int rank, VariantEvaluation ve, GeneScore geneScore, List<Disease> associatedDiseases, HiPhivePriorityResult hiPhivePriorityResult) {
        boolean emptyVariant = ve.coordinates().equals(Coordinates.empty());
        GeneIdentifier geneIdentifier = geneScore.geneIdentifier();
        ModeOfInheritance modeOfInheritance = geneScore.modeOfInheritance();
        String moi = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        Optional<TranscriptAnnotation> transcriptAnnotation = Optional.ofNullable(ve.transcriptAnnotations().isEmpty() ? null : ve.transcriptAnnotations().getFirst());
        List<AcmgAssignment> acmgAssignments = geneScore.acmgAssignments();
        Optional<AcmgAssignment> acmgAssignment = acmgAssignments.stream().filter(assignment -> assignment.variantEvaluation().equals(ve)).findFirst();
        String gnomadString = ve.toGnomad();
        String variantId = emptyVariant ? "" : gnomadString + "_" + moi;
        Optional<GeneConstraint> geneConstraint = Optional.ofNullable(GeneConstraints.geneConstraint(geneIdentifier.geneSymbol()));
        FrequencyData frequencyData = ve.frequencyData();
        Optional<Frequency> maxFrequency = Optional.ofNullable(frequencyData.maxFrequency());
        PathogenicityData pathogenicityData = ve.pathogenicityData();
        ClinVarData clinVarData = pathogenicityData.clinVarData();
        Optional<PathogenicityScore> maxPath = Optional.ofNullable(pathogenicityData.mostPathogenicScore());
        return new VariantResult(rank,
                variantId,
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
                failedFilters(modeOfInheritance, ve),
                ve.id(),
                frequencyData.rsId(),
                ve.contigName(), ve.contigId(), ve.start(), ve.end(), ve.ref(), ve.alt(), ve.changeLength(),
                ve.phredScore(),
                emptyVariant ? "./." : ve.genotypeString(),
                ve.variantEffect(),
                transcriptAnnotation.map(TranscriptAnnotation::accession).orElse(""),
                transcriptAnnotation.map(TranscriptAnnotation::hgvsCdna).orElse(""),
                transcriptAnnotation.map(TranscriptAnnotation::hgvsProtein).orElse(""),

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
                pathogenicityData.pathogenicityScores().stream().map(RawPathogenicityScore::from).toList(),
                hiPhivePriorityResult == null ? 0 : hiPhivePriorityResult.humanScore(),
                hiPhivePriorityResult == null ? 0 : hiPhivePriorityResult.mouseScore(),
                hiPhivePriorityResult == null ? 0 : hiPhivePriorityResult.fishScore(),
                hiPhivePriorityResult == null ? 0 : hiPhivePriorityResult.ppiScore(),
                associatedDiseases,
                geneScore.compatibleDiseaseMatches().stream()
                        .map(dm -> new DiseaseMatch(dm.score(), dm.model().diseaseId(), toSimplePhenotypeMatches(dm.bestPhenotypeMatches())))
                        .toList(),
                hiPhivePriorityResult == null ? List.of() : hiPhivePriorityResult.phenotypeEvidence().stream()
                        .filter(modelMatch -> modelMatch.organism() == Organism.MOUSE)
                        .map(m -> new MouseMatch(m.score(), m.model().id(), toSimplePhenotypeMatches(m.bestPhenotypeMatches())))
                        .toList(),
                hiPhivePriorityResult == null ? List.of() : hiPhivePriorityResult.phenotypeEvidence().stream()
                        .filter(modelMatch -> modelMatch.organism() == Organism.FISH)
                        .map(m -> new FishMatch(m.score(), m.model().id(), toSimplePhenotypeMatches(m.bestPhenotypeMatches())))
                        .toList(),
                hiPhivePriorityResult == null ? List.of() : hiPhivePriorityResult.ppiEvidence().stream()
                        .map(m -> new PpiMatch(m.score(), m.model().id(), toSimplePhenotypeMatches(m.bestPhenotypeMatches())))
                        .toList()
        );
    }

    private List<SimplePhenotypeMatch> toSimplePhenotypeMatches(List<PhenotypeMatch> phenotypeMatches) {
        List<SimplePhenotypeMatch> temp = new ArrayList<>(phenotypeMatches.size());
        for (PhenotypeMatch match : phenotypeMatches) {
            temp.add(new SimplePhenotypeMatch(match.score(), match.queryPhenotype(), match.matchPhenotype()));
        }
        return List.copyOf(temp);
    }

    record RawPathogenicityScore(PathogenicitySource source, double score, double rawScore) {
        static RawPathogenicityScore from(PathogenicityScore pathScore) {
            return new RawPathogenicityScore(pathScore.source(), pathScore.score(), pathScore.rawScore());
        }
    }

    record VariantResult(int rank, String variant,
                         String geneSymbol, String hgncId, String ensemblGeneId, String ncbiGeneId,
                         String moi, double pValue,
                         double geneCombinedScore, double genePhenotypeScore, double geneVariantScore, float variantScore, boolean isContributingVariant,
                         boolean isWhiteListVariant,
                         List<FilterType> failedFilters,
                         String vcfId, String rsId, String contigName, int contigId, int start, int end, String ref, String alt, int changeLength,
                         double qual, String genotype,
                         VariantEffect functionalClass, String transcriptId, String hgvsC, String hgvsP,
                         AcmgClassification acmgClassification, List<String> acmgEvidence,
                         double acmgProbability, int acmgTotalPoints, int acmgPathPoints, int acmgBenignPoints,
                         String clinVarVariationId, ClinVarData.ClinSig clinVarPrimaryInterpretation,
                         int clinVarStarRating,
                         boolean isLofIntolerant, double pLI, double loeuf, double loeufLower, double loeufUpper,
                         double missenseZ, double synonymousZ,
                         FrequencySource maxFreqSource, double maxFreq, int maxFreqAC, int maxFreqAN, int maxFreqHoms,
                         List<Frequency> frequencies,
                         PathogenicitySource maxPathSource, double maxPathScore, double maxPathRawScore,
                         List<RawPathogenicityScore> pathogenicityScores,
                         double diseasePhenotypeScore,
                         double mousePhenotypeScore,
                         double fishPhenotypeScore,
                         double ppiScore,
                         List<Disease> associatedDiseases,
                         List<DiseaseMatch> diseaseMatches,
                         List<MouseMatch> mouseMatches,
                         List<FishMatch> fishMatches,
                         List<PpiMatch> ppiMatches
    ) {}

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

    private List<FilterType> failedFilters(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        return switch (variantEvaluation.filterStatusForMode(modeOfInheritance)) {
            case FAILED -> List.copyOf(variantEvaluation.failedFilterTypesForMode(modeOfInheritance));
            case UNFILTERED, PASSED -> List.of();
        };
    }

    record DiseaseMatch(double score, String diseaseId, List<SimplePhenotypeMatch> phenotypeMatches) {}
    record MouseMatch(double score, String mgiId, List<SimplePhenotypeMatch> phenotypeMatches) {}
    record FishMatch(double score, String zfinId, List<SimplePhenotypeMatch> phenotypeMatches) {}
    record PpiMatch(double score, String id, List<SimplePhenotypeMatch> phenotypeMatches) {}

    record SimplePhenotypeMatch(double score, PhenotypeTerm query, PhenotypeTerm match) {}

}
