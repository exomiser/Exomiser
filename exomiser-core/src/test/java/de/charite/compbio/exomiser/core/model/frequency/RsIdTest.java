/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.frequency;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RsIdTest {

    private static final int ID = 234567364;
    private static final RsId instance = RsId.valueOf(ID);

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(ID));
    }

    @Test
    public void testHashCode() {
        RsId other = RsId.valueOf(ID);
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
        assertThat(instance.equals(RsId.valueOf(ID + 1)), is(false));
    }

    @Test
    public void testEquals() {
        assertThat(instance.equals(RsId.valueOf(ID)), is(true));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("rs" + ID));
    }
    
}
