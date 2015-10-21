/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataServiceMock;
import de.charite.compbio.exomiser.core.model.FilterStatus;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import static de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource.*;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataProviderTest {
    
    private static final Set<PathogenicitySource> EMPTY_SET = Collections.emptySet();

    private PathogenicityDataProvider instance;
    private VariantDataServiceMock variantDataService;

    private VariantEvaluation variant;
    private static final PathogenicityData EXPECTED_PATH_DATA = new PathogenicityData(new PolyPhenScore(1f), new SiftScore(0f), new MutationTasterScore(1f));
    private static final PathogenicityData EMPTY_PATH_DATA = new PathogenicityData(new HashSet<>());

    @Before
    public void setUp() {
        variantDataService = new VariantDataServiceMock();
        variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").pathogenicityData(EMPTY_PATH_DATA).build();
        variantDataService.put(variant, EXPECTED_PATH_DATA);
    }

    @Test
    public void testProvideVariantData() {
        instance = new PathogenicityDataProvider(variantDataService, EnumSet.of(POLYPHEN, SIFT, MUTATION_TASTER), new StubPassAllVariantFilter(FilterType.PATHOGENICITY_FILTER));
                
        assertThat(variant.getPathogenicityData(), equalTo(EMPTY_PATH_DATA));
        instance.provideVariantData(variant);
        assertThat(variant.getPathogenicityData(), equalTo(EXPECTED_PATH_DATA));
    }
    
    @Test
    public void testRunFilter() {
        FilterType variantFilterType = FilterType.PATHOGENICITY_FILTER;
        instance = new PathogenicityDataProvider(variantDataService, EnumSet.of(POLYPHEN, SIFT, MUTATION_TASTER), new StubPassAllVariantFilter(variantFilterType));
                
        assertThat(variant.getPathogenicityData(), equalTo(EMPTY_PATH_DATA));
        assertThat(variant.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
        
        FilterResult filterResult = instance.runFilter(variant);
        
        assertThat(variant.getPathogenicityData(), equalTo(EXPECTED_PATH_DATA));
        assertThat(filterResult.getFilterType(), equalTo(variantFilterType));
    }

    @Test
    public void testGetFilterType() {
        VariantFilter decoratedFilter = new PathogenicityFilter(true);
        instance = new PathogenicityDataProvider(variantDataService, EMPTY_SET, decoratedFilter);
        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
    }
 
    @Test
    public void testGetDecoratedFilter() {
        VariantFilter decoratedFilter = new PathogenicityFilter(true);
        instance = new PathogenicityDataProvider(variantDataService, EMPTY_SET, decoratedFilter);
        assertThat(instance.getDecoratedFilter(), equalTo(decoratedFilter));
    }
}
