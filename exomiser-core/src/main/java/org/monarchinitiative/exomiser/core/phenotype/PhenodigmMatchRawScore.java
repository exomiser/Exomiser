/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * DTO class for transferring data from {@link PhenotypeMatcher} to {@link PhenodigmModelScorer}.
 *
 * @since 8.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PhenodigmMatchRawScore {

    private final double maxModelMatchScore;
    private final double sumModelBestMatchScores;
    private final List<String> matchingPhenotypes;
    private final List<PhenotypeMatch> bestPhenotypeMatches;

    PhenodigmMatchRawScore(double maxModelMatchScore, double sumModelBestMatchScores, List<String> matchingPhenotypes, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.maxModelMatchScore = maxModelMatchScore;
        this.sumModelBestMatchScores = sumModelBestMatchScores;
        this.matchingPhenotypes = ImmutableList.copyOf(matchingPhenotypes);
        this.bestPhenotypeMatches = ImmutableList.copyOf(bestPhenotypeMatches);
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
