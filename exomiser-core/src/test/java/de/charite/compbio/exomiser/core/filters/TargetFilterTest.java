/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author jj8
 */
@RunWith(MockitoJUnitRunner.class)
public class TargetFilterTest {

    private TargetFilter instance;
    
    @Mock
    VariantEvaluation missensePassesFilter;
    @Mock
    VariantEvaluation downstreamFailsFilter;
    @Mock
    VariantEvaluation synonymousFailsFilter;
    @Mock
    VariantEvaluation upstreamFailsFilter;
    @Mock
    VariantEvaluation intergenicFailsFilter;

    public TargetFilterTest() {
    }

    @Before
    public void setUp() {
        instance = new TargetFilter();
        
        Mockito.when(missensePassesFilter.getVariantEffect()).thenReturn(VariantEffect.MISSENSE_VARIANT);
        Mockito.when(downstreamFailsFilter.getVariantEffect()).thenReturn(VariantEffect.DOWNSTREAM_GENE_VARIANT);
        Mockito.when(synonymousFailsFilter.getVariantEffect()).thenReturn(VariantEffect.SYNONYMOUS_VARIANT);
        Mockito.when(upstreamFailsFilter.getVariantEffect()).thenReturn(VariantEffect.UPSTREAM_GENE_VARIANT);
        Mockito.when(intergenicFailsFilter.getVariantEffect()).thenReturn(VariantEffect.INTERGENIC_VARIANT);
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.TARGET_FILTER));
    }

    @Test
    public void testMissenseVariantPassesFilter() {
        FilterResult filterResult = instance.runFilter(missensePassesFilter);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testDownstreamVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(downstreamFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testIntergenicVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(intergenicFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }
    
    @Test
    public void testUpstreamVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(upstreamFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }
    
    @Test
    public void testSynonymousVariantFailsFilter() {
       FilterResult filterResult = instance.runFilter(synonymousFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }
    
    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherFilter() {
        PathogenicityFilter obj = new PathogenicityFilter(true);
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testEqualOtherTagetFilter() {
        TargetFilter obj = new TargetFilter();
        assertThat(instance.equals(obj), is(true));
    }

    @Test
    public void testHashCode() {
        TargetFilter anotherTargetFilter = new TargetFilter();
        assertThat(instance.hashCode(), equalTo(anotherTargetFilter.hashCode()));
    }
}
