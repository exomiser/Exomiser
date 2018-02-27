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
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.EnumSet;
import java.util.Set;

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
    private Gene compatibleWithAutosomalDominantAndRecessive;
    private Gene compatibleWithXLinked;

    @Before
    public void setUp() {

        compatibleWithAutosomalDominant = new Gene("mockGeneId", 12345);
        compatibleWithAutosomalDominant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        compatibleWithAutosomalRecessive = new Gene("mockGeneId", 12345);
        compatibleWithAutosomalRecessive.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        compatibleWithAutosomalDominantAndRecessive = new Gene("mockGeneId", 12345);
        compatibleWithAutosomalDominantAndRecessive.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        compatibleWithXLinked = new Gene("mockGeneId", 12345);
        compatibleWithXLinked.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_RECESSIVE));
    }

    @Test
    public void testGetModeOfInheritance() {
        Set<ModeOfInheritance> desiredInheritanceMode = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        InheritanceFilter instance = new InheritanceFilter(desiredInheritanceMode);
        assertThat(instance.getCompatibleModes(), equalTo(desiredInheritanceMode));
    }
    
    @Test
    public void testGeneNotPassedOrFailedInheritanceFilterWhenInheritanceModeIsUnspecified() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.ANY));
        
        FilterResult filterResult = instance.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.wasRun(), is(false));
    }

    @Test
    public void testGeneNotPassedOrFailedInheritanceFilterWhenInheritanceModeIsEmpty() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.noneOf(ModeOfInheritance.class));

        FilterResult filterResult = instance.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.wasRun(), is(false));
    }

    @Test
    public void testFilterGenePasses() {
        InheritanceFilter dominantFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalDominant);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterGenePassesAllInheritanceModes() {
        InheritanceFilter inheritanceFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        FilterResult filterResult = inheritanceFilter.runFilter(compatibleWithAutosomalDominantAndRecessive);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterGenePassesOneOfTheInheritanceModes() {
        InheritanceFilter inheritanceFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        FilterResult filterResult = inheritanceFilter.runFilter(compatibleWithAutosomalDominant);

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterGeneFailsTheInheritanceModes() {
        InheritanceFilter recessiveFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        FilterResult filterResult = recessiveFilter.runFilter(compatibleWithAutosomalDominant);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    public void testFilterGeneFails() {
        InheritanceFilter dominantFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalRecessive);

        FilterTestHelper.assertFailed(filterResult);
    }

    @Test
    public void testGetFilterType() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.X_DOMINANT));

        assertThat(instance.getFilterType(), equalTo(FilterType.INHERITANCE_FILTER));
    }

    @Test
    public void testHashCode() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.X_DOMINANT));
        InheritanceFilter other = new InheritanceFilter(EnumSet.of(ModeOfInheritance.X_DOMINANT));

        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        InheritanceFilter otherDominantFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));


        assertThat(dominantFilter.equals(otherDominantFilter), is(true));

    }

    @Test
    public void testNotEquals() {
        InheritanceFilter recessiveFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        InheritanceFilter dominantFilter = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        assertThat(recessiveFilter.equals(dominantFilter), is(false));

    }

    @Test
    public void testNotEqualOtherObject() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        String string = "string";
        assertThat(instance.equals(string), is(false));
        assertThat(string.equals(instance), is(false));

    }
    
    @Test
    public void testNotEqualNull() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        assertThat(instance.equals(null), is(false));
    }
    
    @Test
    public void testToString() {
        InheritanceFilter instance = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        assertThat(instance.toString(), equalTo("InheritanceFilter{compatibleModes=[AUTOSOMAL_RECESSIVE]}"));
    }

}
