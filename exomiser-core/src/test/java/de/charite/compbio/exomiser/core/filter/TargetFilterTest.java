/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.filter.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filter.TargetFilter;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
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
    
    private static VariantEvaluation missensePassesFilter;
    private static VariantEvaluation downstreamFailsFilter;
    private static VariantEvaluation synonymousFailsFilter;
    private static VariantEvaluation upstreamFailsFilter;
    private static VariantEvaluation intergenicFailsFilter;

    @Mock
    Variant missenseVariant;
    @Mock
    Variant downstreamVariant;
    @Mock
    Variant synonymousVariant;
    @Mock
    Variant upstreamVariant;
    @Mock
    Variant intergenicVariant;

    public TargetFilterTest() {
    }

    @Before
    public void setUp() {
        instance = new TargetFilter();
        
        MockitoAnnotations.initMocks(this);

        Mockito.when(missenseVariant.getVariantTypeConstant()).thenReturn(VariantType.MISSENSE);
        Mockito.when(downstreamVariant.getVariantTypeConstant()).thenReturn(VariantType.DOWNSTREAM);
        Mockito.when(synonymousVariant.getVariantTypeConstant()).thenReturn(VariantType.SYNONYMOUS);
        Mockito.when(upstreamVariant.getVariantTypeConstant()).thenReturn(VariantType.UPSTREAM);
        Mockito.when(intergenicVariant.getVariantTypeConstant()).thenReturn(VariantType.INTERGENIC);

        missensePassesFilter = new VariantEvaluation(missenseVariant);
        downstreamFailsFilter = new VariantEvaluation(downstreamVariant);
        synonymousFailsFilter = new VariantEvaluation(synonymousVariant);
        upstreamFailsFilter = new VariantEvaluation(upstreamVariant);
        intergenicFailsFilter = new VariantEvaluation(intergenicVariant);
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
