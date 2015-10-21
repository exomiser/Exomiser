/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Test;
import static org.junit.Assert.*;

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
        assertThat(instance.passedFilter(), is(true));
    }

    @Test
    public void testPassedFilterIsFalseWhenFilterResultStatusIsFail() {
        FilterResult instance = new FailFilterResult(FilterType.FREQUENCY_FILTER);
        assertThat(instance.passedFilter(), is(false));
    }

    @Test
    public void testGetResultStatus() {
        FilterResult instance = new PassFilterResult(FilterType.INTERVAL_FILTER);
        assertThat(instance.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

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
