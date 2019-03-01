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

import java.util.Objects;

/**
 * Class for use with {@link PathogenicityScore} cases which do not fit the standard 0-1 scale where 0 is considered not
 * pathogenic and 1 to be highly pathogenic. Examples of this are CADD, SIFT and MPC.
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ScaledPathogenicityScore extends BasePathogenicityScore {

    protected final float rawScore;

    /**
     *
     * @param source    source of the data
     * @param rawScore  the raw or unscaled score, as reported b the source
     * @param scaledScore   the raw score, scaled to fit the 0 - 1 scaling
     */
    ScaledPathogenicityScore(PathogenicitySource source, float rawScore, float scaledScore) {
        super(source, scaledScore);
        this.rawScore = rawScore;
    }

    @Override
    public PathogenicitySource getSource() {
        return source;
    }

    @Override
    public float getRawScore() {
        return rawScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScaledPathogenicityScore)) return false;
        ScaledPathogenicityScore that = (ScaledPathogenicityScore) o;
        return Float.compare(that.rawScore, rawScore) == 0 &&
                Float.compare(that.score, score) == 0 &&
                source == that.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, rawScore, score);
    }

    @Override
    public String toString() {
        return String.format("%s: %.3f (%.3f)", source, score, rawScore);
    }
}
