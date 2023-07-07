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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

class SvDaoBoundaryCalculatorTest {

    private final GenomicAssembly hg37 = GenomicAssemblies.GRCh37p13();

    private GenomicRegion buildRegion(int chr, int start, int end) {
        return GenomicRegion.of(hg37.contigById(chr), Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end);
    }

    @ParameterizedTest
    @CsvSource({
            "  1, 100,  0.01",
            "  1, 100,  0.1",
            " 11, 100,  0.56",
            "  1, 100,  0.33",
            " 10, 100,  0.825",
            "  1, 110,  0.825",
            " 50, 150,  0.50",
            "  1, 100,  0.01",

            "100, 200,  1.00",
            "100, 200,  0.95",
            "100, 200,  0.90",
            "100, 200,  0.85",
            "100, 200,  0.80",
            "100, 200,  0.75",
            "100, 200,  0.70",
            "100, 200,  0.65",
            "100, 200,  0.60",
            "100, 200,  0.55",
            "100, 200,  0.50",
            "100, 200,  0.45",
            "100, 200,  0.40",
            "100, 200,  0.35",
            "100, 200,  0.30",
            "100, 200,  0.25",
            "100, 200,  0.20",
            "100, 200,  0.15",
            "100, 200,  0.10",

            "1000, 2000,  0.85",
            "1000, 2000,  0.80",
            "1000, 2000,  0.75",
            "1000, 2000,  0.70",

            "2000, 4000,  0.85",
            "2000, 4000,  0.80",
            "2000, 4000,  0.75",
            "2000, 4000,  0.70",

            // minima
            "2133, 4007,   0.85",
            "2133, 4007,   0.80",
            "2133, 4007,   0.75",
            "2133, 4007,   0.70",

            "21232, 42007,   0.85",
            "21232, 42007,   0.80",
            "21232, 42007,   0.75",
            "21232, 42007,   0.70",
    })
    void testMinimalBounds(int startX, int endX, float minSimilarity) {
        GenomicRegion x = buildRegion(1, startX, endX);

        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(x, minSimilarity);

        GenomicRegion lowerMin = buildRegion(1, boundary.startMax(), endX);
        GenomicRegion upperMin = buildRegion(1, startX, boundary.endMin());

        assertThat(SvDaoUtil.jaccard(x, lowerMin), closeTo(minSimilarity, 0.01));
        assertThat(SvDaoUtil.jaccard(x, upperMin), closeTo(minSimilarity, 0.01));
    }

    @ParameterizedTest
    @CsvSource({
            "1100, 1200,  1.00",
            "1100, 1200,  0.95",
            "1100, 1200,  0.90",
            "1100, 1200,  0.85",
            "1100, 1200,  0.80",
            "1100, 1200,  0.75",
            "1100, 1200,  0.70",
            "1100, 1200,  0.65",
            "1100, 1200,  0.60",
            "1100, 1200,  0.55",
            "1100, 1200,  0.50",
            //"100, 200,  0.45", // underflows contig start, therefore cannot achieve desired similarity
            "1100, 1200,  0.45",
            "1100, 1200,  0.40",
            "1100, 1200,  0.35",
            "1100, 1200,  0.30",
            "1100, 1200,  0.25",
            "1100, 1200,  0.20",
            "1100, 1200,  0.15",
            "1100, 1200,  0.10",

            "1000, 2000,  0.85",
            "1000, 2000,  0.80",
            "1000, 2000,  0.75",
            "1000, 2000,  0.70",

            "2000, 4000,  0.85",
            "2000, 4000,  0.80",
            "2000, 4000,  0.75",
            "2000, 4000,  0.70",

            // minima
            "2133, 4007,   0.85",
            "2133, 4007,   0.80",
            "2133, 4007,   0.75",
            "2133, 4007,   0.70",

            "21232, 42007,   0.85",
            "21232, 42007,   0.80",
            "21232, 42007,   0.75",
            "21232, 42007,   0.70",
    })
    void testMaximalBounds(int startX, int endX, float minSimilarity) {
        GenomicRegion x = buildRegion(1, startX, endX);

        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(x, minSimilarity);

        GenomicRegion lowerMax = buildRegion(1, boundary.startMin(), endX);
        GenomicRegion upperMax = buildRegion(1, startX, boundary.endMax());

        assertThat(SvDaoUtil.jaccard(x, lowerMax), closeTo(minSimilarity, 0.01));
        assertThat(SvDaoUtil.jaccard(x, upperMax), closeTo(minSimilarity, 0.01));
    }

    @Test
    void testMaximalBoundsDontOverflowContig() {
        Contig chr1 = hg37.contigById(1);
        // 1:1-249250621
        GenomicRegion chromosome1 = buildRegion(1, 1, chr1.length());
        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(chromosome1, 0.75);
        GenomicRegion lowerMax = buildRegion(1, boundary.startMin(), chromosome1.end());
        assertThat(lowerMax, equalTo(chromosome1));
        GenomicRegion upperMax = buildRegion(1, chromosome1.start(), boundary.endMax());
        assertThat(upperMax, equalTo(chromosome1));
    }

    @Test
    void testSingleBaseInsertion() {
        GenomicRegion x = buildRegion(1, 10, 10);

        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(x, 0.75);

        assertThat(boundary.innerBoundsOffset(), equalTo(1));
        assertThat(boundary.outerBoundsOffset(), equalTo(1));
        assertThat(boundary.startMin(), equalTo(9));
        assertThat(boundary.startMax(), equalTo(11));
        assertThat(boundary.endMin(), equalTo(9));
        assertThat(boundary.endMax(), equalTo(11));
    }

    @Test
    void testTwoBaseInsertion() {
        GenomicRegion x = buildRegion(1, 10, 11);

        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(x, 0.75);

        assertThat(boundary.innerBoundsOffset(), equalTo(1));
        assertThat(boundary.outerBoundsOffset(), equalTo(1));
        assertThat(boundary.startMin(), equalTo(9));
        assertThat(boundary.startMax(), equalTo(11));
        assertThat(boundary.endMin(), equalTo(10));
        assertThat(boundary.endMax(), equalTo(12));
    }

    @Test
    void testThreeBaseInsertion() {
        GenomicRegion x = buildRegion(1, 10, 12);
        SvDaoBoundaryCalculator boundary = new SvDaoBoundaryCalculator(x, 0.75);

        assertThat(boundary.innerBoundsOffset(), equalTo(1));
        assertThat(boundary.outerBoundsOffset(), equalTo(1));
        assertThat(boundary.startMin(), equalTo(9));
        assertThat(boundary.startMax(), equalTo(11));
        assertThat(boundary.endMin(), equalTo(11));
        assertThat(boundary.endMax(), equalTo(13));
    }

}