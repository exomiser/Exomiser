package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Utility class for ranking Gene and VariantEvaluation objects for use by ResultsWriters.
 *
 * @since 13.1.0
 */
class GeneScoreRanker {

    private static final Logger logger = LoggerFactory.getLogger(GeneScoreRanker.class);

    private final OutputSettings outputSettings;
    private final boolean contributingVariantsOnly;

    private final List<Gene> filteredGenesForOutput;
    private final Map<GeneIdentifier, Gene> genesById;

    GeneScoreRanker(AnalysisResults analysisResults, OutputSettings outputSettings) {
        this.outputSettings = outputSettings;
        this.contributingVariantsOnly = outputSettings.outputContributingVariantsOnly();
        this.filteredGenesForOutput = outputSettings.filterGenesForOutput(analysisResults.getGenes());
        this.genesById = filteredGenesForOutput.stream().collect(toUnmodifiableMap(Gene::getGeneIdentifier, Function.identity()));
    }

    Map<GeneIdentifier, Gene> mapGenesByGeneIdentifier() {
        return genesById;
    }

    private List<GeneScore> calculateRankedGeneScores() {
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
                    } else if (!gene.hasVariants() && compatibleGeneScores.isEmpty()) {
                        // in the case of a phenotype-only analysis there wil be no variants loaded which will result in
                        // an empty genes.tsv file. To avoid this, we want to add the ANY MOI score. The combined score
                        // will only be zero for genes where both the phenotypeScore and variantScore are zero.
                        compatibleGeneScores.add(gene.getGeneScoreForMode(ModeOfInheritance.ANY));
                    }
                    return compatibleGeneScores.stream();
                })
                .sorted()
                .collect(partitioningBy(o -> o.getCombinedScore() != 0));

        if (outputSettings.outputContributingVariantsOnly()) {
            return rankedAndUnrankedGeneScores.get(true);
        } else {
            List<GeneScore> rankedGeneScores = new ArrayList<>(rankedAndUnrankedGeneScores.get(true).size() + rankedAndUnrankedGeneScores.get(false).size());
            rankedGeneScores.addAll(rankedAndUnrankedGeneScores.get(true));
            rankedGeneScores.addAll(rankedAndUnrankedGeneScores.get(false));
            return List.copyOf(rankedGeneScores);
        }
    }

    Stream<RankedGene> rankedGenes() {
        ScoreRanker scoreRanker = new ScoreRanker(4);
        return calculateRankedGeneScores().stream()
                .map(geneScore -> {
                    int rank = scoreRanker.rank(geneScore.getCombinedScore());
                    return new RankedGene(rank, genesById.get(geneScore.getGeneIdentifier()), geneScore);
                });
    }

    Stream<RankedVariant> rankedVariants() {
        return rankedGenes().flatMap(rankedGene -> {
            int rank = rankedGene.rank();
            GeneScore geneScore = rankedGene.geneScore();
            ModeOfInheritance modeOfInheritance = geneScore.getModeOfInheritance();
            logger.debug("{} {} {} {} {} {}", rank, geneScore.getGeneIdentifier().getGeneSymbol(), modeOfInheritance.getAbbreviation(), geneScore.getCombinedScore(), geneScore.getPhenotypeScore(), geneScore.getVariantScore());
            // a GeneScore only contains the contributing variants so can't be used directly to get the variants involved, hence the requirement for the Gene.
            return rankedGene.gene()
                    .getVariantEvaluations().stream()
                    .filter(variantEvaluation -> !contributingVariantsOnly || variantEvaluation.contributesToGeneScoreUnderMode(modeOfInheritance))
                    .filter(variantEvaluation -> variantEvaluation.isCompatibleWith(modeOfInheritance))
                    .filter(variantEvaluation -> (geneScore.getCombinedScore() == 0) != variantEvaluation.passedFilters())
                    .sorted(VariantEvaluation::compareByRank)
                    .map(ve -> new RankedVariant(rank, ve, geneScore));
        });
    }

    static class RankedGene {
        private final int rank;
        private final Gene gene;
        private final GeneScore geneScore;

        public RankedGene(int rank, Gene gene, GeneScore geneScore) {
            this.rank = rank;
            this.gene = Objects.requireNonNull(gene);
            this.geneScore = Objects.requireNonNull(geneScore);
        }

        public int rank() {
            return rank;
        }

        public Gene gene() {
            return gene;
        }

        public GeneScore geneScore() {
            return geneScore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RankedGene that = (RankedGene) o;
            return rank == that.rank && gene.equals(that.gene) && geneScore.equals(that.geneScore);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rank, gene, geneScore);
        }

        @Override
        public String toString() {
            return "RankedGene{" +
                    "rank=" + rank +
                    ", gene=" + gene +
                    ", geneScore=" + geneScore +
                    '}';
        }
    }

    static class RankedVariant {
        private final int rank;
        private final VariantEvaluation variantEvaluation;
        private final GeneScore geneScore;

        public RankedVariant(int rank, VariantEvaluation variantEvaluation, GeneScore geneScore) {
            this.rank = rank;
            this.variantEvaluation = Objects.requireNonNull(variantEvaluation);
            this.geneScore = Objects.requireNonNull(geneScore);
        }

        public int rank() {
            return rank;
        }

        public VariantEvaluation variantEvaluation() {
            return variantEvaluation;
        }

        public GeneScore geneScore() {
            return geneScore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RankedVariant that = (RankedVariant) o;
            return rank == that.rank && variantEvaluation.equals(that.variantEvaluation) && geneScore.equals(that.geneScore);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rank, variantEvaluation, geneScore);
        }

        @Override
        public String toString() {
            return "RankedVariant{" +
                    "rank=" + rank +
                    ", variantEvaluation=" + variantEvaluation +
                    ", geneScore=" + geneScore +
                    '}';
        }
    }
}
