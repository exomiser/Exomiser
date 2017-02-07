package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;

import java.util.List;
import java.util.Objects;

/**
 * DTO class for transferring data from {@link OrganismPhenotypeMatches} to {@link PhiveModelScorer}
 *
 * @since 8.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OrganismPhenotypeMatchScore {

    private double maxModelMatchScore;
    private double sumModelBestMatchScores;
    private List<String> matchingPhenotypes;
    private List<PhenotypeMatch> bestPhenotypeMatches;

    OrganismPhenotypeMatchScore(double maxModelMatchScore, double sumModelBestMatchScores, List<String> matchingPhenotypes, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.maxModelMatchScore = maxModelMatchScore;
        this.sumModelBestMatchScores = sumModelBestMatchScores;
        this.matchingPhenotypes = matchingPhenotypes;
        this.bestPhenotypeMatches = bestPhenotypeMatches;
    }

    public double getMaxModelMatchScore() {
        return maxModelMatchScore;
    }

    public double getSumModelBestMatchScores() {
        return sumModelBestMatchScores;
    }

    public List<String> getMatchingPhenotypes() {
        return matchingPhenotypes;
    }

    public List<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganismPhenotypeMatchScore that = (OrganismPhenotypeMatchScore) o;
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
        return "OrganismPhenotypeMatchScore{" +
                "maxModelMatchScore=" + maxModelMatchScore +
                ", sumModelBestMatchScores=" + sumModelBestMatchScores +
                ", matchingPhenotypes=" + matchingPhenotypes +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                '}';
    }
}
