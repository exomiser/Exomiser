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
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceFilterTest {

    private Gene compatibleWithAutosomalDominant;
    private Gene compatibleWithAutosomalRecessive;
    private Gene compatibleWithXLinked;

    @Before
    public void setUp() {

        compatibleWithAutosomalDominant = new Gene("mockGeneId", 12345);
        compatibleWithAutosomalDominant.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        compatibleWithAutosomalRecessive = new Gene("mockGeneId", 12345);
        compatibleWithAutosomalRecessive.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        compatibleWithXLinked = new Gene("mockGeneId", 12345);
        compatibleWithXLinked.setInheritanceModes(EnumSet.of(ModeOfInheritance.X_RECESSIVE));
    }

    @Test
    public void testGetModeOfInheritance() {
        ModeOfInheritance desiredInheritanceMode = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        InheritanceFilter instance = new InheritanceFilter(desiredInheritanceMode);
        assertThat(instance.getModeOfInheritance(), equalTo(desiredInheritanceMode));
    }
    
    @Test
    public void testGeneNotPassedOrFailedInheritanceFilterWhenInheritanceModeIsUnInitialised() {

        ModeOfInheritance desiredInheritanceMode = ModeOfInheritance.UNINITIALIZED;
        InheritanceFilter instance = new InheritanceFilter(desiredInheritanceMode);
        
        FilterResult filterResult = instance.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.wasRun(), is(false));
    }

    @Test
    public void testFilterGenePasses() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalDominant);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterGeneFails() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalRecessive);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    public void testGetFilterType() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.X_DOMINANT);

        assertThat(instance.getFilterType(), equalTo(FilterType.INHERITANCE_FILTER));
    }

    @Test
    public void testHashCode() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.X_DOMINANT);
        InheritanceFilter other = new InheritanceFilter(ModeOfInheritance.X_DOMINANT);

        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        InheritanceFilter otherDominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        assertThat(dominantFilter.equals(otherDominantFilter), is(true));

    }

    @Test
    public void testNotEquals() {
        InheritanceFilter recessiveFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        assertThat(recessiveFilter.equals(dominantFilter), is(false));

    }

    @Test
    public void testNotEqualOtherObject() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        String string = "string";
        assertThat(instance.equals(string), is(false));
        assertThat(string.equals(instance), is(false));

    }
    
    @Test
    public void testNotEqualNull() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        Object object = null;
        
        assertThat(instance.equals(object), is(false));
    }
    
    @Test
    public void testToString() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        assertThat(instance.toString(), equalTo("Inheritance filter: ModeOfInheritance=AUTOSOMAL_RECESSIVE"));
    }

}
