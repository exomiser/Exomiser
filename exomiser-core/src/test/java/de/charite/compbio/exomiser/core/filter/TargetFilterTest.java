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
        TargetFilter instance = new TargetFilter();
        assertThat(instance.getFilterType(), equalTo(FilterType.TARGET_FILTER));
    }

    @Test
    public void testFilterVariants() {
        List<VariantEvaluation> variantList = new ArrayList<>();
        
        variantList.add(missensePassesFilter);
        variantList.add(downstreamFailsFilter);
        variantList.add(intergenicFailsFilter);
        variantList.add(upstreamFailsFilter);
        variantList.add(synonymousFailsFilter);
        
        TargetFilter instance = new TargetFilter();
        instance.filterVariants(variantList);
        
        Set failedFilterSet = EnumSet.of(FilterType.TARGET_FILTER);

        assertThat(missensePassesFilter.passesFilters(), is(true));
        assertThat(missensePassesFilter.getFailedFilters().isEmpty(), is(true));

        assertThat(downstreamFailsFilter.passesFilters(), is(false));
        assertThat(downstreamFailsFilter.getFailedFilters(), equalTo(failedFilterSet));

        assertThat(intergenicFailsFilter.passesFilters(), is(false));
        assertThat(intergenicFailsFilter.getFailedFilters(), equalTo(failedFilterSet));

        assertThat(upstreamFailsFilter.passesFilters(), is(false));
        assertThat(upstreamFailsFilter.getFailedFilters(), equalTo(failedFilterSet));

        assertThat(synonymousFailsFilter.passesFilters(), is(false));
        assertThat(synonymousFailsFilter.getFailedFilters(), equalTo(failedFilterSet));

    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        TargetFilter instance = new TargetFilter();
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testNotEqualOtherFilter() {
        PathogenicityFilter obj = new PathogenicityFilter(true);
        TargetFilter instance = new TargetFilter();
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testEqualOtherTagetFilter() {
        TargetFilter obj = new TargetFilter();
        TargetFilter instance = new TargetFilter();
        assertThat(instance.equals(obj), is(true));
    }

}
