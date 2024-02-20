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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportTest {

    FilterReport instance;

    private static final int PASSED = 12;
    private static final int FAILED = 345;
    private static final String MESSAGE = "hello";

    @BeforeEach
    public void setUp() {
        instance = new FilterReport(FilterType.FREQUENCY_FILTER, PASSED, FAILED, List.of(MESSAGE));
    }

    @Test
    public void testGetFilterType() {
        FilterType expResult = FilterType.FREQUENCY_FILTER;

        assertThat(instance.getFilterType(), equalTo(expResult));
    }

    @Test
    public void testGetMessages() {
        List<String> expectedMessages = new ArrayList<>();
        expectedMessages.add(MESSAGE);
        assertThat(instance.getMessages(), equalTo(expectedMessages));
    }

    @Test
    public void testHasMessagesIsFalseWhenNoMessagesAdded() {
        FilterReport instance = new FilterReport(FilterType.FREQUENCY_FILTER, 0, 0, Collections.emptyList());
        assertThat(instance.hasMessages(), is(false));
    }

    @Test
    public void testHasMessagesIsTrueWhenMessagesAdded() {
        assertThat(instance.hasMessages(), is(true));
    }

    @Test
    public void testGetPassed() {
        assertThat(instance.getPassed(), equalTo(PASSED));
    }

    @Test
    public void testGetFailed() {
        assertThat(instance.getFailed(), equalTo(FAILED));
    }

    @Test
    public void testHashCode() {
        FilterReport other = new FilterReport(FilterType.FREQUENCY_FILTER, PASSED, FAILED, List.of(MESSAGE));
        int expResult = other.hashCode();
        assertThat(instance.hashCode(), equalTo(expResult));

    }

    @Test
    public void testEqualsNullIsFalse() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testEqualsOtherClassIsFalse() {
        Object obj = "string";
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testEqualsOtherFilterReportWithDifferentMessageIsFalse() {
        FilterReport other = new FilterReport(FilterType.FREQUENCY_FILTER, PASSED, FAILED, Collections.emptyList());
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testEqualsOtherFilterReportWithOtherPassFailIsFalse() {
        FilterReport other = new FilterReport(FilterType.FREQUENCY_FILTER, FAILED, PASSED, Collections.emptyList());
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testEqualsOtherFilterReport() {
        FilterReport other = new FilterReport(FilterType.FREQUENCY_FILTER, PASSED, FAILED, List.of(MESSAGE));
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualsOtherFilterReportWithDifferentFailedNumber() {
        FilterReport other = new FilterReport(FilterType.FREQUENCY_FILTER, PASSED, PASSED, Collections.emptyList());
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualsOtherFilterReportOfDifferentFilterType() {
        FilterReport other = new FilterReport(FilterType.INHERITANCE_FILTER, PASSED, FAILED, Collections.emptyList());
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testToString() {
        String expResult = String.format("FilterReport for %s: pass:%d fail:%d %s", FilterType.FREQUENCY_FILTER, PASSED, FAILED, List.of(MESSAGE));
        assertThat(instance.toString(), equalTo(expResult));
    }

}
