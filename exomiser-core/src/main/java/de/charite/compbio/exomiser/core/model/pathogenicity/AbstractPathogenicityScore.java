/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AbstractPathogenicityScore implements PathogenicityScore {

    protected final float score;

    public AbstractPathogenicityScore(float score) {
        this.score = score;
    }

    @Override
    public float getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Float.floatToIntBits(this.score);
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
        final AbstractPathogenicityScore other = (AbstractPathogenicityScore) obj;
        if (Float.floatToIntBits(this.score) != Float.floatToIntBits(other.score)) {
            return false;
        }
        return true;
    }

    //Higher scores are more likely pathogenic so this is the reverse of what's normal (we're using a probablility of pathogenicity)
    //comparable requires a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    protected static final int MORE_PATHOGENIC = -1;
    protected static final int EQUALS = 0;
    protected static final int LESS_PATHOGENIC = 1;

    /**
     * For the purposes of this comparator scores are ranked on a scale of 0 to
     * 1 where 0 is considered probably non-pathogenic and 1 probably
     * pathogenic. The comparator will rank the more pathogenic
     * PathogenicityScore higher than a less pathogenic score such that in a
     * sorted list the most pathogenic score will come first.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(PathogenicityScore other) {
        float otherScore = other.getScore();
        float thisScore = score;
        //remember if we're comparing this against a SIFT score we need to invert the
        //SIFT score otherwise the comparison will be inverted
        if (other.getClass() == SiftScore.class) {
            otherScore = 1f - other.getScore();
        }
        if (this.getClass() == SiftScore.class) {
            thisScore = 1f - score;
        }

        if (thisScore == otherScore) {
            return EQUALS;
        }
        if (thisScore > otherScore) {
            return MORE_PATHOGENIC;
        }
        return LESS_PATHOGENIC;
    }
}
