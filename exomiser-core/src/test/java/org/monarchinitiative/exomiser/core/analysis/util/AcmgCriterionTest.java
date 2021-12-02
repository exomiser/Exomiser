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

package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.analysis.util.AcmgCriterion.Evidence;

class AcmgCriterionTest {

    @Test
    void testEvidenceDisplayString() {
        assertThat(Evidence.STAND_ALONE.displayString(), equalTo("StandAlone"));
        assertThat(Evidence.VERY_STRONG.displayString(), equalTo("VeryStrong"));
        assertThat(Evidence.STRONG.displayString(), equalTo("Strong"));
        assertThat(Evidence.MODERATE.displayString(), equalTo("Moderate"));
        assertThat(Evidence.SUPPORTING.displayString(), equalTo("Supporting"));
    }

    @Test
    void testEvidenceParseValue() {
        assertThat(Evidence.parseValue("STAND_ALONE"), equalTo(Evidence.STAND_ALONE));
        assertThat(Evidence.parseValue("StandAlone"), equalTo(Evidence.STAND_ALONE));
        assertThat(Evidence.parseValue("VeryStrong"), equalTo(Evidence.VERY_STRONG));
        assertThat(Evidence.parseValue("Strong"), equalTo(Evidence.STRONG));
        assertThat(Evidence.parseValue("Moderate"), equalTo(Evidence.MODERATE));
        assertThat(Evidence.parseValue("Supporting"), equalTo(Evidence.SUPPORTING));
    }

    @Test
    void testEvidenceParseValueNotRecognisedThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Evidence.parseValue("Invalid input"));
    }

}