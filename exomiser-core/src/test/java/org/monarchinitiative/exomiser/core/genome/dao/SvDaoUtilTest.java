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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SvDaoUtilTest {

    private final GenomicAssembly hg37 = GenomicAssemblies.GRCh37p13();

    private GenomicRegion buildRegion(int chr, int start, int end) {
        return GenomicRegion.of(hg37.contigById(chr), Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, start, end);
    }

    private GenomicRegion minRegion(GenomicRegion insertion, int margin) {
        return buildRegion(1, insertion.start() + margin, insertion.end() - margin);
    }

    private GenomicRegion maxRegion(GenomicRegion insertion, int margin) {
        return buildRegion(1, Math.max(insertion.start() - margin, 1), insertion.end() + margin);
    }

    @Test
    void testJaccardCoefficientOtherChromosomes() {
        GenomicRegion R1_100 = buildRegion(1, 1, 100);
        GenomicRegion R2_100 = buildRegion(2, 1, 100);

        assertThat(SvDaoUtil.jaccard(R1_100, R2_100), equalTo(0.0));
    }

    @ParameterizedTest
    @CsvSource({
            "1,  1, 100,    1,   1, 100,   1.0",
            "1,  1, 100,    1,  10,  10,   0.01",
            "1,  1, 100,    1,  10,  20,   0.1",
            "1,  1, 100,    1,  50, 100,   0.5",
            "1,  1, 100,    1,  50, 150,   0.33",
            "1, 10, 100,    1,   1, 110,   0.825",
            "1, 50, 150,    1,  50, 100,   0.5",
            "1,  1, 100,    1, 200, 500,   0.0",
    })
    void testJaccardCoefficient(int chrX, int startX, int endX, int chrY, int startY, int endY, float expect) {
        GenomicRegion x = buildRegion(chrX, startX, endX);
        GenomicRegion y = buildRegion(chrY, startY, endY);

        assertThat(SvDaoUtil.jaccard(x, y), closeTo(expect, 0.1));
    }

    @Test
    void testJaccardInsertionOfLengthZeroWithNoGivenLength() {
        // esv3304209 is an INS_ME with no length in DGV (DGV does not contain length information)
        // this is represented in the DBVAR table as:
        // DGV_VARIANTS:
        // 10	23037996	23037996	CNV	MOBILE_ELEMENT_INSERTION	esv3304209	20981092	OLIGO_ACGH,DIGITAL_ARRAY,PCR,SEQUENCING	185	10	0
        // DGV file:
        // esv3304209	10	23037995	23037996	CNV	mobile element insertion	1000_Genomes_Consortium_Pilot_Project	20981092 Digital array,Oligo aCGH,PCR,Sequencing	essv7754847,essv7752925,essv7749293,essv7742151,essv7741351,essv7759021,essv7750468,essv7759990,essv7743888,essv7741543                                                                                        M        185     10      0               ""      NA18501,NA18502,NA18505,NA18508,NA18511,NA18519,NA18856,NA18861,NA18871,NA19257
        // DBVAR_VARIANTS:
        // 10	23037995	10	23037996	300	INS	0,0	0,0	essv7741351	-1	NaN	UNKNOWN	not_provided	""	true	false	""		""	UNKNOWN
        // DBVAR VCF:
        // 10	23037995	essv7741351	A	<INS:ME>	.	.	DBVARID;SVTYPE=INS;CIPOS=-32,32;CIEND=-32,32;IMPRECISE;END=23037996;SVLEN=300;EXPERIMENT=129;SAMPLE=NA18871;REGIONID=esv3304209
        GenomicRegion insMe = buildRegion(10, 23037996, 23037996);
        assertThat(SvDaoUtil.jaccard(insMe, insMe), equalTo(1.0));
    }

    @Test
    void testJaccardInsertionOfLengthOneWithNoGivenLength() {
        GenomicRegion insMe = buildRegion(10, 23037995, 23037996);
        assertThat(SvDaoUtil.jaccard(insMe, insMe), equalTo(1.0));
    }

    @Test
    void bondaryCalcThrowsExceptionWithZeroValueInput() {
        GenomicRegion region = buildRegion(1, 1, 100);
        assertThrows(IllegalArgumentException.class, () -> SvDaoUtil.getBoundaryMargin(region, -0.1));
    }

    @Test
    void bondaryCalcThrowsExceptionWithGreaterThanOneInput() {
        GenomicRegion region = buildRegion(1, 1, 100);
        assertThrows(IllegalArgumentException.class, () -> SvDaoUtil.getBoundaryMargin(region, 1.1));
    }

    @Test
    void boundaryCalc() {
        GenomicRegion region = buildRegion(1, 1, 100);
        double minCoverage = 0.85;
        int margin = SvDaoUtil.getBoundaryMargin(region, minCoverage);
        // maximum range
        assertThat(SvDaoUtil.jaccard(region, maxRegion(region, margin)), greaterThan(minCoverage));
        // minimum range
        assertThat(SvDaoUtil.jaccard(region, minRegion(region, margin)), greaterThan(minCoverage));
    }

    @Test
    void testMarginLargeInsertion() {
        GenomicRegion region = buildRegion(1, 112345, 9998362);
        double minCoverage = 0.95;
        int margin = SvDaoUtil.getBoundaryMargin(region, minCoverage);
        assertThat(SvDaoUtil.jaccard(region, maxRegion(region, margin)), greaterThan(minCoverage));
        assertThat(SvDaoUtil.jaccard(region, minRegion(region, margin)), greaterThan(minCoverage));
    }

    @Test
    void testMarginDeletion() {
        GenomicRegion deletion = buildRegion(1, 40685415, 40685474);
        double minCoverage = 0.85;
        int margin = SvDaoUtil.getBoundaryMargin(deletion, minCoverage);
        assertThat(SvDaoUtil.jaccard(deletion, maxRegion(deletion, margin)), greaterThan(minCoverage));
        assertThat(SvDaoUtil.jaccard(deletion, minRegion(deletion, margin)), greaterThan(minCoverage));
    }

    @Test
    void testDeletion() {
        GenomicRegion deletion = buildRegion(1, 61569, 62915555);
        double minCoverage = 0.85;
        int margin = SvDaoUtil.getBoundaryMargin(deletion, minCoverage);
        double error = 0.01;
        assertThat(SvDaoUtil.jaccard(deletion, maxRegion(deletion, margin)), greaterThan(minCoverage - error));
        assertThat(SvDaoUtil.jaccard(deletion, minRegion(deletion, margin)), greaterThan(minCoverage - error));
    }
}