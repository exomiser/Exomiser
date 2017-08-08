/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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