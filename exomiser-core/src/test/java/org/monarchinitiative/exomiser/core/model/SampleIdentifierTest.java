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

package org.monarchinitiative.exomiser.core.model;


import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleIdentifierTest {

    @Test
    public void throwsExceptionWhenInitialisedWithNull() {
        assertThrows(NullPointerException.class, () -> SampleIdentifier.of(null, 0));
    }

    @Test
    public void throwsExceptionWhenInitialisedWithEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> SampleIdentifier.of("", 0));
    }

    @Test
    public void defaultInstance() {
        assertThat(SampleIdentifier.of("sample", 0), equalTo(SampleIdentifier.defaultSample()));
    }

    @Test
    public void standardInitialisation() {
        SampleIdentifier instance = SampleIdentifier.of("Slartibartfast", 1);
        assertThat(instance.getId(), equalTo("Slartibartfast"));
        assertThat(instance.getGenotypePosition(), equalTo(1));
    }

    @Test
    public void testToString() {
        System.out.println(SampleIdentifier.of("SMPL12345", 3));
    }
}