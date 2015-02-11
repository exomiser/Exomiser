/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.GenericFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenericFilterResultTest {

    public GenericFilterResultTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testGetScore() {
        float expResult = 0.0F;
        GenericFilterResult instance = new GenericFilterResult(FilterType.BED_FILTER, expResult, FilterResultStatus.PASS);
        float result = instance.getScore();
        assertEquals(expResult, result, 0.0);
    }

    @Test
    public void testGetFilterType() {
        FilterType expResult = FilterType.FREQUENCY_FILTER;

        GenericFilterResult instance = new GenericFilterResult(expResult, 0.0f, FilterResultStatus.PASS);

        FilterType result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    @Test
    public void testPassedFilterIsTrueWhenFilterResultStatusIsPass() {
        FilterResultStatus resultStatus = FilterResultStatus.PASS;

        GenericFilterResult instance = new GenericFilterResult(FilterType.FREQUENCY_FILTER, 0.0f, resultStatus);

        assertThat(instance.passedFilter(), is(true));
    }

    @Test
    public void testPassedFilterIsFalseWhenFilterResultStatusIsFail() {
        FilterResultStatus resultStatus = FilterResultStatus.FAIL;

        GenericFilterResult instance = new GenericFilterResult(FilterType.FREQUENCY_FILTER, 0.0f, resultStatus);

        assertThat(instance.passedFilter(), is(false));
    }

    @Test
    public void testGetResultStatus() {
        FilterResultStatus expResult = FilterResultStatus.PASS;
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, expResult);
        FilterResultStatus result = instance.getResultStatus();
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        GenericFilterResult another = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        int expResult = another.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testNotEqualToNullObject() {
        Object obj = null;
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        assertThat(instance, not(equalTo(obj)));
    }
    
    @Test
    public void testNotEqualToDifferentFilterType() {
        GenericFilterResult other = new GenericFilterResult(FilterType.BED_FILTER, 0.0f, FilterResultStatus.FAIL);
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFilterResult() {
        GenericFilterResult other = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 0.0f, FilterResultStatus.FAIL);
        assertThat(instance, equalTo(other));
    }

    @Test
    public void testToString() {
        GenericFilterResult instance = new GenericFilterResult(FilterType.INTERVAL_FILTER, 1.0f, FilterResultStatus.PASS);
        String expResult = "Filter=Interval score=1.000 status=PASS";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
