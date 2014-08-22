/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

/**
 *
 * @author jj8
 */
public class PhredScore {
    
    private final float score;

    public PhredScore(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Float.floatToIntBits(this.score);
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
        final PhredScore other = (PhredScore) obj;
        if (Float.floatToIntBits(this.score) != Float.floatToIntBits(other.score)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "PHRED: " + score;
    }
    
    
}
