/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.pathogenicity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for PathogenicityScore data about a variant.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityData {

    private final PolyPhenScore polyPhenScore;
    private final MutationTasterScore mutationTasterScore;
    private final SiftScore siftScore;
    private final CaddScore caddScore;
    private final List<PathogenicityScore> knownPathogenicityScores;

    public PathogenicityData(PolyPhenScore polyPhenScore, MutationTasterScore mutationTasterScore, SiftScore siftScore, CaddScore caddScore) {
        this.polyPhenScore = polyPhenScore;
        this.mutationTasterScore = mutationTasterScore;
        this.siftScore = siftScore;
        this.caddScore = caddScore;
        knownPathogenicityScores = new ArrayList<>();

        if (polyPhenScore != null) {
            knownPathogenicityScores.add(polyPhenScore);
        }
        if (mutationTasterScore != null) {
            knownPathogenicityScores.add(mutationTasterScore);
        }
        if (siftScore != null) {
            knownPathogenicityScores.add(siftScore);
        }
        //we're not using CaddRaw or Cadd for the pathogenicity filtering yet so
        //enabling it here will cause serious scoring issues with the overal scores unless this
        //is taken into account by the PathogenicityFilter
//        if (caddScore != null) {
//            knownPathogenicityScores.add(caddScore);
//        }

    }

    public PolyPhenScore getPolyPhenScore() {
        return polyPhenScore;
    }

    public MutationTasterScore getMutationTasterScore() {
        return mutationTasterScore;
    }

    public SiftScore getSiftScore() {
        return siftScore;
    }

    public CaddScore getCaddScore() {
        return caddScore;
    }

    public List<PathogenicityScore> getKnownPathogenicityScores() {
        return new ArrayList(knownPathogenicityScores);
    }

    public boolean hasPredictedScore() {
        return !knownPathogenicityScores.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.polyPhenScore);
        hash = 29 * hash + Objects.hashCode(this.mutationTasterScore);
        hash = 29 * hash + Objects.hashCode(this.siftScore);
        hash = 29 * hash + Objects.hashCode(this.caddScore);
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
        if (!Objects.equals(this.polyPhenScore, other.polyPhenScore)) {
            return false;
        }
        if (!Objects.equals(this.mutationTasterScore, other.mutationTasterScore)) {
            return false;
        }
        if (!Objects.equals(this.siftScore, other.siftScore)) {
            return false;
        }
        if (!Objects.equals(this.caddScore, other.caddScore)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("PathogenicityData{%s, %s, %s, %s}", polyPhenScore, mutationTasterScore, siftScore, caddScore);
    }

}
