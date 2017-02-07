/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.model.Organism;
import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.model.PhenotypeTerm;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TheoreticalModel {

    private final Organism organism;

    private final List<PhenotypeTerm> queryTerms;
    private final Set<PhenotypeMatch> bestPhenotypeMatches;
    private final double theoreticalMaxMatchScore;
    private final double theoreticalBestAvgScore;

    TheoreticalModel(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        this.organism = organism;
        this.queryTerms = ImmutableList.copyOf(termPhenotypeMatches.keySet());
        this.bestPhenotypeMatches = makeBestPhenotypeMatches(termPhenotypeMatches);
        this.theoreticalMaxMatchScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).max().orElse(0d);
        this.theoreticalBestAvgScore = calculateBestAverageScore(bestPhenotypeMatches, termPhenotypeMatches.keySet().size());
    }

    //calculates the average score of the best phenotype matches over all of query phenotypes, not just those with matches.
    private double calculateBestAverageScore(Set<PhenotypeMatch> bestPhenotypeMatches, int numQueryPhenotypes) {
        if (bestPhenotypeMatches.isEmpty()) {
            // otherwise get a NaN value that escalates to other scores and eventually throws an exception
            return 0;
        }
        return bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).sum() / numQueryPhenotypes;
    }

    private Set<PhenotypeMatch> makeBestPhenotypeMatches(Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        return termPhenotypeMatches.values()
                .stream()
                .map(bestPhenotypeMatch())
                .filter(Objects::nonNull)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Finds the best PhenotypeMatch for that phenotype term. This is the one with the highest score. OR returns a null
     */
    private Function<Set<PhenotypeMatch>, PhenotypeMatch> bestPhenotypeMatch() {
        return phenotypeMatches -> phenotypeMatches.stream()
                .sorted(Comparator.comparingDouble(PhenotypeMatch::getScore).reversed())
                .findFirst()
                .orElse(null);
    }

    public Organism getOrganism() {
        return organism;
    }

    public List<PhenotypeTerm> getQueryTerms() {
        return queryTerms;
    }

    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    public double getMaxMatchScore() {
        return theoreticalMaxMatchScore;
    }

    public double getBestAvgScore() {
        return theoreticalBestAvgScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TheoreticalModel)) return false;
        TheoreticalModel that = (TheoreticalModel) o;
        return Double.compare(that.theoreticalMaxMatchScore, theoreticalMaxMatchScore) == 0 &&
                Double.compare(that.theoreticalBestAvgScore, theoreticalBestAvgScore) == 0 &&
                organism == that.organism &&
                Objects.equals(bestPhenotypeMatches, that.bestPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, bestPhenotypeMatches, theoreticalMaxMatchScore, theoreticalBestAvgScore);
    }

    @Override
    public String toString() {
        return "TheoreticalModel{" +
                "organism=" + organism +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                ", bestMatchScore=" + theoreticalMaxMatchScore +
                ", bestAverageScore=" + theoreticalBestAvgScore +
                '}';
    }
}
