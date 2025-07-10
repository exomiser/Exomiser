/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.sample;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record AgeRange(Age lower, Age upper) {

    public AgeRange {
        Objects.requireNonNull(lower);
        Objects.requireNonNull(upper);
    }

    public static AgeRange between(Age lower, Age upper) {
        if (lower.toPeriod().toTotalMonths() > upper.toPeriod().toTotalMonths()) {
            throw new IllegalArgumentException("Upper age limit must be greater than lower");
        }
        return new AgeRange(lower, upper);
    }

    public static AgeRange over(Age limit) {
        return new AgeRange(limit, Age.unknown());
    }

    public static AgeRange under(Age limit) {
        return new AgeRange(Age.unknown(), limit);
    }

    @Override
    public String toString() {
        return "AgeRange{" +
                "lower=" + lower +
                ", upper=" + upper +
                '}';
    }
}
