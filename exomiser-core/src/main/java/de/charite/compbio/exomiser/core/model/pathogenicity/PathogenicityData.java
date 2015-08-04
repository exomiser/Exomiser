/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

import static de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Container for PathogenicityScore data about a variant.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityData {

    private final Map<PathogenicitySource, PathogenicityScore> pathogenicityScores;

    public PathogenicityData(PathogenicityScore... pathScore) {
        this(new HashSet<>(Arrays.asList(pathScore)));
    }

    public PathogenicityData(Set<PathogenicityScore> pathScores) {
        pathogenicityScores = new EnumMap(PathogenicitySource.class);
        for (PathogenicityScore pathScore : pathScores) {
            if (pathScore != null) {
                pathogenicityScores.put(pathScore.getSource(), pathScore);
            }
        }
    }

    public PolyPhenScore getPolyPhenScore() {
        return (PolyPhenScore) getPredictedScore(POLYPHEN);
    }

    public MutationTasterScore getMutationTasterScore() {
        return (MutationTasterScore) getPredictedScore(MUTATION_TASTER);
    }

    public SiftScore getSiftScore() {
        return (SiftScore) getPredictedScore(SIFT);
    }

    public CaddScore getCaddScore() {
        return (CaddScore) getPredictedScore(CADD);
    }

    public List<PathogenicityScore> getPredictedPathogenicityScores() {
        return new ArrayList(pathogenicityScores.values());
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
            //TODO: return a new NonPathogenicPathogenicityScore?
//            return new AbstractPathogenicityScore(VariantTypePathogenicityScores.NON_PATHOGENIC_SCORE, VARIANT_TYPE);
        }
        List<PathogenicityScore> knownPathScores = this.getPredictedPathogenicityScores();
        Collections.sort(knownPathScores);
        PathogenicityScore mostPathogenic = knownPathScores.get(0);
        return mostPathogenic;
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
        if (!Objects.equals(this.pathogenicityScores, other.pathogenicityScores)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PathogenicityData" + pathogenicityScores.values();
    }

}
