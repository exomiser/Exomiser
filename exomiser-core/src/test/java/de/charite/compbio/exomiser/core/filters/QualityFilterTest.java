/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class QualityFilterTest {
    
    private QualityFilter instance;

    private static final double MIN_QUAL_THRESHOLD = 3.0f;
    private static final double OVER_THRESHOLD = MIN_QUAL_THRESHOLD + 1.0f;
    private static final double UNDER_THRESHOLD = MIN_QUAL_THRESHOLD - 1.0f;
    
    private static VariantEvaluation highQualityPassesFilter;
    private static VariantEvaluation lowQualityFailsFilter;

    @Mock
    Variant mockHighQualityVariant;
    @Mock
    Variant mockLowQualityVariant;

    public QualityFilterTest() {
    }

    @Before
    public void setUp() {

        Mockito.when(mockHighQualityVariant.getVariantPhredScore()).thenReturn(OVER_THRESHOLD);
        Mockito.when(mockLowQualityVariant.getVariantPhredScore()).thenReturn(UNDER_THRESHOLD);
        
        highQualityPassesFilter = new VariantEvaluation(mockHighQualityVariant);
        lowQualityFailsFilter = new VariantEvaluation(mockLowQualityVariant);
        
        instance = new QualityFilter(MIN_QUAL_THRESHOLD);
        
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
