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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.svart.GenomicRegion;

/**
 * Utility class for helping with calculations involving {@link GenomicRegion} objects.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class SvDaoUtil {

    private SvDaoUtil() {
        // static utility class.
    }

    /**
     * Determines the Jaccard coefficient of two variant change lengths. A
     * return value of 1.0 indicates an identical change length and 0 two opposite signed values. For example an
     * insertion with a positive change length and a deletion with a negative change length will return a value of zero.
     *
     * @param x the first variant change length
     * @param y the second variant change length
     * @return Jaccard coefficient of x and y, assuming the same sign or 0
     */
    public static double jaccard(int x, int y) {
        if (x == 0 && y == 0) {
            return 1;
        }
        if (Integer.signum(x) != Integer.signum(y)) {
            return 0;
        }
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        double intersection = Math.min(absX, absY);
        return intersection / (absX + absY - intersection);
    }

    /**
     * Determines the Jaccard coefficient of two {@link GenomicRegion} based on their positions on a chromosome. A
     * return value of 1.0 indicates an identical region and 0 a completely non-overlapping region.
     *
     * @param x the first {@link GenomicRegion}
     * @param y the second {@link GenomicRegion}
     * @return Jaccard coefficient of x and y
     */
    public static double jaccard(GenomicRegion x, GenomicRegion y) {
        // Jaccard is the intersection over the union, e.g.
        //    |-----------------|               = x
        //              |------------------|    = y
        //
        //              |-------|               = intersection(x, y)
        //    |-----------------------------|   = union(x, y)
        // J(x, y) = intersection(x, y) / (length(x) + length(y) - intersection(x, y))
        double intersection = x.overlapLength(y);
        return intersection / (x.length() + y.length() - intersection);
    }

    public static double reciprocalOverlap(GenomicRegion x, GenomicRegion y) {
        if (!x.overlapsWith(y)) {
            return 0;
        }
        double intersection = x.overlapLength(y);
        return Math.min(intersection / x.length(), intersection / y.length());
    }
}
