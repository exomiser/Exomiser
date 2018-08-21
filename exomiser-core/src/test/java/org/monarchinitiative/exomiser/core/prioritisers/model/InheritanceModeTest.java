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

package org.monarchinitiative.exomiser.core.prioritisers.model;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
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
        assertThat(InheritanceMode.AUTOSOMAL_RECESSIVE.getTerm(), equalTo("autosomal recessive"));
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
        assertThat(InheritanceMode.valueOfInheritanceCode("D"), equalTo(InheritanceMode.AUTOSOMAL_DOMINANT));
        assertThat(InheritanceMode.valueOfInheritanceCode("R"), equalTo(InheritanceMode.AUTOSOMAL_RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("B"), equalTo(InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("X"), equalTo(InheritanceMode.X_LINKED));
        assertThat(InheritanceMode.valueOfInheritanceCode("XD"), equalTo(InheritanceMode.X_DOMINANT));
        assertThat(InheritanceMode.valueOfInheritanceCode("XR"), equalTo(InheritanceMode.X_RECESSIVE));
        assertThat(InheritanceMode.valueOfInheritanceCode("Y"), equalTo(InheritanceMode.Y_LINKED));
        assertThat(InheritanceMode.valueOfInheritanceCode("M"), equalTo(InheritanceMode.MITOCHONDRIAL));
        assertThat(InheritanceMode.valueOfInheritanceCode("S"), equalTo(InheritanceMode.SOMATIC));
        assertThat(InheritanceMode.valueOfInheritanceCode("P"), equalTo(InheritanceMode.POLYGENIC));
        assertThat(InheritanceMode.valueOfInheritanceCode("U"), equalTo(InheritanceMode.UNKNOWN));
        assertThat(InheritanceMode.valueOfInheritanceCode("unrecognised code"), equalTo(InheritanceMode.UNKNOWN));
    }

    @Test
    public void isCompatibleWithDominant() {
        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT.isCompatibleWithDominant(), is(true));
        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE.isCompatibleWithDominant(), is(true));

        assertThat(InheritanceMode.AUTOSOMAL_RECESSIVE.isCompatibleWithDominant(), is(false));
    }

    @Test
    public void isCompatibleWithRecessive() {
        assertThat(InheritanceMode.AUTOSOMAL_RECESSIVE.isCompatibleWithRecessive(), is(true));
        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE.isCompatibleWithRecessive(), is(true));

        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT.isCompatibleWithRecessive(), is(false));
    }

    @Test
    public void isXlinked() {
        assertThat(InheritanceMode.X_DOMINANT.isXlinked(), is(true));
        assertThat(InheritanceMode.X_LINKED.isXlinked(), is(true));
        assertThat(InheritanceMode.X_RECESSIVE.isXlinked(), is(true));

        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT.isXlinked(), is(false));
    }

    @Test
    public void testToModeOfInheritance() {
        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT)));
        assertThat(InheritanceMode.AUTOSOMAL_RECESSIVE.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
        assertThat(InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.AUTOSOMAL_DOMINANT)));
        assertThat(InheritanceMode.X_DOMINANT.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.X_DOMINANT)));
        assertThat(InheritanceMode.X_RECESSIVE.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.X_RECESSIVE)));
        assertThat(InheritanceMode.MITOCHONDRIAL.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.MITOCHONDRIAL)));
        assertThat(InheritanceMode.X_LINKED.toModeOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT)));
        assertThat(InheritanceMode.POLYGENIC.toModeOfInheritance(), equalTo(EnumSet.noneOf(ModeOfInheritance.class)));
    }

    @Test
    public void testToString() {
        System.out.println(InheritanceMode.X_DOMINANT);
    }
}
