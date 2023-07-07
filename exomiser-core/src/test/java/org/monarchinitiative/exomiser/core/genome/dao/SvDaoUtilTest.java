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

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SvDaoUtilTest {

    private final GenomicAssembly hg37 = GenomicAssemblies.GRCh37p13();

    private GenomicRegion buildRegion(int chr, int start, int end) {
        return GenomicRegion.of(hg37.contigById(chr), Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end);
    }

    @Test
    void testJaccardCoefficientOtherChromosomes() {
        GenomicRegion R1_100 = buildRegion(1, 1, 100);
        GenomicRegion R2_100 = buildRegion(2, 1, 100);

        assertThat(SvDaoUtil.jaccard(R1_100, R2_100), equalTo(0.0));
    }

    @ParameterizedTest
    @CsvSource({
            "  1, 100,     1, 100,   1.0",
            "  1, 100,    10,  10,   0.01",
            "  1, 100,    11,  11,   0.01",
            "  1, 100,    99,  99,   0.01",
            "  1, 100,    11,  20,   0.1",
            " 10, 100,    50, 100,   0.56",
            "  1, 100,    51, 150,   0.333",
            " 10, 100,     1, 110,   0.827",
            "  1, 110,    10, 100,   0.827",
            " 50, 150,    50, 100,   0.504",
            "  1, 100,   200, 500,   0.0",

            // minima
            "100, 200,   125, 200,   0.75",  // 25 = 100 * (1 - 0.75)
            "100, 200,   100, 175,   0.75",
            // maxima
            "100, 200,    66, 200,   0.75", // 33 = (100 / 0.75) - 100
            "100, 200,   100, 233,   0.75",

            // minima
            "100, 200,   130, 200,   0.70",  // 30 = 100 * (1 - 0.70)
            "100, 200,   100, 170,   0.70",
            // maxima
            "100, 200,    57, 200,   0.70", // 43 = (100 / 0.75) - 100
            "100, 200,   100, 243,   0.70",

            // minima
            "100, 200,   150, 200,   0.50",  // 50 = 100 * (1 - 0.50)
            "100, 200,   100, 150,   0.50",
            // maxima
            "100, 200,     1, 200,   0.50", // 100 = (100 / 0.75) - 100
            "100, 200,   100, 300,   0.50",

            // minima
            "1000, 2000,   1250, 2000,   0.75", // 250 = (2000 - 1000) * (1 - 0.75)
            "1000, 2000,   1000, 1750,   0.75",
            // maxima
            "1000, 2000,    667, 2000,   0.75", // 333 = ((2000 - 1000) / 0.75 ) - (2000 - 1000)
            "1000, 2000,   1000, 2333,   0.75",

            // minima
            "2000, 4000,   2500, 4000,   0.75", // 500 = (4000 - 2000) * (1 - 0.75)
            "2000, 4000,   2000, 3500,   0.75",
            // maxima
            "2000, 4000,   1333, 4000,   0.75", // 700 = (2000 / 0.75 ) - 2000
            "2000, 4000,   2000, 4666,   0.75",

            // minima
            "2133, 4007,   2602, 4007,   0.75", // 468.5 = (4007 - 2133) * (1 - 0.75)
            "2133, 4007,   2133, 3538,   0.75", // 469
            // maxima
            "2133, 4007,   1508, 4007,   0.75", // 625 = 624.666666667 = ((4007 - 2133) / 0.75)  - (4007 - 2133)
            "2133, 4007,   2133, 4632,   0.75", // 656

            "234613, 24006577,   1, 31930565,   0.75", // 7923988 = ((24006577 - 234613) / 0.75)  - (24006577 - 234613)
    })
    void testJaccardCoefficient(int startX, int endX, int startY, int endY, float expect) {
        GenomicRegion x = buildRegion(1, startX, endX);
        GenomicRegion y = buildRegion(1, startY, endY);
        assertThat(SvDaoUtil.jaccard(x, y), closeTo(expect, 0.01));
    }

    @ParameterizedTest
    @CsvSource({
            " 1, 100,     1, 100,   1.0",
            " 1, 100,    10,  10,   0.01",
            " 1, 100,    10,  20,   0.1",
            "11, 100,    50, 100,   0.56",
            " 1, 100,    50, 150,   0.5", // n.b. this is 0.33 when calculated using jaccard
            "10, 100,     1, 110,   0.825",
            " 1, 110,    10, 100,   0.825",
            "50, 150,    50, 100,   0.5",
            "50, 100,    50, 150,   0.5",
            " 1, 100,   200, 500,   0.0",
            " 100, 200,   100, 175,   0.75",
            " 100, 200,   125, 200,   0.75",
            " 100, 200,   125, 175,   0.50",
    })
    void testReciprocalOverlap(int startX, int endX, int startY, int endY, float expect) {
        GenomicRegion x = buildRegion(1, startX, endX);
        GenomicRegion y = buildRegion(1, startY, endY);
        assertThat(SvDaoUtil.reciprocalOverlap(x, y), closeTo(expect, 0.01));
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

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1.0",
            "10, 10, 1.0",
            "10, 7, 0.7",
            "10, 5, 0.5",
            "100, 99, 0.99",
            "99, 100, 0.99",
            "10, 100, 0.10",
            "-1, 1, 0.0",
            "-10, 1, 0.0",
            "-10, -5, 0.5",
            "-10, -7, 0.7",
            "-10, -10, 1.0",
            "-99, -100, 0.99",

    })
    void testJaccardChangeLength(int x, int y, double expect) {
        assertThat(SvDaoUtil.jaccard(x, y), equalTo(expect));
    }
}