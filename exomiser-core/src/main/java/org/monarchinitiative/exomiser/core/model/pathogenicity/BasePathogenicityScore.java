/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.pathogenicity;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class BasePathogenicityScore implements PathogenicityScore {

    //Higher scores are more likely pathogenic so this is the reverse of what's normal (we're using a probablility of pathogenicity)
    //comparable requires a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    static final int MORE_PATHOGENIC = -1;
    static final int EQUALS = 0;
    static final int LESS_PATHOGENIC = 1;

    protected final float score;
    protected final PathogenicitySource source;

    BasePathogenicityScore(PathogenicitySource source, float score) {
        this.score = score;
        this.source = source;
    }

    @Override
    public float getScore() {
        return score;
    }

    @Override
    public PathogenicitySource getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, source);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PathogenicityScore)) {
            return false;
        }
        final BasePathogenicityScore other = (BasePathogenicityScore) o;

        if (source != other.source) {
            return false;
        }

        return Float.compare(other.score, score) == 0;
    }

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

    @Override
    public String toString() {
        return String.format("%s: %.3f", source, score);
    }
}
