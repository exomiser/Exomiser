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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import java.util.*;

/**
 * Container for PathogenicityScore data about a variant.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityData {

    private static final PathogenicityData EMPTY_DATA = new PathogenicityData(Collections.emptyList());

    private final Map<PathogenicitySource, PathogenicityScore> pathogenicityScores;

    public static PathogenicityData of(PathogenicityScore... pathScore) {
        return new PathogenicityData(Arrays.asList(pathScore));
    }

    public static PathogenicityData of(Collection<PathogenicityScore> pathScores) {
        return new PathogenicityData(pathScores);
    }

    public static PathogenicityData empty() {
        return EMPTY_DATA;
    }

    private PathogenicityData(Collection<PathogenicityScore> pathScores) {
        pathogenicityScores = new EnumMap<>(PathogenicitySource.class);
        for (PathogenicityScore pathScore : pathScores) {
            if (pathScore != null) {
                pathogenicityScores.put(pathScore.getSource(), pathScore);
            }
        }
    }

    public PolyPhenScore getPolyPhenScore() {
        return (PolyPhenScore) getPredictedScore(PathogenicitySource.POLYPHEN);
    }

    public MutationTasterScore getMutationTasterScore() {
        return (MutationTasterScore) getPredictedScore(PathogenicitySource.MUTATION_TASTER);
    }

    public SiftScore getSiftScore() {
        return (SiftScore) getPredictedScore(PathogenicitySource.SIFT);
    }

    public CaddScore getCaddScore() {
        return (CaddScore) getPredictedScore(PathogenicitySource.CADD);
    }

    public RemmScore getRemmScore() {
        return (RemmScore) getPredictedScore(PathogenicitySource.REMM);
    }
    
    public List<PathogenicityScore> getPredictedPathogenicityScores() {
        return new ArrayList<>(pathogenicityScores.values());
    }

    public boolean isEmpty() {
        return pathogenicityScores.isEmpty();
    }

    public boolean hasPredictedScore() {
        return !pathogenicityScores.isEmpty();
    }

    public boolean hasPredictedScore(PathogenicitySource pathogenicitySource) {
        return pathogenicityScores.containsKey(pathogenicitySource);
    }

    /**
     * Returns the PathogenicityScore from the requested source, or null if not present.
     *
     * @param pathogenicitySource
     * @return
     */
    public PathogenicityScore getPredictedScore(PathogenicitySource pathogenicitySource) {
        return pathogenicityScores.get(pathogenicitySource);
    }

    /**
     * @return The most pathogenic score or null if there are no predicted scores
     */
    public PathogenicityScore getMostPathogenicScore() {
        if (pathogenicityScores.isEmpty()) {
            return null;
            //TODO: return a new NonPathogenicPathogenicityScore? Optional<PathogenicityScore>?
//            return new AbstractPathogenicityScore(VariantTypePathogenicityScores.NON_PATHOGENIC_SCORE, VARIANT_TYPE);
        }
        List<PathogenicityScore> knownPathScores = this.getPredictedPathogenicityScores();
        Collections.sort(knownPathScores);
        return knownPathScores.get(0);
//        return pathogenicityScores.values().stream().max(Comparator.naturalOrder()).orElse(null);
    }


    /**
     * @return the predicted pathogenicity score for this data set. The score is ranked from 0 (non-pathogenic) to 1 (highly pathogenic)
     */
    public float getScore() {
        if (pathogenicityScores.isEmpty()) {
            return VariantTypePathogenicityScores.NON_PATHOGENIC_SCORE;
        }

        PathogenicityScore mostPathogenicPredictedScore = getMostPathogenicScore();
        //Thanks to SIFT being about tolerance rather than pathogenicity, the score is inverted
        if (mostPathogenicPredictedScore.getClass() == SiftScore.class) {
            return 1 - mostPathogenicPredictedScore.getScore();
        }
        return mostPathogenicPredictedScore.getScore();
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.pathogenicityScores);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathogenicityData other = (PathogenicityData) obj;
        return Objects.equals(this.pathogenicityScores, other.pathogenicityScores);
    }

    @Override
    public String toString() {
        return "PathogenicityData" + pathogenicityScores.values();
    }

}
