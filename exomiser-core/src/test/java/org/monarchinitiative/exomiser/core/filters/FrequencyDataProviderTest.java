/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceMock;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProviderTest {
    
    private static final Set<FrequencySource> EMPTY_SET = Collections.emptySet();

    private FrequencyDataProvider instance;
    private VariantDataServiceMock variantDataService;

    private VariantEvaluation variant;
    private final FrequencyData defaultFrequencyData = FrequencyData.empty();

    @BeforeEach
    public void setUp() {
        variantDataService = new VariantDataServiceMock();
        variant = VariantEvaluation.builder(1, 1, "A", "T").frequencyData(defaultFrequencyData).build();
    }
        
    @Test
    public void testReturnsFilterTypeOfDecoratedFilterFrequencyFilter() {
        final VariantFilter decoratedFilter = new FrequencyFilter(100f);

        instance = new FrequencyDataProvider(variantDataService, EMPTY_SET, decoratedFilter);

        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testReturnsFilterTypeOfDecoratedFilterKnownVariantFilter() {
        final KnownVariantFilter decoratedFilter = new KnownVariantFilter();

        instance = new FrequencyDataProvider(variantDataService, EMPTY_SET, decoratedFilter);

        assertThat(instance.getFilterType(), equalTo(decoratedFilter.getFilterType()));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testProvidesFrequencyDataForVariantWhenRun() {
        FrequencyData expectedData = FrequencyData.of(RsId.of(123456), Frequency.of(ESP_ALL, 1.0f));
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
        FilterTestHelper.assertPassed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(decoratedFilter.getFilterType()));
    }

    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSourcesNoDataInDatabase() {
        final KnownVariantFilter decoratedFilter = new KnownVariantFilter();

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(ESP_ALL), decoratedFilter);
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(FrequencyData.empty()));
    }

    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSourcesOneSourceSpecifiedAllDataSourcesInDatabase() {

        Frequency espAll = Frequency.of(ESP_ALL, 0.01f);
        FrequencyData variantFrequencyData = FrequencyData.of(RsId.of(123456), espAll, Frequency.of(EXAC_AFRICAN_INC_AFRICAN_AMERICAN, 0.234f), Frequency
                .of(EXAC_FINNISH, 0.02f));
        
        variantDataService.put(variant, variantFrequencyData);

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(espAll.getSource()), new KnownVariantFilter());
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(FrequencyData.of(RsId.of(123456), espAll)));
    }
    
    @Test
    public void testFrequencyDataOnlyContainsSpecifiedSourcesTwoSourcesSpecifiedAllDataSourcesInDatabase() {

        Frequency espAll = Frequency.of(ESP_ALL, 0.01f);
        Frequency exacAfr = Frequency.of(EXAC_AFRICAN_INC_AFRICAN_AMERICAN, 0.234f);
        FrequencyData variantFrequencyData = FrequencyData.of(RsId.of(123456), espAll, exacAfr, Frequency.of(EXAC_FINNISH, 0.02f));

        variantDataService.put(variant, variantFrequencyData);

        instance = new FrequencyDataProvider(variantDataService, EnumSet.of(espAll.getSource(), exacAfr.getSource()), new KnownVariantFilter());
        instance.runFilter(variant);

        assertThat(variant.getFrequencyData(), equalTo(FrequencyData.of(RsId.of(123456), espAll, exacAfr)));
    }
    
    @Test
    public void testGetDecoratedFilter() {
        VariantFilter decoratedFilter = new KnownVariantFilter();
        instance = new FrequencyDataProvider(variantDataService, EnumSet.noneOf(FrequencySource.class), decoratedFilter);
        assertThat(instance.getDecoratedFilter(), equalTo(decoratedFilter));
    }
}
