/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterTest {

    private TargetFilter instance;
    
    VariantEvaluation missensePassesFilter;
    VariantEvaluation downstreamFailsFilter;
    VariantEvaluation synonymousFailsFilter;
    VariantEvaluation upstreamFailsFilter;
    VariantEvaluation intergenicFailsFilter;

    @Before
    public void setUp() {
        instance = new TargetFilter();
        
        missensePassesFilter = testVariantBuilder().variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        downstreamFailsFilter = testVariantBuilder().variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT).build();
        synonymousFailsFilter = testVariantBuilder().variantEffect(VariantEffect.SYNONYMOUS_VARIANT).build();
        upstreamFailsFilter = testVariantBuilder().variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT).build();
        intergenicFailsFilter = testVariantBuilder().variantEffect(VariantEffect.INTERGENIC_VARIANT).build();
    }

    private VariantEvaluation.VariantBuilder testVariantBuilder() {
        return new VariantEvaluation.VariantBuilder(1, 1, "A", "T");
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

    @Ignore  
    @Test
    public void testIntergenicVariantFailsFilter() {
        FilterResult filterResult = instance.runFilter(intergenicFailsFilter);
        
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }
    
    @Ignore    
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
