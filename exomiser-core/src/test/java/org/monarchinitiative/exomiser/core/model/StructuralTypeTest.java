/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class StructuralTypeTest {

    @Test
    void testParseUnknown() {
        assertThat(StructuralType.parseValue("WIBBLE"), equalTo(StructuralType.UNKNOWN));
    }

    @Test
    void testParseAngleBracketValue() {
        assertThat(StructuralType.parseValue("<DEL>"), equalTo(StructuralType.DEL));
        assertThat(StructuralType.parseValue("<INS:ME:HERV>"), equalTo(StructuralType.INS_ME_HERV));
        assertThat(StructuralType.parseValue("<INS:ME:ALU>"), equalTo(StructuralType.INS_ME_ALU));
    }

    @Test
    void testParseStrippedValue() {
        assertThat(StructuralType.parseValue("DEL"), equalTo(StructuralType.DEL));
    }

    @Test
    void testGetBaseMethodFromBaseType() {
        assertThat(StructuralType.DEL.getBaseType(), equalTo(StructuralType.DEL));
    }

    @Test
    void testGetBaseTypeFromSubType() {
        assertThat(StructuralType.DEL_ME_ALU.getBaseType(), equalTo(StructuralType.DEL));
        assertThat(StructuralType.INS_ME.getBaseType(), equalTo(StructuralType.INS));
        assertThat(StructuralType.BND.getBaseType(), equalTo(StructuralType.BND));
    }

    @Test
    void testGetSubType() {
        assertThat(StructuralType.DEL.getSubType(), equalTo(StructuralType.DEL));
        assertThat(StructuralType.DEL_ME.getSubType(), equalTo(StructuralType.DEL_ME));
        assertThat(StructuralType.DEL_ME_ALU.getSubType(), equalTo(StructuralType.DEL_ME));
        assertThat(StructuralType.INS_ME.getSubType(), equalTo(StructuralType.INS_ME));
        assertThat(StructuralType.INS_ME_HERV.getSubType(), equalTo(StructuralType.INS_ME));
        assertThat(StructuralType.BND.getSubType(), equalTo(StructuralType.BND));
    }

    @Test
    void testNonCanonicalDelMobileElementSubType() {
        assertThat(StructuralType.parseValue("DEL:ME:SINE"), equalTo(StructuralType.DEL_ME));
    }

    @Test
    void testNonCanonicalStrType() {
        assertThat(StructuralType.parseValue("STR"), equalTo(StructuralType.STR));
    }

    @Test
    void testStrWithNumRepeats() {
        assertThat(StructuralType.parseValue("<STR27>"), equalTo(StructuralType.STR));
        assertThat(StructuralType.parseValue("STR27"), equalTo(StructuralType.STR));
    }

    @Test
    void testIsStructural() {
        assertThat(StructuralType.UNKNOWN.isStructural(), is(true));
        assertThat(StructuralType.NON_STRUCTURAL.isStructural(), is(false));
    }

    @Test
    void testBaseTypeForCanvasTypes() {
        assertThat(StructuralType.CNV_GAIN.getBaseType(), equalTo(StructuralType.CNV));
        assertThat(StructuralType.CNV_LOSS.getBaseType(), equalTo(StructuralType.CNV));
        assertThat(StructuralType.CNV_LOH.getBaseType(), equalTo(StructuralType.CNV));
        assertThat(StructuralType.CNV_COMPLEX.getBaseType(), equalTo(StructuralType.CNV));
    }
}