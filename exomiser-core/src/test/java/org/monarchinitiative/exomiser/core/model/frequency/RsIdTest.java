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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model.frequency;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class RsIdTest {

    private static final int ID = 234567364;
    private static final RsId instance = RsId.of(ID);

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(ID));
    }

    @Test
    public void testHashCode() {
        RsId other = RsId.of(ID);
        int expected = other.hashCode();
        assertThat(instance.hashCode(), equalTo(expected));
    }

    @Test
    public void testEqualsNotNull() {
        assertThat(instance.equals(null), is(false));
    }
    
    @Test
    public void testEqualsNotSomethingElse() {
        assertThat(instance.equals("1335464574"), is(false));
    }

    @Test
    public void testNotEqualsDifferentId() {
        assertThat(instance.equals(RsId.of(ID + 1)), is(false));
    }

    @Test
    public void testEquals() {
        assertThat(instance.equals(RsId.of(ID)), is(true));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("rs234567364"));
    }

    @Test
    public void testValueOfNullReturnsEmpty() {
        assertThat(RsId.of(null), equalTo(RsId.empty()));
    }

    @Test
    public void testValueOfLessThanZeroReturnsEmpty() {
        assertThat(RsId.of(Integer.MIN_VALUE), equalTo(RsId.empty()));
    }

    @Test
    public void testValueOfZeroReturnsEmpty() {
        assertThat(RsId.of(0), equalTo(RsId.empty()));
    }

    @Test
    public void testValueOfEmpty() {
        assertThat(RsId.of("."), equalTo(RsId.empty()));
    }

    @Test
    public void testEmptyInputReturnsEmpty() {
        assertThat(RsId.of(""), equalTo(RsId.empty()));
    }

    @Test
    public void testIsEmpty() {
        assertThat(RsId.empty().isEmpty(), is(true));
    }

    @Test
    public void testNotEmpty() {
        assertThat(RsId.of(123456).isEmpty(), is(false));
    }

    @Test
    public void testEmptyToString() {
        assertThat(RsId.empty().toString(), equalTo("."));
    }

    @Test
    public void testParseValidRsId() {
        assertThat(RsId.of("rs123456"), equalTo(RsId.of(123456)));
    }

    @Test
    public void testParseRsIdFromStringInt() {
        assertThat(RsId.of("123456"), equalTo(RsId.of(123456)));
    }

    @Test
    public void testValueOfInvalidThrowsException() {
        assertThrows(NumberFormatException.class, () -> RsId.of("wibble"));
    }

}
