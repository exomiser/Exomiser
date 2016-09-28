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

import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TheoreticalModel {

    private final Organism organism;
    //These could be pulled out into a BestTheoreticalModel and then other Models can be compared to this.
    private final Set<PhenotypeMatch> bestPhenotypeMatches;
    private final double theoreticalMaxMatchScore;
    private final double theoreticalBestAvgScore;

    TheoreticalModel(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        this.organism = organism;
        this.bestPhenotypeMatches = makeBestPhenotypeMatches(termPhenotypeMatches);
        this.theoreticalMaxMatchScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).max().orElse(0d);
        this.theoreticalBestAvgScore = bestPhenotypeMatches.stream().mapToDouble(PhenotypeMatch::getScore).average().orElse(0d);
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

    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    public double getMaxMatchScore() {
        return theoreticalMaxMatchScore;
    }

    public double getBestAvgScore() {
        return theoreticalBestAvgScore;
    }

    public double compare(double modelMaxMatchScore, double modelBestAvgScore) {
        // calculate combined score
        if (modelMaxMatchScore != 0) {
            double combinedScore = 50 * (modelMaxMatchScore / theoreticalMaxMatchScore + modelBestAvgScore / theoreticalBestAvgScore);
            if (combinedScore > 100) {
                combinedScore = 100;
            }
            return combinedScore / 100;
        }
        return 0;
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
