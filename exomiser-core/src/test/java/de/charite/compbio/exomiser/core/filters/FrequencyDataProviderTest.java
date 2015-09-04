package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataProviderTest {

    private FrequencyDataProvider instance;
    private final VariantDataService variantDataService = new VariantDataServiceStub();

    private VariantEvaluation variant;
    private final FrequencyData defaultFrequencyData = new FrequencyData(null, new HashSet<>());

    @Before
    public void setUp() {
        variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").frequencyData(defaultFrequencyData).build();
    }

    @Test
    public void testCanDecorateFrequencyFilter() {
        instance = new FrequencyDataProvider(variantDataService, new FrequencyFilter(100f));
        assertThat(instance.getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testCanDecorateKnownVariantFilter() {
        instance = new FrequencyDataProvider(variantDataService, new KnownVariantFilter());
        assertThat(instance.getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
        assertThat(instance.isVariantFilter(), is(true));
    }

    @Test
    public void testProvidesFrequencyDataForVariantWhenRun() {
        instance = new FrequencyDataProvider(variantDataService, new KnownVariantFilter());
        assertThat(variant.getFrequencyData(), equalTo(defaultFrequencyData));

        instance.runFilter(variant);
        assertThat(variant.getFrequencyData(), equalTo(new FrequencyData(new RsId(123456), new HashSet<>())));
    }

    @Test
    public void testFilterResultIsThatOfDecoratedFilter() {
        instance = new FrequencyDataProvider(variantDataService, new KnownVariantFilter());

        FilterResult filterResult = instance.runFilter(variant);
        assertThat(filterResult.passedFilter(), is(false));
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
        assertThat(filterResult.getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
    }

}
