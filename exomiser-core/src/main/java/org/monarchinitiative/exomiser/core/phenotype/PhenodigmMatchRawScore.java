package org.monarchinitiative.exomiser.core.phenotype;

import java.util.List;
import java.util.Objects;

/**
 * DTO class for transferring data from {@link PhenotypeMatcher} to {@link PhenodigmModelScorer}.
 *
 * @since 8.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenodigmMatchRawScore {

    private double maxModelMatchScore;
    private double sumModelBestMatchScores;
    private List<String> matchingPhenotypes;
    private List<PhenotypeMatch> bestPhenotypeMatches;

    PhenodigmMatchRawScore(double maxModelMatchScore, double sumModelBestMatchScores, List<String> matchingPhenotypes, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.maxModelMatchScore = maxModelMatchScore;
        this.sumModelBestMatchScores = sumModelBestMatchScores;
        this.matchingPhenotypes = matchingPhenotypes;
        this.bestPhenotypeMatches = bestPhenotypeMatches;
    }

    double getMaxModelMatchScore() {
        return maxModelMatchScore;
    }

    double getSumModelBestMatchScores() {
        return sumModelBestMatchScores;
    }

    List<String> getMatchingPhenotypes() {
        return matchingPhenotypes;
    }

    List<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhenodigmMatchRawScore that = (PhenodigmMatchRawScore) o;
        return Double.compare(that.maxModelMatchScore, maxModelMatchScore) == 0 &&
                Double.compare(that.sumModelBestMatchScores, sumModelBestMatchScores) == 0 &&
                Objects.equals(matchingPhenotypes, that.matchingPhenotypes) &&
                Objects.equals(bestPhenotypeMatches, that.bestPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxModelMatchScore, sumModelBestMatchScores, matchingPhenotypes, bestPhenotypeMatches);
    }

    @Override
    public String toString() {
        return "PhenodigmMatchRawScore{" +
                "maxModelMatchScore=" + maxModelMatchScore +
                ", sumModelBestMatchScores=" + sumModelBestMatchScores +
                ", matchingPhenotypes=" + matchingPhenotypes +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                '}';
    }
}
