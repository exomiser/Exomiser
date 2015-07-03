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
    public void testGetScore() {
        float expResult = 0.0F;
        FilterResult instance = new PassFilterResult(FilterType.BED_FILTER, expResult);
        float result = instance.getScore();
        assertEquals(expResult, result, 0.0);
    }

    @Test
    public void testGetFilterType() {
        FilterType expResult = FilterType.FREQUENCY_FILTER;

        FilterResult instance = new PassFilterResult(expResult, 0.0f);

        FilterType result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    @Test
    public void testPassedFilterIsTrueWhenFilterResultStatusIsPass() {
        FilterResult instance = new PassFilterResult(FilterType.FREQUENCY_FILTER, 0.0f);
        assertThat(instance.passedFilter(), is(true));
    }

    @Test
    public void testPassedFilterIsFalseWhenFilterResultStatusIsFail() {
        FilterResult instance = new FailFilterResult(FilterType.FREQUENCY_FILTER, 0.0f);
        assertThat(instance.passedFilter(), is(false));
    }

    @Test
    public void testGetResultStatus() {
        FilterResult instance = new PassFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        assertThat(instance.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testHashCode() {
        FilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        FilterResult another = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        int expResult = another.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testNotEqualToNullObject() {
        Object obj = null;
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        assertThat(instance, not(equalTo(obj)));
    }
    
    @Test
    public void testNotEqualToDifferentFilterType() {
        AbstractFilterResult other = new FailFilterResult(FilterType.BED_FILTER, 0.0f);
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFilterResult() {
        AbstractFilterResult other = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        AbstractFilterResult instance = new FailFilterResult(FilterType.INTERVAL_FILTER, 0.0f);
        assertThat(instance, equalTo(other));
    }

    @Test
    public void testToString() {
        AbstractFilterResult instance = new PassFilterResult(FilterType.INTERVAL_FILTER, 1.0f);
        String expResult = "Filter=Interval score=1.000 status=PASS";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
