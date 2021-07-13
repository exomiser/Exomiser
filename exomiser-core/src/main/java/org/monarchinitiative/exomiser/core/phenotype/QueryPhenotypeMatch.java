/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.Function;

/**
 * Immutable class representing the raw phenotype similarity matches between the input query phenotype terms (HPO) and
 * the best possible matches for the organism in question. In the case of matching human HP-HP terms this will likely be
 * a set of self matches, assuming the terms are above the IC threshold of 2.5. For other organisms the matches will be
 * between the HP terms and the relevant organism phenotype ontology e.g. MP or ZP.
 *
 * This class pre-calculates some of the constant values for a given input phenotype required for computing the
 * phenodigm combined score against a {@link Model} using a {@link PhenodigmModelScorer} supplied with the relevant
 * species-specific {@link PhenotypeMatcher}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class QueryPhenotypeMatch {

    private final Organism organism;
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> queryTermPhenotypeMatches;

    private final List<PhenotypeTerm> queryTerms;
    private final Set<PhenotypeMatch> bestPhenotypeMatches;
    private final double theoreticalMaxMatchScore;
    private final double theoreticalBestAvgScore;

    QueryPhenotypeMatch(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> queryTermPhenotypeMatches) {
        this.organism = organism;
        this.queryTermPhenotypeMatches = ImmutableMap.copyOf(queryTermPhenotypeMatches);

        this.queryTerms = List.copyOf(this.queryTermPhenotypeMatches.keySet());
        this.bestPhenotypeMatches = makeBestPhenotypeMatches(this.queryTermPhenotypeMatches);
        this.theoreticalMaxMatchScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).max().orElse(0d);
        this.theoreticalBestAvgScore = calculateBestAverageScore(bestPhenotypeMatches, queryTerms.size());
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
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Finds the best PhenotypeMatch for that phenotype term. This is the one with the highest score. OR returns a null
     */
    private Function<Set<PhenotypeMatch>, PhenotypeMatch> bestPhenotypeMatch() {
        return phenotypeMatches -> phenotypeMatches.stream().max(Comparator.comparingDouble(PhenotypeMatch::getScore))
                .orElse(null);
    }

    public Organism getOrganism() {
        return organism;
    }

    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getQueryTermPhenotypeMatches() {
        return queryTermPhenotypeMatches;
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
        if (o == null || getClass() != o.getClass()) return false;
        QueryPhenotypeMatch that = (QueryPhenotypeMatch) o;
        return Double.compare(that.theoreticalMaxMatchScore, theoreticalMaxMatchScore) == 0 &&
                Double.compare(that.theoreticalBestAvgScore, theoreticalBestAvgScore) == 0 &&
                organism == that.organism &&
                Objects.equals(queryTermPhenotypeMatches, that.queryTermPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, queryTermPhenotypeMatches, theoreticalMaxMatchScore, theoreticalBestAvgScore);
    }

    @Override
    public String toString() {
        return "QueryPhenotypeMatch{" +
                "organism=" + organism +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                ", bestMatchScore=" + theoreticalMaxMatchScore +
                ", bestAverageScore=" + theoreticalBestAvgScore +
                '}';
    }
}
