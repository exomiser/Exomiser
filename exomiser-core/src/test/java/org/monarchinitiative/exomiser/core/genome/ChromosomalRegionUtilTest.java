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

package org.monarchinitiative.exomiser.core.genome;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ChromosomalRegionUtilTest {

    @Test
    void testJaccardCoefficientOtherChromosomes() {
        ChromosomalRegion R1_100 = new GeneticInterval(1, 1, 100);
        ChromosomalRegion R2_100 = new GeneticInterval(2, 1, 100);

        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R2_100), equalTo(0.0));
    }

    @Test
    void testJaccardCoefficient() {
        ChromosomalRegion R1_100 = new GeneticInterval(1, 1, 100);
        ChromosomalRegion R10_20 = new GeneticInterval(1, 10, 20);
        ChromosomalRegion R10_100 = new GeneticInterval(1, 10, 100);
        ChromosomalRegion R1_110 = new GeneticInterval(1, 1, 110);
        ChromosomalRegion R50_100 = new GeneticInterval(1, 50, 100);
        ChromosomalRegion R50_150 = new GeneticInterval(1, 50, 150);
        ChromosomalRegion R200_500 = new GeneticInterval(1, 200, 500);

        ChromosomalRegion R10_10 = new GeneticInterval(1, 10, 10);

        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R1_100), equalTo(1.0));
        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R10_20), closeTo(0.1, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R10_10), closeTo(0.01, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R50_100, R1_100), closeTo(0.5, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R50_150), closeTo(0.33, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R10_20), closeTo(0.1, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R10_100, R1_110), closeTo(0.825, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R50_150, R50_100), closeTo(0.5, 0.01));
        assertThat(ChromosomalRegionUtil.jaccard(R1_100, R200_500), equalTo(0.0));
    }

    @Test
    void bondaryCalcThrowsExceptionWithZeroValueInput() {
        ChromosomalRegion region = new GeneticInterval(1, 1, 100);
        assertThrows(IllegalArgumentException.class, () -> ChromosomalRegionUtil.getBoundaryMargin(region, -0.1));
    }

    @Test
    void bondaryCalcThrowsExceptionWithGreaterThanOneInput() {
        ChromosomalRegion region = new GeneticInterval(1, 1, 100);
        assertThrows(IllegalArgumentException.class, () -> ChromosomalRegionUtil.getBoundaryMargin(region, 1.1));
    }

    @Test
    void boundaryCalc() {
        ChromosomalRegion region = new GeneticInterval(1, 1, 100);
        double minCoverage = 0.85;
        int margin = ChromosomalRegionUtil.getBoundaryMargin(region, minCoverage);
        // maximum range
        assertThat(ChromosomalRegionUtil.jaccard(region, maxRegion(region, margin)), greaterThan(minCoverage));
        // minimum range
        assertThat(ChromosomalRegionUtil.jaccard(region, minRegion(region, margin)), greaterThan(minCoverage));
    }

    @Test
    void testMarginLargeInsertion() {
        ChromosomalRegion insertion = new GeneticInterval(1, 112345, 9998362);
        double minCoverage = 0.95;
        int margin = ChromosomalRegionUtil.getBoundaryMargin(insertion, minCoverage);
        assertThat(ChromosomalRegionUtil.jaccard(insertion, maxRegion(insertion, margin)), greaterThan(minCoverage));
        assertThat(ChromosomalRegionUtil.jaccard(insertion, minRegion(insertion, margin)), greaterThan(minCoverage));
    }

    @Test
    void testMarginDeletion() {
        ChromosomalRegion deletion = new GeneticInterval(1, 40685415, 40685474);
        double minCoverage = 0.85;
        int margin = ChromosomalRegionUtil.getBoundaryMargin(deletion, minCoverage);
        System.out.printf("%s minCoverage: %f margin: %d%n", deletion, minCoverage, margin);
        assertThat(ChromosomalRegionUtil.jaccard(deletion, maxRegion(deletion, margin)), greaterThan(minCoverage));
        assertThat(ChromosomalRegionUtil.jaccard(deletion, minRegion(deletion, margin)), greaterThan(minCoverage));
    }

    @Test
    void testDeletion() {
        ChromosomalRegion deletion = new GeneticInterval(1, 61569, 62915555);
        double minCoverage = 0.85;
        int margin = ChromosomalRegionUtil.getBoundaryMargin(deletion, minCoverage);
        System.out.printf("%s minCoverage: %f margin: %d%n", deletion, minCoverage, margin);
        double error = 0.01;
        assertThat(ChromosomalRegionUtil.jaccard(deletion, maxRegion(deletion, margin)), greaterThan(minCoverage - error));
        assertThat(ChromosomalRegionUtil.jaccard(deletion, minRegion(deletion, margin)), greaterThan(minCoverage - error));
    }

    private GeneticInterval minRegion(ChromosomalRegion insertion, int margin) {
        return new GeneticInterval(1, insertion.getStart() + margin, insertion.getEnd() - margin);
    }

    private GeneticInterval maxRegion(ChromosomalRegion insertion, int margin) {
        return new GeneticInterval(1, insertion.getStart() - margin, insertion.getEnd() + margin);
    }
}