/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataServiceMock;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.VariantEvaluation.VariantBuilder;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class RegulatoryFeatureDataProviderTest {
    
    private RegulatoryFeatureDataProvider instance;
    private VariantDataServiceMock variantDataService;

    private VariantEvaluation nonRegulatoryNonCodingVariant;
    private VariantEvaluation regulatoryNonCodingVariant;
    private VariantEvaluation missenseCodingVariant;

    private VariantBuilder variantBuilder;
    
    @Before
    public void setUp() {
        variantDataService = new VariantDataServiceMock();
        variantBuilder = new VariantEvaluation.VariantBuilder(1, 1, "A", "T");
        
        nonRegulatoryNonCodingVariant = variantBuilder.variantEffect(VariantEffect.INTERGENIC_VARIANT).build();
        regulatoryNonCodingVariant = variantBuilder.variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT).build();
        missenseCodingVariant = variantBuilder.variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        
    }
    
    @Test
    public void testGetFilterType() {
        VariantFilter decoratedFilter = new RegulatoryFeatureFilter();
        instance = new RegulatoryFeatureDataProvider(variantDataService, decoratedFilter);
        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
    }
    
    @Ignore
    @Test
    public void testRunFilter_NonRegulatoryNonCodingVariant() {
        variantDataService.put(regulatoryNonCodingVariant, VariantEffect.REGULATORY_REGION_VARIANT);
        
        instance = new RegulatoryFeatureDataProvider(variantDataService, new StubPassAllVariantFilter(FilterType.REGULATORY_FEATURE_FILTER));
        FilterResult filterResult = instance.runFilter(regulatoryNonCodingVariant);
        
        assertThat(regulatoryNonCodingVariant.getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
        assertThat(filterResult.getFilterType(), equalTo(FilterType.REGULATORY_FEATURE_FILTER));
        assertThat(filterResult.passedFilter(), is(true));
    }
    
}
