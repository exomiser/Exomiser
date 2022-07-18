package org.monarchinitiative.exomiser.core.analysis.util;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CombinedScorePvalueCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CombinedScorePvalueCalculator.class);

    private final int bootStrapValue;
    private final int numFilteredGenes;

    private final double[] phenoScoreCache;
    private final int phenoScoreCacheSize;
    private final Set<PriorityType> priorityTypes;

    CombinedScorePvalueCalculator(int bootStrapValue, PriorityType prioritiserType, double[] phenoScoreCache, int numFilteredGenes) {
        this.bootStrapValue = bootStrapValue;
        this.phenoScoreCache = phenoScoreCache;
        this.phenoScoreCacheSize = phenoScoreCache.length;
        this.priorityTypes = Set.of(prioritiserType);
        this.numFilteredGenes = numFilteredGenes;
    }

    /**
     * Creates a {@link CombinedScorePvalueCalculator} with a randomly generated set of phenotype scores - the size of
     * which is specified by the user.
     * @param bootStrapValue number of iterations of the bootstrap process.
     * @param numScores number of scores to randomly generate
     * @return a {@link CombinedScorePvalueCalculator} instance
     * @since 13.1.0
     */
    public static CombinedScorePvalueCalculator withRandomScores(int bootStrapValue, long numScores, int numFilteredGenes) {
        logger.info("Setting up phenotype score cache on {} random scores", numScores);
        var phenoScoreCache = ThreadLocalRandom.current().doubles(numScores).toArray();
        return new CombinedScorePvalueCalculator(bootStrapValue, PriorityType.NONE, phenoScoreCache, numFilteredGenes);
    }

    public static CombinedScorePvalueCalculator of(int bootStrapValue, Prioritiser<?> prioritiser, List<String> sampleHpoIds, List<Gene> unscoredGenes, int numFilteredGenes) {
        Objects.requireNonNull(prioritiser);
        Objects.requireNonNull(sampleHpoIds);
        Objects.requireNonNull(unscoredGenes);
        logger.info("Setting up phenotype score cache on {} genes", unscoredGenes.size());
        var phenoScoreCache = generatePhenoScoreCache(prioritiser, sampleHpoIds, unscoredGenes);
        logger.info("Bootstrapping combined scores for {} filtered genes (bootstrap value = {})", numFilteredGenes, bootStrapValue);
        return new CombinedScorePvalueCalculator(bootStrapValue, prioritiser.getPriorityType(), phenoScoreCache, numFilteredGenes);
    }

    private static double[] generatePhenoScoreCache(Prioritiser<?> prioritiser, List<String> hpoIds, List<Gene> genes) {
        prioritiser.prioritizeGenes(hpoIds, genes);
        PriorityType priorityType = prioritiser.getPriorityType();
        return genes.stream()
                .mapToDouble(gene -> {
                    PriorityResult priorityResult = gene.getPriorityResult(priorityType);
                    if (priorityResult != null) {
                        return priorityResult.getScore();
                    }
                    return 0.0;
                })
                .toArray();
    }

    double calculatePvalueFromCombinedScore(double combinedScore) {
        if (combinedScore == 0 || phenoScoreCacheSize == 0) {
            return 1d;
        }
        int numHigherScores = 1;
        for (int i = 0; i < bootStrapValue; ++i) {
            for (int j = 0; j < numFilteredGenes; j++) {
                int index = ThreadLocalRandom.current().nextInt(phenoScoreCacheSize);
                double randomPhenoScore = phenoScoreCache[index];
                double randomVariantScore = ThreadLocalRandom.current().nextDouble();
                double randomCombined = GeneScorer.calculateCombinedScore(randomVariantScore, randomPhenoScore, priorityTypes);
                if (randomCombined >= combinedScore) {
                    ++numHigherScores;
                }
            }
        }

        return (double) numHigherScores / (bootStrapValue * numFilteredGenes);
    }

}
