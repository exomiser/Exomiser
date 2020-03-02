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

package org.monarchinitiative.exomiser.core.model;

import java.util.Objects;

/**
 * Class representing the VCF confidence interval:
 * <p>
 * ##INFO=<ID=CIPOS,Number=2,Type=Integer,Description="Confidence interval around POS for imprecise variants">
 * ##INFO=<ID=CIEND,Number=2,Type=Integer,Description="Confidence interval around END for imprecise variants">
 * <p>
 * Although I can't find a formal definition stating this, the examples always show first integer as negative or zero and
 * the second positive or zero.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class ConfidenceInterval {

    private static final ConfidenceInterval EMPTY = new ConfidenceInterval(0, 0);

    private final int lowerBound;
    private final int upperBound;

    private ConfidenceInterval(int lowerBound, int upperBound) {
        if (lowerBound > 0 || upperBound < 0) {
            throw new IllegalArgumentException("'" + lowerBound + ", " + upperBound + "' ConfidenceInterval must have negative lowerBound and positive upperBound");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static ConfidenceInterval of(int lowerBound, int upperBound) {
        if (lowerBound == 0 && upperBound == 0) {
            return EMPTY;
        }
        return new ConfidenceInterval(lowerBound, upperBound);
    }

    public static ConfidenceInterval empty() {
        return EMPTY;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getMinPos(int pos) {
        return pos + lowerBound;
    }

    public int getMaxPos(int pos) {
        return pos + upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfidenceInterval)) return false;
        ConfidenceInterval that = (ConfidenceInterval) o;
        return lowerBound == that.lowerBound &&
                upperBound == that.upperBound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public String toString() {
        return "ConfidenceInterval{" +
                "lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                '}';
    }
}
