/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;

/**
 * Utility class for helping with calculations involving {@link ChromosomalRegion} objects.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class ChromosomalRegionUtil {

    private ChromosomalRegionUtil() {
        // static utility class.
    }

    /**
     * Returns a value to be added/subtracted to a ChromosomalRegion's start and end positions such that another region
     * whose start and end positions fall within these boundaries will have the required minimum coverage provided in the
     * method. For example the region 10-20 with a 0.85 overlap would have a margin value of 1 to be added/subtracted to
     * the start and end - i.e. anywhere between 10+/-1 and 20+/-1 will satisfy the 85% overlap requirement. Return values
     * are rounded to the nearest integer.
     *
     * @param region      The {@link ChromosomalRegion} for which the boundaries are to be calculated
     * @param minCoverage The minimum required overlap for other regions
     * @return The modifier, to the nearest whole number, to be added and subtracted from the start and end of the input
     * region
     */
    public static int getBoundaryMargin(ChromosomalRegion region, double minCoverage) {
        if (minCoverage < -0 || minCoverage > 1) {
            throw new IllegalArgumentException("minCoverage must be in range 0.0 - 1.0");
        }
        return (int) Math.round(length(region) * (1 - minCoverage) / 2d);
    }

    /**
     * Determines the Jaccard coefficient of two {@link ChromosomalRegion} based on their positions on a chromosome. A
     * return value of 1.0 indicates an identical region and 0 a completely non-overlapping region.
     *
     * @param x the first {@link ChromosomalRegion}
     * @param y the second {@link ChromosomalRegion}
     * @return Jaccard coefficient of x and y
     */
    public static double jaccard(ChromosomalRegion x, ChromosomalRegion y) {
        if (x.getStartContigId() != y.getStartContigId()) {
            return 0;
        }
        // Jaccard is the intersection over the union, e.g.
        //    |-----------------|               = x
        //              |------------------|    = y
        //
        //              |-------|               = intersection(x, y)
        //    |-----------------------------|   = union(x, y)
        // J(x, y) = intersection(x, y) / (length(x) + length(y) - intersection(x, y))
        int intersection = intersection(x, y);
        return (double) intersection / (length(x) + length(y) - intersection);
    }

    private static int intersection(ChromosomalRegion x, ChromosomalRegion y) {
        if (x.getEnd() < y.getStart() || y.getEnd() < x.getStart()) {
            return 0;
        }
        return Math.max(Math.min(x.getEnd(), y.getEnd()) - Math.max(x.getStart(), y.getStart()), 1);
    }

    // returns zero-based length
    private static int length(ChromosomalRegion region) {
        return Math.max(region.getEnd() - region.getStart(), 1);
    }
}
