package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.SampleMismatchException;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleNameCheckerTest {

    @Test
    public void testGetProbandSampleIdWithEmptyProbandNameAndEmptySampleNames() {
        int sampleId = SampleNameChecker.getProbandSampleId("", Collections.emptyList());
        assertThat(sampleId, equalTo(0));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMatch() {
        int sampleId = SampleNameChecker.getProbandSampleId("David", Collections.singletonList("David"));
        assertThat(sampleId, equalTo(0));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndEmptySampleNames() {
        SampleNameChecker.getProbandSampleId("David", Collections.emptyList());
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMisMatch() {
        SampleNameChecker.getProbandSampleId("David", Collections.singletonList("Slartibartfast"));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMisMatch() {
        SampleNameChecker.getProbandSampleId("David", Arrays.asList("Slartibartfast", "Homer"));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithEmptyProbandNameAndSampleNamesMisMatch() {
        SampleNameChecker.getProbandSampleId("", Arrays.asList("Slartibartfast", "Homer"));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchFirstInList() {
        int sampleId = SampleNameChecker.getProbandSampleId("David", Arrays.asList("David", "Slartibartfast"));
        assertThat(sampleId, equalTo(0));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchNotFirstInList() {
        int sampleId = SampleNameChecker.getProbandSampleId("David", Arrays.asList("Slartibartfast", "David"));
        assertThat(sampleId, equalTo(1));
    }

    @Test
    public void testGetProbandSampleNameWithEmptyProbandNameAndNoSampleNameInList() {
        String sampleName = SampleNameChecker.getProbandSampleName("", Collections.emptyList());
        assertThat(sampleName, equalTo(""));
    }

    @Test
    public void testGetProbandSampleNameWithEmptyProbandNameAndOneSampleNameInList() {
        String sampleName = SampleNameChecker.getProbandSampleName("", Collections.singletonList("David"));
        assertThat(sampleName, equalTo("David"));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleNameWithEmptyProbandNameAndMultipleSampleNamesInList() {
        SampleNameChecker.getProbandSampleName("", Arrays.asList("Slartibartfast", "David"));
    }

}