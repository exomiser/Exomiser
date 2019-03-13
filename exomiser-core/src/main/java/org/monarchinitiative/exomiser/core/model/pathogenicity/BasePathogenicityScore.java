/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class BasePathogenicityScore implements PathogenicityScore {

    protected final PathogenicitySource source;
    protected final float score;

    BasePathogenicityScore(PathogenicitySource source, float score) {
        checkBounds(source, score);
        this.source = source;
        this.score = score;
    }

    private static void checkBounds(PathogenicitySource source, float score) {
        if (score < 0f || score > 1f) {
            String message = String.format("%s score of %.3f is out of range. Must be in the range of 0.0 - 1.0", source, score);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public PathogenicitySource getSource() {
        return source;
    }

    @Override
    public float getScore() {
        return score;
    }


    @JsonIgnore
    @Override
    public float getRawScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasePathogenicityScore)) return false;
        BasePathogenicityScore that = (BasePathogenicityScore) o;
        return Float.compare(that.score, score) == 0 &&
                source == that.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, score);
    }

    @Override
    public String toString() {
        return String.format("%s: %.3f", source, score);
    }
}
