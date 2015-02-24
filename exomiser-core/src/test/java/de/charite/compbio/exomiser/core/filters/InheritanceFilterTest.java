/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.EnumSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void testGeneNotPassedOrFailedInheritanceFilterWhenInheritanceModeIsUnInitialised() {

        ModeOfInheritance desiredInheritanceMode = ModeOfInheritance.UNINITIALIZED;
        InheritanceFilter instance = new InheritanceFilter(desiredInheritanceMode);
        
        FilterResult filterResult = instance.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.NOT_RUN));
    }

    @Test
    public void testFilterGenePasses() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalDominant);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testFilterGeneFails() {
        InheritanceFilter dominantFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
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
