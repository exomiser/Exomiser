/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataServiceMock;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;

import java.util.Collections;
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
    private final PathogenicityData defaultPathogenicityData = new PathogenicityData(new HashSet<>());

    @Before
    public void setUp() {
        variantDataService = new VariantDataServiceMock();
        variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").pathogenicityData(defaultPathogenicityData).build();
    }

    @Test
    public void testRunFilter() {
    }

    @Test
    public void testGetFilterType() {
        VariantFilter decoratedFilter = new PathogenicityFilter(true);
        instance = new PathogenicityDataProvider(variantDataService, EMPTY_SET, decoratedFilter);
        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
    }
    
}
