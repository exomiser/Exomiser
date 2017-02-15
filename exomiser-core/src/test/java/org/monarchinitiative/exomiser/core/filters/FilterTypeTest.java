/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterTypeTest {

    /**
     * Test of valueOf method, of class FilterType.
     */
    @Test
    public void testValueOf() {
        String name = "FREQUENCY_FILTER";
        FilterType expResult = FilterType.FREQUENCY_FILTER;
        FilterType result = FilterType.valueOf(name);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGenePriorityScoreFilter() {
        FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;
        String name = "PRIORITY_SCORE_FILTER";
        assertThat(FilterType.valueOf(name), equalTo(filterType));
        assertThat(filterType.toString(), equalTo("Gene priority score"));
    }

    @Test
    public void testHasFailedVariantFilter() {
        FilterType filterType = FilterType.FAILED_VARIANT_FILTER;
        String name = "FAILED_VARIANT_FILTER";
        assertThat(FilterType.valueOf(name), equalTo(filterType));
        assertThat(filterType.toString(), equalTo("Failed upstream analysis"));
    }
}
