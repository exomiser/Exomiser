/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataServiceMock;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static de.charite.compbio.exomiser.core.filters.FilterTestHelper.assertPassed;
import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProviderTest {
    
    private static final Set<FrequencySource> EMPTY_SET = Collections.emptySet();

    private FrequencyDataProvider instance;
    private VariantDataServiceMock variantDataService;

    private VariantEvaluation variant;
    private final FrequencyData defaultFrequencyData = new FrequencyData(null, new HashSet<>());

    @Before
    public void setUp() {
        variantDataService = new VariantDataServiceMock();
        variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").frequencyData(defaultFrequencyData).build();
    }
        
    @Test
    public void testReturnsFilterTypeOfDecoratedFilter_FrequencyFilter() {
        final VariantFilter decoratedFilter = new FrequencyFilter(100f);

        instance = new FrequencyDataProvider(variantDataService, EMPTY_SET, decoratedFilter);

        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testReturnsFilterTypeOfDecoratedFilter_KnownVariantFilter() {
        final KnownVariantFilter decoratedFilter = new KnownVariantFilter();

        instance = new FrequencyDataProvider(variantDataService, EMPTY_SET, decoratedFilter);

        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testProvidesFrequencyDataForVariantWhenRun() {
        FrequencyData expectedData = new FrequencyData(new RsId(123456), new Frequency(1.0f, ESP_ALL));
        variantDataService.put(variant, expectedData);
       
        instance = new FrequencyDataProvider(variantDataService, EnumSet.allOf(FrequencySource.class), new KnownVariantFilter());
        assertThat(variant.getFrequencyData(), equalTo(defaultFrequencyData));

        instance.runFilter(variant);
        assertThat(variant.getFrequencyData(), equalTo(expectedData));
    }

    @Test
    public void testFilterResultIsThatOfDecoratedFilter() {
        final KnownVariantFilter decoratedFilter = new KnownVariantFilter();

        instance = new FrequencyDataProvider(variantDataService, EMPTY_SET, decoratedFilter);

        FilterResult filterResult = instance.runFilter(variant);
        assertPassed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(decoratedFilter.getFilterType()));
    }

    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSources_NoDataInDatabase() {
        final KnownVariantFilter decoratedFilter = new KnownVariantFilter();

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(ESP_ALL), decoratedFilter);
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(new FrequencyData(null, new HashSet<>())));
    }

    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSources_OneSourceSpecifiedAllDataSourcesInDatabase() {

        Frequency espAll = new Frequency(0.01f, ESP_ALL);
        FrequencyData variantFrequencyData = new FrequencyData(new RsId(123456), espAll, new Frequency(0.234f, EXAC_AFRICAN_INC_AFRICAN_AMERICAN), new Frequency(0.02f, EXAC_FINNISH));
        
        variantDataService.put(variant, variantFrequencyData);

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(espAll.getSource()), new KnownVariantFilter());
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(new FrequencyData(new RsId(123456), espAll)));
    }
    
    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSources_TwoSourcesSpecifiedAllDataSourcesInDatabase() {

        Frequency espAll = new Frequency(0.01f, ESP_ALL);
        Frequency exacAfr = new Frequency(0.234f, EXAC_AFRICAN_INC_AFRICAN_AMERICAN);
        FrequencyData variantFrequencyData = new FrequencyData(new RsId(123456), espAll, exacAfr, new Frequency(0.02f, EXAC_FINNISH));

        variantDataService.put(variant, variantFrequencyData);

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(espAll.getSource(), exacAfr.getSource()), new KnownVariantFilter());
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(new FrequencyData(new RsId(123456), espAll, exacAfr)));
    }
    
    @Test
    public void testGetDecoratedFilter() {
        VariantFilter decoratedFilter = new KnownVariantFilter();
        instance = new FrequencyDataProvider(variantDataService, EnumSet.noneOf(FrequencySource.class), decoratedFilter);
        assertThat(instance.getDecoratedFilter(), equalTo(decoratedFilter));
    }
}
