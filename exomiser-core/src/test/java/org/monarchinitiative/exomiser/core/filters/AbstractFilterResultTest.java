/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AbstractFilterResultTest {

    @Test
    public void testGetFilterType() {
        FilterType expResult = FilterType.FREQUENCY_FILTER;

        FilterResult instance = new PassFilterResult(expResult);

        FilterType result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    @Test
    public void testPassedFilterIsTrueWhenFilterResultStatusIsPass() {
        FilterResult instance = new PassFilterResult(FilterType.FREQUENCY_FILTER);
        assertThat(instance.passed(), is(true));
    }

    @Test
    public void testPassedFilterIsFalseWhenFilterResultStatusIsFail() {
        FilterResult instance = new FailFilterResult(FilterType.FREQUENCY_FILTER);
        assertThat(instance.passed(), is(false));
    }

//    @Test
//    public void testGetResultStatus() {
//        FilterResult instance = new PassFilterResult(FilterType.INTERVAL_FILTER);
//        assertThat(instance.getResultStatus(), equalTo(FilterResultStatus.PASS));
//    }

    @Test
    public void testHashCode() {
        FilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER);
        FilterResult another = new FailFilterResult(FilterType.INTERVAL_FILTER);
        int expResult = another.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testNotEqualToNullObject() {
        Object obj = null;
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER);
        assertThat(instance, not(equalTo(obj)));
    }
    
    @Test
    public void testNotEqualToDifferentFilterType() {
        AbstractFilterResult other = new FailFilterResult(FilterType.BED_FILTER);
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFilterResult() {
        AbstractFilterResult other = new FailFilterResult(FilterType.INTERVAL_FILTER);
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER);
        assertThat(instance, equalTo(other));
    }

    @Test
    public void testToString() {
        AbstractFilterResult instance = new PassFilterResult(FilterType.INTERVAL_FILTER);
        String expResult = "Filter=Interval status=PASS";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
