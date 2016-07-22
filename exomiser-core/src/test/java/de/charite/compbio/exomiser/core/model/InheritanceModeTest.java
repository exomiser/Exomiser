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

package de.charite.compbio.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jj8
 */
public class InheritanceModeTest {

    /**
     * Test of valueOf method, of class InheritanceMode.
     */
    @Test
    public void testValueOf() {
        assertThat(InheritanceMode.valueOf("X_RECESSIVE"), equalTo(InheritanceMode.X_RECESSIVE));
    }

    /**
     * Test of getTerm method, of class InheritanceMode.
     */
    @Test
    public void testGetTerm() {
        assertThat(InheritanceMode.RECESSIVE.getTerm(), equalTo("autosomal recessive"));
    }

    /**
     * Test of getInheritanceCode method, of class InheritanceMode.
     */
    @Test
    public void testGetInheritanceCode() {
        assertThat(InheritanceMode.MITOCHONDRIAL.getInheritanceCode(), equalTo("M"));
    }

    /**
     * Test of valueOfInheritanceCode method, of class InheritanceMode.
     */
    @Test
    public void testValueOfInheritanceCode() {
        assertThat(InheritanceMode.valueOfInheritanceCode("D"), equalTo(InheritanceMode.DOMINANT));
        assertThat(InheritanceMode.valueOfInheritanceCode("R"), equalTo(InheritanceMode.RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("B"), equalTo(InheritanceMode.DOMINANT_AND_RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("X"), equalTo(InheritanceMode.X_LINKED));
        assertThat(InheritanceMode.valueOfInheritanceCode("XD"), equalTo(InheritanceMode.X_DOMINANT));
        assertThat(InheritanceMode.valueOfInheritanceCode("XR"), equalTo(InheritanceMode.X_RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("Y"), equalTo(InheritanceMode.Y_LINKED));
        assertThat(InheritanceMode.valueOfInheritanceCode("M"), equalTo(InheritanceMode.MITOCHONDRIAL));
        assertThat(InheritanceMode.valueOfInheritanceCode("S"), equalTo(InheritanceMode.SOMATIC));
        assertThat(InheritanceMode.valueOfInheritanceCode("P"), equalTo(InheritanceMode.POLYGENIC));
        assertThat(InheritanceMode.valueOfInheritanceCode("U"), equalTo(InheritanceMode.UNKNOWN));
    }

    /**
     * Test of valueOfHpoTerm method, of class InheritanceMode.
     */
    @Test
    public void testValueOfHpoTerm() {
        assertThat(InheritanceMode.valueOfHpoTerm("kjghdgh"), equalTo(InheritanceMode.UNKNOWN));
    }

    @Test
    public void testToString() {
        System.out.println(InheritanceMode.X_DOMINANT);
    }
}
