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

package org.monarchinitiative.exomiser.core.model;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleIdentifiersTest {

    @Test
    public void testGetProbandSampleIdWithEmptyProbandNameAndEmptySampleNames() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("", List.of());
        assertThat(sampleId, equalTo(SampleIdentifiers.defaultSample()));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMatch() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("David", List.of("David"));
        assertThat(sampleId, equalTo("David"));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndEmptySampleNames() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("David", List.of());
        assertThat(sampleId, equalTo("David"));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNameMisMatch() {
        List<String> sampleNames = List.of("Slartibartfast");
        assertThrows(IllegalStateException.class, () ->
                SampleIdentifiers.checkProbandIdentifier("David", sampleNames)
        );
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMisMatch() {
        List<String> sampleNames = List.of("Slartibartfast", "Homer");
        assertThrows(IllegalStateException.class, () ->
                SampleIdentifiers.checkProbandIdentifier("David", sampleNames)
        );
    }

    @Test
    public void testGetProbandSampleIdWithEmptyProbandNameAndSampleNamesMisMatch() {
        List<String> sampleNames = List.of("Slartibartfast", "Homer");
        assertThrows(IllegalStateException.class, () ->
                SampleIdentifiers.checkProbandIdentifier("", sampleNames)
        );
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchFirstInList() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("David", List.of("David", "Slartibartfast"));
        assertThat(sampleId, equalTo("David"));
    }

    @Test
    public void testGetProbandSampleIdWithProbandNameAndSampleNamesMatchNotFirstInList() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("David", List.of("Slartibartfast", "David"));
        assertThat(sampleId, equalTo("David"));
    }

    @Test
    public void testGetProbandSampleNameWithEmptyProbandNameAndOneSampleNameInList() {
        String sampleId = SampleIdentifiers.checkProbandIdentifier("", List.of("David"));
        assertThat(sampleId, equalTo("David"));
    }

    @Test
    public void testGetProbandSampleNameWithEmptyProbandNameAndMultipleSampleNamesInList() {
        List<String> sampleNames = List.of("Slartibartfast", "David");
        assertThrows(IllegalStateException.class, () ->
                SampleIdentifiers.checkProbandIdentifier("", sampleNames)
        );
    }

    @Test
    public void testCreateProbandIdentifierThrowsExceptionWithDuplicatedNames() {
        List<String> sampleNames = List.of("Slartibartfast", "David", "Slartibartfast");
        assertThrows(IllegalStateException.class, () ->
                SampleIdentifiers.checkProbandIdentifier("David", sampleNames)
        );
    }
}