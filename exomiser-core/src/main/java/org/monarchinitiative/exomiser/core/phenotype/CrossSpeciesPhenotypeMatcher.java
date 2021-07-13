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

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

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
                .collect(toCollection(TreeSet::new));

        //'hpId + mpId' : phenotypeMatch
        this.mappedTerms = termPhenotypeMatches.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(
                        toMap(makeKey(), Function.identity()),
                        Collections::unmodifiableMap));
    }

    private Function<PhenotypeMatch, String> makeKey() {
        return match -> KeyGenerator.forwardKey(match.getQueryPhenotypeId(), match.getMatchPhenotypeId());
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
        return getPhenodigmMatchRawScoreNew(modelPhenotypes);
    }

    private PhenodigmMatchRawScore getPhenodigmMatchRawScoreOriginal(List<String> modelPhenotypes) {
        // Could be HP, MP or ZP id
        List<String> matchedModelPhenotypeIds = getMatchedModelPhenotypeIds(modelPhenotypes);

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

        return new PhenodigmMatchRawScore(maxModelMatchScore, sumModelBestMatchScores, matchedModelPhenotypeIds, List
                .copyOf(bestPhenotypeMatchForTerms.values()));
    }

    private PhenodigmMatchRawScore getPhenodigmMatchRawScoreNew(List<String> modelPhenotypes) {
        // Could be HP, MP or ZP id
        List<String> matchedModelPhenotypeIds = getMatchedModelPhenotypeIds(modelPhenotypes);

        // return values
        double maxModelMatchScore = 0;
        double sumModelBestMatchScores = 0;
        Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms = new LinkedHashMap<>();

        List<PhenotypeMatch> bestForwardReverseMatches = findBestForwardAndReverseMatches(matchedModelPhenotypeIds);
        for (PhenotypeMatch match : bestForwardReverseMatches) {
            double score = match.getScore();
            if (score > 0) {
                addMatchIfAbsentOrBetterThanCurrent(match, bestPhenotypeMatchForTerms);
                maxModelMatchScore = Math.max(score, maxModelMatchScore);
                sumModelBestMatchScores += score;
            }
        }

        return new PhenodigmMatchRawScore(maxModelMatchScore, sumModelBestMatchScores, matchedModelPhenotypeIds, List
                .copyOf(bestPhenotypeMatchForTerms.values()));
    }

    private List<String> getMatchedModelPhenotypeIds(List<String> modelPhenotypes) {
        List<String> matchedModelPhenotypeIds = new ArrayList<>();
        for (String modelPhenotype : modelPhenotypes) {
            if (matchedOrganismPhenotypeIds.contains(modelPhenotype)) {
                matchedModelPhenotypeIds.add(modelPhenotype);
            }
        }
        return matchedModelPhenotypeIds;
    }

    private void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match, Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        PhenotypeMatch currentBestMatch = bestPhenotypeMatchForTerms.get(matchQueryTerm);
        if (currentBestMatch == null || currentBestMatch.getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
    }

    List<PhenotypeMatch> findBestForwardAndReverseMatches(List<String> matchedModelPhenotypeIds) {
        // This is about 20% faster than the original implementation, is easier for the compiler to optimise and less
        // repetitive. Doing this with streams is about 2x slower.
        // Based on running the Pfeiffer sample three times in succession and averaging over 5 runs, time in ms
        // 9009 human, 37522 mouse, 3157 fish models.
        // Original implementation: 120,115,9 | 63,79,10 | 39,61,5
        // New implementation:       98,99,10 | 60,52,15 | 29,48,8
        List<PhenotypeMatch> bestForwardReverseMatches = new ArrayList<>();
        // find forward matches: query-model
        findForwardMatches(matchedModelPhenotypeIds, bestForwardReverseMatches);
        // find reverse matches: model-query
        findReverseMatches(matchedModelPhenotypeIds, bestForwardReverseMatches);
        return bestForwardReverseMatches;
    }

    private void findForwardMatches(List<String> matchedModelPhenotypeIds, List<PhenotypeMatch> bestForwardReverseMatches) {
        for (String hp : matchedQueryPhenotypeIds) {
            PhenotypeMatch best = getBestPhenotypeMatch(hp, matchedModelPhenotypeIds, KeyGenerator.FORWARD);
            if (best != null) {
                bestForwardReverseMatches.add(best);
            }
        }
    }

    private void findReverseMatches(List<String> matchedModelPhenotypeIds, List<PhenotypeMatch> bestForwardReverseMatches) {
        for (String mp : matchedModelPhenotypeIds) {
            PhenotypeMatch best = getBestPhenotypeMatch(mp, matchedQueryPhenotypeIds, KeyGenerator.REVERSE);
            if (best != null) {
                bestForwardReverseMatches.add(best);
            }
        }
    }

    @Nullable
    private PhenotypeMatch getBestPhenotypeMatch(String hp, Iterable<String> matchedModelPhenotypeIds, KeyGenerator keyGenerator) {
        PhenotypeMatch best = null;
        for (String mp : matchedModelPhenotypeIds) {
            // mapped terms are indexed with hp-model phenotype keys. The similarity is reflexive so we just need to generate the
            // correct key depending on which way were comparing the terms. This is handled by the relevant KeyGenerator.
            String key = keyGenerator.getKey(hp, mp);
            PhenotypeMatch match = mappedTerms.get(key);
            if (match != null) {
                if (best == null || match.getScore() > best.getScore()) {
                    best = match;
                }
            }
        }
        return best;
    }

    /**
     * Generates keys for the mappedTerms map by concatenating the input values in the order defined in the implementation.
     */
    private interface KeyGenerator {

        KeyGenerator FORWARD = new ForwardKeyGenerator();
        KeyGenerator REVERSE = new ReverseKeyGenerator();

        String getKey(String a, String b);

        static String forwardKey(String a, String b) {
            return FORWARD.getKey(a, b);
        }
    }

    private static class ForwardKeyGenerator implements KeyGenerator {
        @Override
        public String getKey(String a, String b) {
            return a + b;
        }
    }

    private static class ReverseKeyGenerator implements KeyGenerator {
        @Override
        public String getKey(String a, String b) {
            return b + a;
        }
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
