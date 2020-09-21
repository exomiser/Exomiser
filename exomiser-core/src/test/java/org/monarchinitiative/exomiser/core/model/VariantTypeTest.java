/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantTypeTest {

    @Test
    void nullInput() {
        assertThrows(NullPointerException.class, () -> VariantType.parseValue(null));
    }

    @Test
    void emptyInput() {
        assertThat(VariantType.parseValue(""), equalTo(VariantType.UNKNOWN));
    }

    @Test
    void testParseUnknown() {
        assertThat(VariantType.parseValue("WIBBLE"), equalTo(VariantType.UNKNOWN));
    }

    @Test
    void testParseAngleBracketValue() {
        assertThat(VariantType.parseValue("<DEL>"), equalTo(VariantType.DEL));
        assertThat(VariantType.parseValue("<INS:ME:HERV>"), equalTo(VariantType.INS_ME_HERV));
        assertThat(VariantType.parseValue("<INS:ME:ALU>"), equalTo(VariantType.INS_ME_ALU));
    }

    @Test
    void testParseStrippedValue() {
        assertThat(VariantType.parseValue("DEL"), equalTo(VariantType.DEL));
    }

    @Test
    void testGetBaseMethodFromBaseType() {
        assertThat(VariantType.DEL.getBaseType(), equalTo(VariantType.DEL));
    }

    @Test
    void testGetBaseTypeFromSubType() {
        assertThat(VariantType.DEL_ME_ALU.getBaseType(), equalTo(VariantType.DEL));
        assertThat(VariantType.INS_ME.getBaseType(), equalTo(VariantType.INS));
        assertThat(VariantType.BND.getBaseType(), equalTo(VariantType.BND));
    }

    @Test
    void testGetSubType() {
        assertThat(VariantType.DEL.getSubType(), equalTo(VariantType.DEL));
        assertThat(VariantType.DEL_ME.getSubType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.DEL_ME_ALU.getSubType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.INS_ME.getSubType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.INS_ME_HERV.getSubType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.BND.getSubType(), equalTo(VariantType.BND));
    }

    @Test
    void testNonCanonicalDelMobileElementSubType() {
        assertThat(VariantType.parseValue("DEL:ME:SINE"), equalTo(VariantType.DEL_ME));
    }

    @Test
    void testNonCanonicalStrType() {
        assertThat(VariantType.parseValue("STR"), equalTo(VariantType.STR));
    }

    @Test
    void testStrWithNumRepeats() {
        assertThat(VariantType.parseValue("<STR27>"), equalTo(VariantType.STR));
        assertThat(VariantType.parseValue("STR27"), equalTo(VariantType.STR));
    }

    @Test
    void testIsStructural() {
        assertThat(VariantType.UNKNOWN.isStructural(), is(true));
        assertThat(VariantType.DEL.isStructural(), is(true));

        assertThat(VariantType.SNV.isStructural(), is(false));
        assertThat(VariantType.MNV.isStructural(), is(false));
        assertThat(VariantType.INDEL.isStructural(), is(false));
    }

    @Test
    void testBaseTypeForCanvasTypes() {
        assertThat(VariantType.CNV_GAIN.getBaseType(), equalTo(VariantType.CNV));
        assertThat(VariantType.CNV_LOSS.getBaseType(), equalTo(VariantType.CNV));
        assertThat(VariantType.CNV_LOH.getBaseType(), equalTo(VariantType.CNV));
        assertThat(VariantType.CNV_COMPLEX.getBaseType(), equalTo(VariantType.CNV));
    }
}