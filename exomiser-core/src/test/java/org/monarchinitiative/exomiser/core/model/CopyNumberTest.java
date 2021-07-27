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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class CopyNumberTest {

    @Test
    public void testEmpty() {
        assertSame(CopyNumber.empty(), CopyNumber.of(-1));
    }

    @Test
    public void testCopies() {
        assertThat(CopyNumber.empty().copies(), equalTo(-1));
        assertThat(CopyNumber.of(2).copies(), equalTo(2));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(CopyNumber.empty().isEmpty());
        assertFalse(CopyNumber.of(1).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, false",
            "1, false",
            "2, false",
            "3, true",
            "4, true",
    })
    public void testIsCopyGain(int copies, boolean expected) {
        assertThat(CopyNumber.of(copies).isCopyGain(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, true",
            "1, true",
            "2, false",
            "3, false",
            "4, false",
    })
    public void testIsCopyLoss(int copies, boolean expected) {
        assertThat(CopyNumber.of(copies).isCopyLoss(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, false",
            "1, false",
            "2, true",
            "3, false",
            "4, false",
    })
    public void isCopyNeutral(int copies, boolean expected) {
        assertThat(CopyNumber.of(copies).isCopyNeutral(), equalTo(expected));
    }
}