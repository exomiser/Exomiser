/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.VariantEvaluation.VariantBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }
    
    @Test
    public void testFilterVariantOfLowQualityFailsFilter() {
        FilterResult filterResult = instance.runFilter(lowQualityFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
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
        Object obj = new TargetFilter();
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
