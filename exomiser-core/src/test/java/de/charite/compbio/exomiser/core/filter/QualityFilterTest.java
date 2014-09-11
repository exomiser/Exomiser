/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class QualityFilterTest {
    
    QualityFilter instance;

    private static final float MIN_QUAL_THRESHOLD = 3.0f;
    private static final float OVER_THRESHOLD = MIN_QUAL_THRESHOLD + 1.0f;
    private static final float UNDER_THRESHOLD = MIN_QUAL_THRESHOLD - 1.0f;
    
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

        MockitoAnnotations.initMocks(this);

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

    @Test
    public void testFilterVariants() {
        List<VariantEvaluation> variantList = new ArrayList<>();
        
        variantList.add(highQualityPassesFilter);
        variantList.add(lowQualityFailsFilter);

        
        instance.filterVariants(variantList);
        
        Set failedFilterSet = EnumSet.of(FilterType.QUALITY_FILTER);

        assertThat(highQualityPassesFilter.passesFilters(), is(true));
        assertThat(highQualityPassesFilter.getFailedFilters().isEmpty(), is(true));

        assertThat(lowQualityFailsFilter.passesFilters(), is(false));
        assertThat(lowQualityFailsFilter.getFailedFilters(), equalTo(failedFilterSet));
    }

    @Test
    public void testFilterVariantOfHighQualityIsTrue() {
        assertThat(instance.filterVariant(highQualityPassesFilter), is(true));
    }
    
    @Test
    public void testFilterVariantOfLowQualityIsFalse() {
        assertThat(instance.filterVariant(lowQualityFailsFilter), is(false));
    }

    @Test
    public void testPassesFilterOverThresholdIsTrue() {
        assertThat(instance.passesFilter(OVER_THRESHOLD), is(true));
    }

    @Test
    public void testPassesFilterUnderThresholdIsFalse() {
        assertThat(instance.passesFilter(UNDER_THRESHOLD), is(false));
    }

    
    @Test
    public void testHashCode() {
        Filter qualityFilter = new QualityFilter(MIN_QUAL_THRESHOLD);
        assertThat(instance.hashCode(), equalTo(qualityFilter.hashCode()));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
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
