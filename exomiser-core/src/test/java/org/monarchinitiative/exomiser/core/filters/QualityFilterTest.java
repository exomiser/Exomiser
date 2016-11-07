/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation.VariantBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class QualityFilterTest {

    private QualityFilter instance;

    private static final double MIN_QUAL_THRESHOLD = 3.0f;
    private static final double OVER_THRESHOLD = MIN_QUAL_THRESHOLD + 1.0f;
    private static final double UNDER_THRESHOLD = MIN_QUAL_THRESHOLD - 1.0f;

    private static VariantEvaluation highQualityPassesFilter;
    private static VariantEvaluation lowQualityFailsFilter;

    @Before
    public void setUp() {

        highQualityPassesFilter = testVariantBuilder().quality(OVER_THRESHOLD).build();
        lowQualityFailsFilter = testVariantBuilder().quality(UNDER_THRESHOLD).build();

        instance = new QualityFilter(MIN_QUAL_THRESHOLD);
    }

    private VariantBuilder testVariantBuilder() {
        return new VariantEvaluation.VariantBuilder(1, 1, "A", "T");
    }

    @Test
    public void testGetMimimumQualityThreshold() {
        assertThat(instance.getMimimumQualityThreshold(), equalTo(MIN_QUAL_THRESHOLD));
    }   

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.QUALITY_FILTER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void filterThrowIllegalArgumentExceptionWhenInitialisedWithNegativeValue() {
        instance = new QualityFilter(-1);
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
        Object obj = new String();
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

}
