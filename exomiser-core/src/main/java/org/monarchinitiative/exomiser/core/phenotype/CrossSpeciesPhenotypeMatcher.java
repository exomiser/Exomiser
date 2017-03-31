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

package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;


/**
 * Stores the PhenotypeMatches for a set of query PhenotypeTerms for an Organism. These represent the best possible matches
 * a {@link Model} could have.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CrossSpeciesPhenotypeMatcher implements PhenotypeMatcher {

    private static final Logger logger = LoggerFactory.getLogger(CrossSpeciesPhenotypeMatcher.class);

    private final Organism organism;
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches;

    private final QueryPhenotypeMatch queryPhenotypeMatch;

    private final Set<String> matchedOrganismPhenotypeIds;
    private final Set<String> matchedQueryPhenotypeIds;

    private final Map<String, PhenotypeMatch> mappedTerms;

    /**
     * @param organism - The organism for which these PhenotypeMatches are associated.
     * @param queryTermPhenotypeMatches - Map of query PhenotypeTerms and their corresponding PhenotypeMatches. If there is no match then an empty Set of PhenotypeMatches is expected.
     */
    //TODO: should the constructor simply take the QueryPhenotypeMatch? This is a bit odd as the queryPhenotypeMatch is required externally to this - could then remove getBestPhenotypeMatches and getQueryPhenotypeMatch from the interface?
    public CrossSpeciesPhenotypeMatcher(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> queryTermPhenotypeMatches) {
        this.organism = organism;
        this.termPhenotypeMatches = ImmutableMap.copyOf(queryTermPhenotypeMatches);

        this.queryPhenotypeMatch = new QueryPhenotypeMatch(this.organism, this.termPhenotypeMatches);

        this.matchedOrganismPhenotypeIds = queryTermPhenotypeMatches
                .values().stream()
                .flatMap(set -> set.stream().map(PhenotypeMatch::getMatchPhenotypeId))
                .collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSet));

        this.matchedQueryPhenotypeIds = queryTermPhenotypeMatches
                .keySet().stream()
                .map(PhenotypeTerm::getId)
                .collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSet));

        this.mappedTerms = getCompoundKeyIndexedPhenotypeMatches();
    }

    @Override
    public Organism getOrganism() {
        return organism;
    }

    @Override
    public List<PhenotypeTerm> getQueryTerms() {
        return ImmutableList.copyOf(termPhenotypeMatches.keySet());
    }

    @Override
    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches() {
        return termPhenotypeMatches;
    }

    private Map<String, PhenotypeMatch> getCompoundKeyIndexedPhenotypeMatches() {
        //'hpId + mpId' : phenotypeMatch
        return termPhenotypeMatches.values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(
                        toMap(makeKey(), Function.identity()),
                        Collections::unmodifiableMap));
    }

    private Function<PhenotypeMatch, String> makeKey() {
        return match -> String.join("", match.getQueryPhenotypeId() + match.getMatchPhenotypeId());
    }

    /**
     * Calculates the best forward and reverse matches for a given set of model phenotypes against the sub-graph of matches
     * for the query phenotypes against this organism. The best forward and reverse matches are not necessarily the same.
     * @param modelPhenotypes
     * @return
     */
    @Override
    public PhenodigmMatchRawScore matchPhenotypeIds(List<String> modelPhenotypes) {
        List<String> matchedModelPhenotypeIds = getMatchingPhenotypes(modelPhenotypes);

        //hpId
        Set<String> hpIdsWithPhenotypeMatch = new TreeSet<>();
        for (PhenotypeMatch match : getBestPhenotypeMatches()) {
            hpIdsWithPhenotypeMatch.add(match.getQueryPhenotypeId());
        }

        double maxModelMatchScore = 0;
        double sumModelBestMatchScores = 0;

        final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms = new LinkedHashMap<>();
        for (String hpId : hpIdsWithPhenotypeMatch) {
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
        // Reciprocal hits
        for (String mpId : matchedModelPhenotypeIds) {
            double bestMatchScore = 0;
            for (String hpId : hpIdsWithPhenotypeMatch) {
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
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm) || bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
    }

    private List<String> getMatchingPhenotypes(List<String> phenotypeIds) {
        ImmutableList.Builder<String> matchedPhenotypes = ImmutableList.builder();
        for (String phenotypeId : phenotypeIds) {
            if (matchedOrganismPhenotypeIds.contains(phenotypeId)) {
                matchedPhenotypes.add(phenotypeId);
            }
        }
        return matchedPhenotypes.build();
    }

    /**
     *
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

         return Stream.concat(forwardMatches.stream(), reciprocalMatches.stream())
                 .collect(ImmutableList.toImmutableList());
    }

    /**
     * Calculates the best PhenotypeMatches grouped by query PhenotypeTerm from the input list of PhenotypeMatches.     *
     * @param bestForwardAndReciprocalMatches
     * @return A list of the best PhenotypeMatches grouped by query PhenotypeTerm from the input list of PhenotypeMatches
     */
     List<PhenotypeMatch> calculateBestPhenotypeMatchesByTerm(List<PhenotypeMatch> bestForwardAndReciprocalMatches) {
        Map<PhenotypeTerm, Optional<PhenotypeMatch>> bestOptionalPhenotypeMatchForTerms = bestForwardAndReciprocalMatches.stream()
                .collect(groupingBy(PhenotypeMatch::getQueryPhenotype, maxBy(comparingDouble(PhenotypeMatch::getScore))));

        return bestOptionalPhenotypeMatchForTerms.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        return queryPhenotypeMatch.getBestPhenotypeMatches();
    }

    @Override
    public QueryPhenotypeMatch getQueryPhenotypeMatch() {
        return queryPhenotypeMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrossSpeciesPhenotypeMatcher)) return false;
        CrossSpeciesPhenotypeMatcher that = (CrossSpeciesPhenotypeMatcher) o;
        return organism == that.organism &&
                Objects.equals(termPhenotypeMatches, that.termPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, termPhenotypeMatches);
    }


    @Override
    public String toString() {
        return "CrossSpeciesPhenotypeMatcher{" +
                "organism=" + organism +
                ", termPhenotypeMatches=" + termPhenotypeMatches +
                '}';
    }

}
