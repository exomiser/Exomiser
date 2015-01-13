package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.exome.Variant;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FrequencyFilterTest {

    FrequencyFilter instance;

    private VariantEvaluation passesEspAllFrequency;
    private VariantEvaluation passesEspAAFrequency;
    private VariantEvaluation passesEspEAFrequency;
    private VariantEvaluation passesDbsnpFrequency;

    private VariantEvaluation failsFrequency;
    private VariantEvaluation passesNoFrequencyData;

    private VariantEvaluation nullFrequencyVariant;

    @Mock
    Variant mockVariant;

    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;

    private static final Frequency ESP_ALL_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_ALL_FAIL = new Frequency(FAIL_FREQ);

    private static final Frequency ESP_AA_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_AA_FAIL = new Frequency(FAIL_FREQ);

    private static final Frequency ESP_EA_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_EA_FAIL = new Frequency(FAIL_FREQ);

    private static final Frequency DBSNP_PASS = new Frequency(PASS_FREQ);
    private static final Frequency DBSNP_FAIL = new Frequency(FAIL_FREQ);

    private static final FrequencyData espAllPassData = new FrequencyData(null, null, ESP_ALL_PASS, null, null);
    private static final FrequencyData espAllFailData = new FrequencyData(null, null, ESP_ALL_FAIL, null, null);
    private static final FrequencyData espAaPassData = new FrequencyData(null, null, null, ESP_AA_PASS, null);
    private static final FrequencyData espEaPassData = new FrequencyData(null, null, null, null, ESP_EA_PASS);
    private static final FrequencyData dbSnpPassData = new FrequencyData(null, DBSNP_PASS, null, null, null);
    private static final FrequencyData noFreqData = new FrequencyData(null, null, null, null, null);

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        boolean filterOutAllKnownVariants = false;

        instance = new FrequencyFilter(FREQ_THRESHOLD, filterOutAllKnownVariants);

        passesEspAllFrequency = new VariantEvaluation(mockVariant);
        passesEspAllFrequency.setFrequencyData(espAllPassData);

        passesEspAAFrequency = new VariantEvaluation(mockVariant);
        passesEspAAFrequency.setFrequencyData(espAaPassData);

        passesEspEAFrequency = new VariantEvaluation(mockVariant);
        passesEspEAFrequency.setFrequencyData(espEaPassData);

        passesDbsnpFrequency = new VariantEvaluation(mockVariant);
        passesDbsnpFrequency.setFrequencyData(dbSnpPassData);

        failsFrequency = new VariantEvaluation(mockVariant);
        failsFrequency.setFrequencyData(espAllFailData);

        passesNoFrequencyData = new VariantEvaluation(mockVariant);
        passesNoFrequencyData.setFrequencyData(noFreqData);

        nullFrequencyVariant = new VariantEvaluation(mockVariant);
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithNegativeFrequency() {
        instance = new FrequencyFilter(-1f, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenInstanciatedWithFrequencyGreaterThanOneHundredPercent() {
        instance = new FrequencyFilter(101f, true);
    }

    @Test
    public void testFilterFailsVariantEvaluationWithNullFrequency() {
        boolean filterOutAllKnownVariants = true;

        instance = new FrequencyFilter(FREQ_THRESHOLD, filterOutAllKnownVariants);

        FilterResult filterResult = instance.runFilter(nullFrequencyVariant);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testFilterPassesVariantEvaluationWithFrequencyUnderThreshold() {
        boolean filterOutAllKnownVariants = false;

        instance = new FrequencyFilter(FREQ_THRESHOLD, filterOutAllKnownVariants);
        System.out.println(passesEspAllFrequency + " " + passesEspAllFrequency.getFrequencyData());
        FilterResult filterResult = instance.runFilter(passesEspAllFrequency);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testFilterFailsVariantEvaluationWithFrequencyUnderThresholdBecauseItHasBeenCharacterised() {
        boolean failAllKnownVariants = true;

        instance = new FrequencyFilter(FREQ_THRESHOLD, failAllKnownVariants);
        System.out.println(passesEspAllFrequency + " " + passesEspAllFrequency.getFrequencyData());
        FilterResult filterResult = instance.runFilter(passesEspAllFrequency);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testFilterPassesVariantEvaluationWithNoFrequencyData() {

        FilterResult filterResult = instance.runFilter(passesNoFrequencyData);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testFilterPassesVariantEvaluationWithNoFrequencyDataWhenToldToFailKnownVariants() {

        boolean failAllKnownVariants = true;

        instance = new FrequencyFilter(FREQ_THRESHOLD, failAllKnownVariants);

        FilterResult filterResult = instance.runFilter(passesNoFrequencyData);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testFilterFailsVariantEvaluationWithFrequencyDataAboveThreshold() {

        FilterResult filterResult = instance.runFilter(failsFrequency);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testPassesFilterFails() {
        assertThat(instance.passesFilter(espAllFailData), is(false));
    }

    @Test
    public void testEaspAllPassesFilter() {
        assertThat(instance.passesFilter(espAllPassData), is(true));
    }

    @Test
    public void testNoFrequencyDataPassesFilter() {
        assertThat(instance.passesFilter(noFreqData), is(true));
    }

    @Test
    public void testHashCode() {
        FrequencyFilter otherFilter = new FrequencyFilter(FREQ_THRESHOLD, false);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testNotEqualNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherObject() {
        Object obj = "Not equal to this";
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualOtherFrequencyFilterWithDifferentThreshold() {
        FrequencyFilter otherFilter = new FrequencyFilter(FAIL_FREQ, false);
        assertThat(instance.equals(otherFilter), is(false));
    }

    @Test
    public void testNotEqualOtherFrequencyFilterWithKnownVariantSwitch() {
        FrequencyFilter otherFilter = new FrequencyFilter(FREQ_THRESHOLD, true);
        assertThat(instance.equals(otherFilter), is(false));
    }

    @Test
    public void testEqualsSelf() {
        assertThat(instance.equals(instance), is(true));
    }

//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        FrequencyFilter instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
