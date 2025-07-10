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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class FilterResultTest {

    @Test
    void testFilterType() {
        FilterType expResult = FilterType.FREQUENCY_FILTER;

        FilterResult instance = FilterResult.pass(expResult);

        FilterType result = instance.filterType();
        assertThat(result, equalTo(expResult));
    }

    @Test
    void testPassedFilterIsTrueWhenFilterResultStatusIsPass() {
        FilterResult instance = FilterResult.pass(FilterType.FREQUENCY_FILTER);
        assertThat(instance.passed(), is(true));
    }

    @Test
    void testPassedFilterIsFalseWhenFilterResultStatusIsFail() {
        FilterResult instance = FilterResult.fail(FilterType.FREQUENCY_FILTER);
        assertThat(instance.passed(), is(false));
    }

    @Test
    void testGetResultStatus() {
        FilterResult instance = FilterResult.pass(FilterType.INTERVAL_FILTER);
        assertThat(instance.passed(), equalTo(true));
    }

    @Test
    void testNotRunFilterResult() {
        FilterResult instance = FilterResult.notRun(FilterType.INTERVAL_FILTER);
        assertThat(instance.passed(), equalTo(false));
        assertThat(instance.failed(), equalTo(false));
        assertThat(instance.wasRun(), equalTo(false));
    }

    @Test
    void testHashCode() {
        FilterResult instance = FilterResult.fail(FilterType.INTERVAL_FILTER);
        FilterResult another = FilterResult.fail(FilterType.INTERVAL_FILTER);
        int expResult = another.hashCode();
        int result = instance.hashCode();
        assertThat(result, equalTo(expResult));
    }

    @Test
    void testNotEqualToNullObject() {
        Object obj = null;
        FilterResult instance = FilterResult.fail(FilterType.INTERVAL_FILTER);
        assertThat(instance, not(equalTo(obj)));
    }
    
    @Test
    void testNotEqualToDifferentFilterType() {
        FilterResult other = FilterResult.fail(FilterType.BED_FILTER);
        FilterResult instance = FilterResult.fail(FilterType.INTERVAL_FILTER);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    void testEqualToOtherFilterResult() {
        FilterResult other = FilterResult.fail(FilterType.INTERVAL_FILTER);
        FilterResult instance = FilterResult.fail(FilterType.INTERVAL_FILTER);
        assertThat(instance, equalTo(other));
    }

    @Test
    void testToString() {
        FilterResult instance = FilterResult.pass(FilterType.INTERVAL_FILTER);
        String expResult = "Filter=INTERVAL_FILTER status=PASS";
        String result = instance.toString();
        assertThat(result, equalTo(expResult));
    }

}
