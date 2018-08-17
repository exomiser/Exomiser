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

package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.SampleMismatchException;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleIdentifierUtilTest {

    @Test
    public void testGetProbandSampleIdWithEmptyProbandNameAndEmptySampleNames() {
        SampleIdentifier sampleId = SampleIdentifierUtil.createProbandIdentifier("", Collections.emptyList());
        assertThat(sampleId, equalTo(SampleIdentifier.defaultSample()));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMatch() {
        SampleIdentifier sampleId = SampleIdentifierUtil.createProbandIdentifier("David", Collections.singletonList("David"));
        assertThat(sampleId, equalTo(SampleIdentifier.of("David", 0)));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndEmptySampleNames() {
        SampleIdentifierUtil.createProbandIdentifier("David", Collections.emptyList());
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMisMatch() {
        SampleIdentifierUtil.createProbandIdentifier("David", Collections.singletonList("Slartibartfast"));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMisMatch() {
        SampleIdentifierUtil.createProbandIdentifier("David", Arrays.asList("Slartibartfast", "Homer"));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleIdWithEmptyProbandNameAndSampleNamesMisMatch() {
        SampleIdentifierUtil.createProbandIdentifier("", Arrays.asList("Slartibartfast", "Homer"));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchFirstInList() {
        SampleIdentifier sampleId = SampleIdentifierUtil.createProbandIdentifier("David", Arrays.asList("David", "Slartibartfast"));
        assertThat(sampleId, equalTo(SampleIdentifier.of("David", 0)));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchNotFirstInList() {
        SampleIdentifier sampleId = SampleIdentifierUtil.createProbandIdentifier("David", Arrays.asList("Slartibartfast", "David"));
        assertThat(sampleId, equalTo(SampleIdentifier.of("David", 1)));
    }

    @Test
    public void testGetProbandSampleNameWithEmptyProbandNameAndOneSampleNameInList() {
        SampleIdentifier sampleId = SampleIdentifierUtil.createProbandIdentifier("", Collections.singletonList("David"));
        assertThat(sampleId, equalTo(SampleIdentifier.of("David", 0)));
    }

    @Test(expected = SampleMismatchException.class)
    public void testGetProbandSampleNameWithEmptyProbandNameAndMultipleSampleNamesInList() {
        SampleIdentifierUtil.createProbandIdentifier("", Arrays.asList("Slartibartfast", "David"));
    }

}