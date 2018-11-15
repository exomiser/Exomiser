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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;


/**
 * Stores the PhenotypeMatches for a set of query PhenotypeTerms for an Organism. These represent the best possible matches
 * a {@link Model} could have.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class CrossSpeciesPhenotypeMatcher implements PhenotypeMatcher {

    private final QueryPhenotypeMatch queryPhenotypeMatch;

    private final Set<String> matchedOrganismPhenotypeIds;
    private final Set<String> matchedQueryPhenotypeIds;

    private final Map<String, PhenotypeMatch> mappedTerms;

    /**
     * @param organism                  - The organism for which these PhenotypeMatches are associated.
     * @param queryTermPhenotypeMatches - Map of query PhenotypeTerms and their corresponding PhenotypeMatches. If there is no match then an empty Set of PhenotypeMatches is expected.
     */
    static CrossSpeciesPhenotypeMatcher of(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> queryTermPhenotypeMatches) {
        QueryPhenotypeMatch queryPhenotypeMatch = new QueryPhenotypeMatch(organism, queryTermPhenotypeMatches);
        return of(queryPhenotypeMatch);
    }

    static CrossSpeciesPhenotypeMatcher of(QueryPhenotypeMatch queryPhenotypeMatch) {
        return new CrossSpeciesPhenotypeMatcher(queryPhenotypeMatch);
    }

    private CrossSpeciesPhenotypeMatcher(QueryPhenotypeMatch queryPhenotypeMatch) {
        this.queryPhenotypeMatch = queryPhenotypeMatch;

        Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches = queryPhenotypeMatch.getQueryTermPhenotypeMatches();

        this.matchedOrganismPhenotypeIds = termPhenotypeMatches.values()
                .stream()
                .flatMap(set -> set.stream().map(PhenotypeMatch::getMatchPhenotypeId))
                .collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSet));

        this.matchedQueryPhenotypeIds = queryPhenotypeMatch.getBestPhenotypeMatches()
                .stream()
                .map(PhenotypeMatch::getQueryPhenotypeId)
                .collect(Collectors.toCollection(TreeSet::new));

        //'hpId + mpId' : phenotypeMatch
        this.mappedTerms = termPhenotypeMatches.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(
                        toMap(makeKey(), Function.identity()),
                        Collections::unmodifiableMap));
    }

    private Function<PhenotypeMatch, String> makeKey() {
        return match -> String.join("", match.getQueryPhenotypeId() + match.getMatchPhenotypeId());
    }

    @Override
    public Organism getOrganism() {
        return queryPhenotypeMatch.getOrganism();
    }

    @Override
    public List<PhenotypeTerm> getQueryTerms() {
        return queryPhenotypeMatch.getQueryTerms();
    }

    @Override
    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches() {
        return queryPhenotypeMatch.getQueryTermPhenotypeMatches();
    }

    @Override
    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        return queryPhenotypeMatch.getBestPhenotypeMatches();
    }

    @Override
    public QueryPhenotypeMatch getQueryPhenotypeMatch() {
        return queryPhenotypeMatch;
    }

    /**
     * Calculates the best forward and reverse matches for a given set of model phenotypes against the sub-graph of matches
     * for the query phenotypes against this organism. The best forward and reverse matches are not necessarily the same.
     *
     * @param modelPhenotypes
     * @return
     */
    @Override
    public PhenodigmMatchRawScore matchPhenotypeIds(List<String> modelPhenotypes) {
        // Could be HP, MP or ZP id
        List<String> matchedModelPhenotypeIds = new ArrayList<>();
        for (String modelPhenotype : modelPhenotypes) {
            if (matchedOrganismPhenotypeIds.contains(modelPhenotype)) {
                matchedModelPhenotypeIds.add(modelPhenotype);
            }
        }

        // return values
        double maxModelMatchScore = 0;
        double sumModelBestMatchScores = 0;
        final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms = new LinkedHashMap<>();

        // calculate forwards hp-mp scores
        for (String hpId : matchedQueryPhenotypeIds) {
            double bestMatchScore = 0;
            for (String mpId : matchedModelPhenotypeIds) {
                String matchIds = hpId + mpId;
                if (mappedTerms.containsKey(matchIds)) {
                    PhenotypeMatch match = mappedTerms.get(matchIds);
                    double matchScore = match.getScore();
                    // identify best match
                    bestMatchScore = Math.max(matchScore, bestMatchScore);
                    if (matchScore > 0) {
                        addMatchIfAbsentOrBetterThanCurrent(match, bestPhenotypeMatchForTerms);
                    }
                }
            }
            if (bestMatchScore > 0) {
                sumModelBestMatchScores += bestMatchScore;
                maxModelMatchScore = Math.max(bestMatchScore, maxModelMatchScore);
            }
        }
        // calculate reciprocal mp-hp scores
        for (String mpId : matchedModelPhenotypeIds) {
            double bestMatchScore = 0;
            for (String hpId : matchedQueryPhenotypeIds) {
                String matchIds = hpId + mpId;
                if (mappedTerms.containsKey(matchIds)) {
                    PhenotypeMatch match = mappedTerms.get(matchIds);
                    double matchScore = match.getScore();
                    // identify best match
                    bestMatchScore = Math.max(matchScore, bestMatchScore);
                    if (matchScore > 0) {
                        addMatchIfAbsentOrBetterThanCurrent(match, bestPhenotypeMatchForTerms);
                    }
                }
            }
            if (bestMatchScore > 0) {
                sumModelBestMatchScores += bestMatchScore;
                maxModelMatchScore = Math.max(bestMatchScore, maxModelMatchScore);
            }
        }

        return new PhenodigmMatchRawScore(maxModelMatchScore, sumModelBestMatchScores, matchedModelPhenotypeIds, ImmutableList
                .copyOf(bestPhenotypeMatchForTerms.values()));
    }

    private void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match, Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm) || bestPhenotypeMatchForTerms.get(matchQueryTerm)
                .getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
    }

    /**
     * @param modelPhenotypes
     * @return
     */
    List<PhenotypeMatch> calculateBestForwardAndReciprocalMatches(List<String> modelPhenotypes) {
        List<String> matchedModelPhenotypeIds = modelPhenotypes.stream()
                .filter(matchedOrganismPhenotypeIds::contains)
                .collect(toList());

        //loop - 191, 206, 211, 293, 260, 221, 229, 247, 203, 204. (226 ms)
        //stream - 1208, 773, 1231, 799, 655, 566, 467, 1037, 792, 722. (825 ms)
        //This takes ~0.7 secs compared to ~0.2 secs using the original loop implementation, although it is now returning
        //the values. Can it be made faster? Do we care?
        List<PhenotypeMatch> forwardMatches = matchedQueryPhenotypeIds.stream()
                .map(hp -> matchedModelPhenotypeIds.stream()
                        .map(mp -> String.join("", hp, mp))
                        .map(mappedTerms::get)
                        .filter(Objects::nonNull)
                        .max(comparingDouble(PhenotypeMatch::getScore)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        //CAUTION!!! This looks very similar to the forward match statement but there are several important differences...
        List<PhenotypeMatch> reciprocalMatches = matchedModelPhenotypeIds.stream()
                .map(mp -> matchedQueryPhenotypeIds.stream()
                        .map(hp -> String.join("", hp, mp))
                        .map(mappedTerms::get)
                        .filter(Objects::nonNull)
                        .max(comparingDouble(PhenotypeMatch::getScore)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        //why turn the lists back into streams when they were streams to start with?
        return Stream.concat(forwardMatches.stream(), reciprocalMatches.stream())
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Calculates the best PhenotypeMatches grouped by query PhenotypeTerm from the input list of PhenotypeMatches.     *
     *
     * @param bestForwardAndReciprocalMatches
     * @return A list of the best PhenotypeMatches grouped by query PhenotypeTerm from the input list of PhenotypeMatches
     */
    List<PhenotypeMatch> calculateBestPhenotypeMatchesByTerm(List<PhenotypeMatch> bestForwardAndReciprocalMatches) {
        Map<PhenotypeTerm, Optional<PhenotypeMatch>> bestOptionalPhenotypeMatchForTerms = bestForwardAndReciprocalMatches
                .stream()
                .collect(groupingBy(PhenotypeMatch::getQueryPhenotype, maxBy(comparingDouble(PhenotypeMatch::getScore))));

        return bestOptionalPhenotypeMatchForTerms.values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossSpeciesPhenotypeMatcher that = (CrossSpeciesPhenotypeMatcher) o;
        return Objects.equals(queryPhenotypeMatch, that.queryPhenotypeMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryPhenotypeMatch);
    }

    @Override
    public String toString() {
        return "CrossSpeciesPhenotypeMatcher{" +
                "organism=" + queryPhenotypeMatch.getOrganism() +
                ", termPhenotypeMatches=" + queryPhenotypeMatch.getQueryTermPhenotypeMatches() +
                '}';
    }

}
