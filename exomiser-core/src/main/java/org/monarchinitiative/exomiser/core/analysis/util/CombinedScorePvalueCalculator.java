package org.monarchinitiative.exomiser.core.analysis.util;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CombinedScorePvalueCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CombinedScorePvalueCalculator.class);

    private static final Random random = new Random();

    private final int bootStrapNum;
    private final double[] phenoScoreCache;
    private final int phenoScoreCacheSize;
    private final Set<PriorityType> priorityTypes;

    CombinedScorePvalueCalculator(int bootStrapNum, PriorityType prioritiserType, double[] phenoScoreCache) {
        this.bootStrapNum = bootStrapNum;
        this.phenoScoreCache = phenoScoreCache;
        this.phenoScoreCacheSize = phenoScoreCache.length;
        this.priorityTypes = Set.of(prioritiserType);
    }

    /**
     * Creates a {@link CombinedScorePvalueCalculator} with a randomly generated set of phenotype scores - the size of
     * which is specified by the user.
     * @param bootStrapNum number of iterations of the bootstrap process.
     * @param numScores number of scores to randomly generate
     * @return a {@link CombinedScorePvalueCalculator} instance
     * @since 13.1.0
     */
    public static CombinedScorePvalueCalculator withRandomScores(int bootStrapNum, long numScores) {
        logger.info("Setting up phenotype score cache on {} random scores", numScores);
        var phenoScoreCache = random.doubles(numScores).toArray();
        return new CombinedScorePvalueCalculator(bootStrapNum, PriorityType.NONE, phenoScoreCache);
    }

    public static CombinedScorePvalueCalculator of(int bootStrapNum, Prioritiser<?> prioritiser, List<String> sampleHpoIds, List<Gene> unscoredGenes) {
        Objects.requireNonNull(prioritiser);
        Objects.requireNonNull(sampleHpoIds);
        Objects.requireNonNull(unscoredGenes);
        logger.info("Setting up phenotype score cache on {} genes", unscoredGenes.size());
        var phenoScoreCache = generatePhenoScoreCache(prioritiser, sampleHpoIds, unscoredGenes);
        return new CombinedScorePvalueCalculator(bootStrapNum, prioritiser.getPriorityType(), phenoScoreCache);
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
        int numHigherScores = 0;
        for (int i = 0; i < bootStrapNum; ++i) {
            int index = random.nextInt(phenoScoreCacheSize);
            double randomPhenoScore = phenoScoreCache[index];
            double randomVariantScore = random.nextDouble();
            double randomCombined = GeneScorer.calculateCombinedScore(randomVariantScore, randomPhenoScore, priorityTypes);
            if (randomCombined > combinedScore) {
                ++numHigherScores;
            }
        }
        return (double) numHigherScores / (double) bootStrapNum;
    }

}
