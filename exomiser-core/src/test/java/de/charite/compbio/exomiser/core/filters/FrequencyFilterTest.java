package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FrequencyFilterTest {
    
    private FrequencyFilter instance;

    private VariantEvaluation passesEspAllFrequency;
    private VariantEvaluation passesEspAAFrequency;
    private VariantEvaluation passesEspEAFrequency;
    private VariantEvaluation passesDbsnpFrequency;

    private VariantEvaluation failsFrequency;
    private VariantEvaluation passesNoFrequencyData;

    private VariantEvaluation nullFrequencyVariant;

    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;

    private static final Frequency ESP_ALL_PASS = new Frequency(PASS_FREQ, FrequencySource.ESP_ALL);
    private static final Frequency ESP_ALL_FAIL = new Frequency(FAIL_FREQ);

    private static final Frequency ESP_AA_PASS = new Frequency(PASS_FREQ, FrequencySource.ESP_AFRICAN_AMERICAN);

    private static final Frequency ESP_EA_PASS = new Frequency(PASS_FREQ, FrequencySource.ESP_EUROPEAN_AMERICAN);

    private static final Frequency DBSNP_PASS = new Frequency(PASS_FREQ, FrequencySource.THOUSAND_GENOMES);

    private static final FrequencyData espAllPassData = new FrequencyData(null, ESP_ALL_PASS);
    private static final FrequencyData espAllFailData = new FrequencyData(null, ESP_ALL_FAIL);
    private static final FrequencyData espAaPassData = new FrequencyData(null, ESP_AA_PASS);
    private static final FrequencyData espEaPassData = new FrequencyData(null, ESP_EA_PASS);
    private static final FrequencyData dbSnpPassData = new FrequencyData(null, DBSNP_PASS);
    private static final FrequencyData noFreqData = new FrequencyData(null);

    @Before
    public void setUp() throws Exception {

        boolean filterOutAllKnownVariants = false;

        instance = new FrequencyFilter(FREQ_THRESHOLD, filterOutAllKnownVariants);

        passesEspAllFrequency = makeTestVariantEvaluation();
        passesEspAllFrequency.setFrequencyData(espAllPassData);

        passesEspAAFrequency = makeTestVariantEvaluation();
        passesEspAAFrequency.setFrequencyData(espAaPassData);

        passesEspEAFrequency = makeTestVariantEvaluation();
        passesEspEAFrequency.setFrequencyData(espEaPassData);

        passesDbsnpFrequency = makeTestVariantEvaluation();
        passesDbsnpFrequency.setFrequencyData(dbSnpPassData);

        failsFrequency = makeTestVariantEvaluation();
        failsFrequency.setFrequencyData(espAllFailData);

        passesNoFrequencyData = makeTestVariantEvaluation();
        passesNoFrequencyData.setFrequencyData(noFreqData);

        nullFrequencyVariant = makeTestVariantEvaluation();
    }
    
    private VariantEvaluation makeTestVariantEvaluation() {
        return new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
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
