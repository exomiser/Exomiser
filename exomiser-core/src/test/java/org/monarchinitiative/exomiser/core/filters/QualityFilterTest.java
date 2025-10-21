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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVcfReader;
import org.monarchinitiative.exomiser.core.genome.VcfFiles;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class QualityFilterTest {

    private QualityFilter instance;

    private static final double MIN_QUAL_THRESHOLD = 3.0f;
    private static final double OVER_THRESHOLD = MIN_QUAL_THRESHOLD + 1.0f;
    private static final double UNDER_THRESHOLD = MIN_QUAL_THRESHOLD - 1.0f;

    private final VariantEvaluation highQualityPassesFilter = TestFactory.variantBuilder(1, 1, "A", "T")
            .quality(OVER_THRESHOLD)
            .build();

    private final VariantEvaluation lowQualityFailsFilter = TestFactory.variantBuilder(1, 1, "A", "T")
            .quality(UNDER_THRESHOLD)
            .build();

    @BeforeEach
    public void setUp() {
        instance = new QualityFilter(MIN_QUAL_THRESHOLD);
    }

    @Test
    public void testGetMimimumQualityThreshold() {
        assertThat(instance.mimimumQualityThreshold(), equalTo(MIN_QUAL_THRESHOLD));
    }   

    @Test
    public void testFilterType() {
        assertThat(instance.filterType(), equalTo(FilterType.QUALITY_FILTER));
    }

    @Test
    public void filterThrowIllegalArgumentExceptionWhenInitialisedWithNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new QualityFilter(-1));
    }

    @Test
    public void testFilterVariantOfHighQualityPassesFilter() {
        FilterResult filterResult = instance.runFilter(highQualityPassesFilter);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterVariantOfLowQualityFailsFilter() {
        FilterResult filterResult = instance.runFilter(lowQualityFailsFilter);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    public void testPassesFilterOverThresholdIsTrue() {
        assertThat(instance.overQualityThreshold(OVER_THRESHOLD), is(true));
    }

    @Test
    public void testPassesFilterUnderThresholdIsFalse() {
        assertThat(instance.overQualityThreshold(UNDER_THRESHOLD), is(false));
    }

    @Test
    public void testHashCode() {
        VariantFilter qualityFilter = new QualityFilter(MIN_QUAL_THRESHOLD);
        assertThat(instance.hashCode(), equalTo(qualityFilter.hashCode()));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualAnotherClass() {
        Object obj = new Object();
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualToOtherWithDifferentQualityThreshold() {
        Object obj = new QualityFilter(8.0f);
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testEqualToOtherWithSameQualityThreshold() {
        Object obj = new QualityFilter(MIN_QUAL_THRESHOLD);
        assertThat(instance.equals(obj), is(true));
    }

    @ParameterizedTest
    @CsvSource({
            "PASS, '1       47607851        rs2056899       A       T       1826.78 VQSRTrancheSNP99.00to99.90      ABHet=0.589;ABHom=1;AC=6;AF=0.75;AN=8;BaseQRankSum=0.561;    GT:AD:DP:GQ:PL   1/1:0,45:45:99:1253,132,0    0/1:6,8:14:99:197,0,182    0/1:12,4:16:66:66,0,346    1/1:0,13:13:36:357,36,0'",
            "FAIL, '1       63735   .       CCTA    C       193.23  PASS    AC=3;AF=0.5;AN=6;     GT:AD:DP:GQ:PL  ./.:.:.:.:.     1/1:6,2:4:6:116,6,0     0/1:8,4:8:99:128,0,301       0/0:18,2:15:30:0,30,666'",
            "FAIL, '1       47123898        rs67089539      TAAA    TAAAA,T 382.01  PASS    AC=1,2;AF=0.125,0.25;AN=8;BaseQRankSum=-2.349;DB;DP=66       GT:AD:DP:GQ:PL  0/0:1,0,0:4:5:0,5,20,6,23,31    0/0:18,0,0:21:13:0,13,306,51,356,783    0/2:14,0,3:20:99:116,140,259,0,102,280  1/2:8,0,4:21:99:362,206,248,163,0,482'",
            "FAIL, '1       70904523        rs112004441     T       TTG,TTGTG       1285.02 PASS    AC=3,4;AF=0.375,0.5;AN=8     GT:AD:DP:GQ:PL  1/2:10,5,6:23:99:555,231,199,192,0,162    1/2:12,1,4:17:30:272,146,131,44,0,30    1/2:6,1,4:11:28:288,148,133,42,0,28    0/2:15,0,2:20:99:297,180,279,0,120,156'",
    })
    void multiSampleGenotypesTest(FilterResult.Status filterStatus, String vcfRecord) {
        TestVcfReader testVcfReader = TestVcfReader.of(List.of("sample1", "sample2", "sample3", "sample4"));
        VariantContext variantContext = testVcfReader.readVariantContext(vcfRecord);
        System.out.println(variantContext);
        QualityFilter qualityFilter = new QualityFilter(MIN_QUAL_THRESHOLD);
        assertThat(qualityFilter.runQualityFilter(variantContext).status(), equalTo(filterStatus));
    }
}
