package org.monarchinitiative.exomiser.core.analysis.util;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;

/**
 * @since 13.1.0
 */
public class CombinedScorePvalueCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CombinedScorePvalueCalculator.class);
    private final Set<PriorityType> priorityTypes;
    private final double[] bootstrappedScores;

    private CombinedScorePvalueCalculator(PriorityType prioritiserType, double[] phenoScoreCache) {
        // Create a constant-sized population of 500K combined scores so that the combined score p-value calculation
        // always runs in an acceptable time for any analysis combination (WGS/ES, FULL/PASS_ONLY). The value of 500K
        // was chosen to be suitably large that either an exome or genome's worth of returned variants (hundreds to thousands)
        // will have a significant population to be compared with.
        this.priorityTypes = Set.of(prioritiserType);
        long population = 500_000L;
        // TODO: Replace Random with java.util.random.RandomGenerator when upgrading java version >= 17
        this.bootstrappedScores = phenoScoreCache.length == 0 ? new double[]{} : new Random()
                .ints(population, 0, phenoScoreCache.length)
                .parallel()
                .mapToDouble(index -> {
                    double randomPhenoScore = phenoScoreCache[index];
                    double randomVariantScore = ThreadLocalRandom.current().nextDouble();
                    return GeneScorer.calculateCombinedScore(randomVariantScore, randomPhenoScore, priorityTypes);
                })
                .toArray();
        logger.debug("Created bootstrapped population of {}", bootstrappedScores.length);
        SummaryStatistics summaryStatistics = new SummaryStatistics(bootstrappedScores);
        if (logger.isDebugEnabled()) {
            logger.debug("Combined score distribution:\n{}", summaryStatistics.asciiDistribution(bootstrappedScores));
        }
        logger.debug("Combined score {}", summaryStatistics);
    }

    /**
     * Creates a {@link CombinedScorePvalueCalculator} with a randomly generated set of phenotype scores - the size of
     * which is specified by the user. Note that this will create a normal distribution of scores with a mean of ~0.5.
     *
     * @param bootStrapValue number of iterations of the bootstrap process.
     * @param numScores      number of scores to randomly generate
     * @return a {@link CombinedScorePvalueCalculator} instance
     * @since 13.1.0
     */
    public static CombinedScorePvalueCalculator withRandomScores(int bootStrapValue, long numScores, int numFilteredGenes) {
        logger.info("Setting up phenotype score cache on {} random scores", numScores);
        var phenoScoreCache = ThreadLocalRandom.current().doubles(numScores).toArray();
        return new CombinedScorePvalueCalculator(PriorityType.NONE, phenoScoreCache);
    }

    public static CombinedScorePvalueCalculator of(int bootStrapValue, Prioritiser<?> prioritiser, List<String> sampleHpoIds, List<Gene> unscoredGenes, int numFilteredGenes) {
        Objects.requireNonNull(prioritiser);
        Objects.requireNonNull(sampleHpoIds);
        Objects.requireNonNull(unscoredGenes);
        logger.debug("Setting up phenotype score cache on {} genes", unscoredGenes.size());
        var phenoScoreCache = generatePhenoScoreCache(prioritiser, sampleHpoIds, unscoredGenes);
        logger.debug("Creating bootstrapped combined scores...");
        return new CombinedScorePvalueCalculator(prioritiser.getPriorityType(), phenoScoreCache);
    }

    /**
     * Returns a no-op p-value calculator which will always return the value 1.0 for any input value.
     *
     * @since 13.2.0
     */
    public static CombinedScorePvalueCalculator noOpCombinedScorePvalueCalculator() {
        return NoOpPvalueScorer.instance();
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
        // this is run in a hot loop for the gene score calculation, so pre-compute the population statistics on class
        // instantiation
        if (combinedScore == 0 || bootstrappedScores.length == 0) {
            return 1d;
        }
        int numHigherScores = 1;
        for (int i = 0; i < bootstrappedScores.length; ++i) {
            double randomCombined = bootstrappedScores[i];
            if (randomCombined >= combinedScore) {
                ++numHigherScores;
            }
        }

        return (double) numHigherScores / bootstrappedScores.length;
    }

    private static class NoOpPvalueScorer extends CombinedScorePvalueCalculator {

        private static final NoOpPvalueScorer INSTANCE = new NoOpPvalueScorer(PriorityType.NONE, new double[0]);

        private NoOpPvalueScorer(PriorityType prioritiserType, double[] phenoScoreCache) {
            super(prioritiserType, phenoScoreCache);
        }

        private static NoOpPvalueScorer instance() {
            return INSTANCE;
        }

        @Override
        double calculatePvalueFromCombinedScore(double combinedScore) {
            return 1d;
        }
    }

    /**
     * @since 13.2.0
     */
    public static class SummaryStatistics {
        private enum Z {
            Z_90("90%", 1.645),
            Z_95("95%", 1.960),
            Z_99("99%", 2.576),
            Z_99_5("99.5%", 2.807),
            Z_99_9("99.9%", 3.291);

            final String label;
            final double value;

            Z(String label, double value) {
                this.label = label;
                this.value = value;
            }
        }

        private static final Z Z_VALUE = Z.Z_95;
        private final int sampleSize;
        private final double mean;
        private final double variance;
        private final double sd;
        private final double ci;

        public SummaryStatistics(double[] values) {
            sampleSize = values.length;
            DoubleSummaryStatistics summaryStatistics = Arrays.stream(values).summaryStatistics();
            mean = summaryStatistics.getAverage();
            variance = Arrays.stream(values).flatMap(score -> DoubleStream.of(Math.pow((score - mean), 2))).average().orElse(0);
            // variance = sqrt(SD)
            sd = Math.pow(variance, 0.5);
            // so for 99.5% CI Z * (SD / sqrt(n)) where n = number of observations
            ci = Z_VALUE.value * (sd / Math.sqrt(sampleSize));
        }

        @Override
        public String toString() {
            return String.format("%s Confidence Interval: %.3f +-%.3f [%.3f, %.3f], SD: %f based on %d samples",
                    Z_VALUE.label, mean, ci, (mean - ci), (mean + ci), sd, sampleSize);
        }

        public String asciiDistribution(double[] values) {
            int[] bins = new int[10];
            for (double value : values) {
                for (int i = 0; i < bins.length; i++) {
                    // bins 0-0.1, 0.1-0.2...
                    if (value > (i / 10.0) && value <= ((i + 1) / 10.0)) {
                        bins[i]++;
                        break;
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder(String.format("|   score   |  count  |%n"));
            stringBuilder.append(String.format("  --------- | -------  %n"));
            for (int i = 0; i < bins.length; i++) {
                int count = bins[i];
                stringBuilder.append(String.format("  %.1f - %.1f | %s %n", (i / 10.0), ((i + 1) / 10.0), count));
            }
            return stringBuilder.toString();
        }
    }
}
