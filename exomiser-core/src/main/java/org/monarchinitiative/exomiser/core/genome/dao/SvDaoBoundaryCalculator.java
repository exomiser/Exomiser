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

import java.util.Objects;

//    /**
//     * Returns a value to be added/subtracted to a ChromosomalRegion's start and end positions such that another region
//     * whose start and end positions fall within these boundaries will have the required minimum coverage provided in the
//     * method. For example the region 10-20 with a 0.85 overlap would have a margin value of 1 to be added/subtracted to
//     * the start and end - i.e. anywhere between 10+/-1 and 20+/-1 will satisfy the 85% overlap requirement. Return values
//     * are rounded to the nearest integer.
//     *
//     * @param region      The {@link GenomicRegion} for which the boundaries are to be calculated
//     * @param minCoverage The minimum required overlap for other regions
//     * @return The modifier, to the nearest whole number, to be added and subtracted from the start and end of the input
//     * region
//     */
public class SvDaoBoundaryCalculator {

    private final GenomicRegion genomicRegion;
    private final double minSimilarity;

    private final int innerBoundsOffset;
    private final int outerBoundsOffset;

    public SvDaoBoundaryCalculator(GenomicRegion genomicRegion, double minSimilarity) {
        this.genomicRegion = genomicRegion;
        this.minSimilarity = minSimilarity;
        int length = genomicRegion.length();
        this.innerBoundsOffset = innerBoundsOffset(length, minSimilarity);
        this.outerBoundsOffset = outerBoundsOffset(length, minSimilarity);
    }

    private int outerBoundsOffset(int length, double minSimilarity) {
        // (100 / 0.70) - 100
        return Math.max(1, (int) Math.round((length / minSimilarity) - length));
    }

    private int innerBoundsOffset(int length, double minSimilarity) {
        // 100 * (1 - 0.70)
        return Math.max(1, (int) Math.round(length * (1 - minSimilarity)));
    }

    public GenomicRegion genomicRegion() {
        return genomicRegion;
    }

    public double minSimilarity() {
        return minSimilarity;
    }

    public int innerBoundsOffset() {
        return innerBoundsOffset;
    }

    public int outerBoundsOffset() {
        return outerBoundsOffset;
    }

    public int startMin() {
        return Math.max(1, genomicRegion.startMin() - outerBoundsOffset);
    }

    public int startMax() {
        return genomicRegion.startMax() + innerBoundsOffset;
    }

    public int endMin() {
        return genomicRegion.endMin() - innerBoundsOffset;
    }

    public int endMax() {
        return Math.min(genomicRegion.contig().length(), genomicRegion.endMax() + outerBoundsOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SvDaoBoundaryCalculator that = (SvDaoBoundaryCalculator) o;
        return Double.compare(that.minSimilarity, minSimilarity) == 0 && genomicRegion.equals(that.genomicRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicRegion, minSimilarity);
    }

    @Override
    public String toString() {
        return "SvDaoBoundaryCalculator{" +
                "genomicRegion=" + genomicRegion +
                ", minSimilarity=" + minSimilarity +
                ", innerBoundsOffset=" + innerBoundsOffset +
                ", outerBoundsOffset=" + outerBoundsOffset +
                '}';
    }
}
