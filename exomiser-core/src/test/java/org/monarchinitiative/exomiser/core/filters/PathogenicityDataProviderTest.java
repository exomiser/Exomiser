/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantDataService;
import org.monarchinitiative.exomiser.core.model.FilterStatus;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataProviderTest {
    
    private static final Set<PathogenicitySource> EMPTY_SET = Collections.emptySet();

    private PathogenicityDataProvider instance;
    private TestVariantDataService variantDataService;

    private VariantEvaluation variant;
    private static final PathogenicityData EXPECTED_PATH_DATA = PathogenicityData.of(PolyPhenScore.of(1f), SiftScore
            .of(0f), MutationTasterScore.of(1f));
    private static final PathogenicityData EMPTY_PATH_DATA = PathogenicityData.empty();

    @BeforeEach
    public void setUp() {
        variant = TestFactory.variantBuilder(1, 1, "A", "T").pathogenicityData(EMPTY_PATH_DATA).build();
        variantDataService = TestVariantDataService.builder()
                .put(variant, EXPECTED_PATH_DATA)
                .build();
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
