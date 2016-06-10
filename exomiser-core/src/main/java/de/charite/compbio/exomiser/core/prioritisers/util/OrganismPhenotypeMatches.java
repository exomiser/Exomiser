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

package de.charite.compbio.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Stores the PhenotypeMatches for a set of query PhenotypeTerms for an Organism.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OrganismPhenotypeMatches {

    private static Logger logger = LoggerFactory.getLogger(OrganismPhenotypeMatches.class);

    private final Organism organism;
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches;
    private final Set<PhenotypeMatch> bestPhenotypeMatches;
    private final double bestMatchScore;
    private final double bestAverageScore;

    /**
     * @param organism             - The organism for which these PhenotypeMatches are associated.
     * @param termPhenotypeMatches - Map of query PhenotypeTerms and their corresponding PhenotypeMatches. If there is no match then an empty Set of PhenotypeMatches is expected.
     */
    public OrganismPhenotypeMatches(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        this.organism = organism;
        this.termPhenotypeMatches = ImmutableMap.copyOf(termPhenotypeMatches);
        this.bestPhenotypeMatches = makeBestPhenotypeMatches(termPhenotypeMatches);
        this.bestMatchScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).max().orElse(0d);
        this.bestAverageScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).average().orElse(0d);
    }

    private Set<PhenotypeMatch> makeBestPhenotypeMatches(Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        Map<PhenotypeTerm, PhenotypeMatch> bestMatches = new HashMap<>();

        for (Map.Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : termPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            for (PhenotypeMatch match : entry.getValue()) {
                ///todo: simple sort on score and pick top value
                double score = match.getScore();
                if (bestMatches.containsKey(queryTerm)) {
                    if (score > bestMatches.get(queryTerm).getScore()) {
                        bestMatches.put(queryTerm, match);
                    }
                } else {
                    bestMatches.put(queryTerm, match);
                }
            }
        }
        return ImmutableSet.copyOf(bestMatches.values());
    }

    public Organism getOrganism() {
        return organism;
    }

    public List<PhenotypeTerm> getQueryTerms() {
        return ImmutableList.copyOf(termPhenotypeMatches.keySet());
    }

    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches() {
        return termPhenotypeMatches;
    }

    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    public Map<String, PhenotypeMatch> getCompoundKeyIndexedPhenotypeMatches() {
        //'hpId + mpId' : phenotypeMatch
        Map<String, PhenotypeMatch> speciesPhenotypeMatches = new HashMap<>();

        for (Map.Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : termPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            String hpId = queryTerm.getId();
            for (PhenotypeMatch match : entry.getValue()) {
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                String mpId = matchTerm.getId();
                String matchIds = hpId + mpId;
                speciesPhenotypeMatches.put(matchIds, match);
            }
        }
        return ImmutableMap.copyOf(speciesPhenotypeMatches);
    }

    public double getBestMatchScore() {
        return bestMatchScore;
    }

    public double getBestAverageScore() {
        return bestAverageScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganismPhenotypeMatches)) return false;
        OrganismPhenotypeMatches that = (OrganismPhenotypeMatches) o;
        return Double.compare(that.bestMatchScore, bestMatchScore) == 0 &&
                Double.compare(that.bestAverageScore, bestAverageScore) == 0 &&
                organism == that.organism &&
                Objects.equals(termPhenotypeMatches, that.termPhenotypeMatches) &&
                Objects.equals(bestPhenotypeMatches, that.bestPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, termPhenotypeMatches, bestPhenotypeMatches, bestMatchScore, bestAverageScore);
    }

    @Override
    public String toString() {
        return "OrganismPhenotypeMatches{" +
                "organism=" + organism +
                ", bestMatchScore=" + bestMatchScore +
                ", bestAverageScore=" + bestAverageScore +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                '}';
    }
}
