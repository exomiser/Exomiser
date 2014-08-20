package de.charite.compbio.exomiser.core.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter Variants on the basis of the predicted pathogenicity. This class
 * filters both on variant class (NONSENSE, MISSENSE, INTRONIC) etc., as well as
 * on the basis of MutationTaster/Polyphen2/SIFT scores for mutations.
 *
 * @author Peter N Robinson
 * @version 0.17 (3 February, 2014)
 *
 */
public class PathogenicityFilterScore implements FilterScore {

    private static final Logger logger = LoggerFactory.getLogger(PathogenicityFilterScore.class);

    /**
     * The overall value of the estimated pathogenicity of this mutation. Must
     * be a value between 0 and 1. 0: Completely sure it is nonpathogenic 1.0:
     * Completely sure it is pathogenic
     */
    private final float score;

    public PathogenicityFilterScore(float pathogenicityScore) {
        this.score = pathogenicityScore;
    }
    
    /**
     * @return return a float representation of the filter result [0..1]. Note
     * that 0 means predicted to be non-pathogenic, and 1.0 means maximally
     * pathogenic prediction.
     */
    @Override
    public float getScore() {
        return this.score;
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
        final PathogenicityFilterScore other = (PathogenicityFilterScore) obj;
        if (Float.floatToIntBits(this.score) != Float.floatToIntBits(other.score)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("Path score: %.3f", score);
    }
}
