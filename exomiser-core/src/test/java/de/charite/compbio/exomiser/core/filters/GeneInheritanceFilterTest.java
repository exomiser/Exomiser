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
public class GeneInheritanceFilterTest {

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
        GeneInheritanceFilter instance = new GeneInheritanceFilter(desiredInheritanceMode);
        
        FilterResult filterResult = instance.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.NOT_RUN));
    }

    @Test
    public void testFilterGenePasses() {
        GeneInheritanceFilter dominantFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalDominant);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testFilterGeneFails() {
        GeneInheritanceFilter dominantFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        FilterResult filterResult = dominantFilter.runFilter(compatibleWithAutosomalRecessive);

        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testGetFilterType() {
        GeneInheritanceFilter instance = new GeneInheritanceFilter(ModeOfInheritance.X_DOMINANT);

        assertThat(instance.getFilterType(), equalTo(FilterType.INHERITANCE_FILTER));
    }

    @Test
    public void testHashCode() {
        GeneInheritanceFilter instance = new GeneInheritanceFilter(ModeOfInheritance.X_DOMINANT);
        GeneInheritanceFilter other = new GeneInheritanceFilter(ModeOfInheritance.X_DOMINANT);

        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GeneInheritanceFilter dominantFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        GeneInheritanceFilter otherDominantFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        assertThat(dominantFilter.equals(otherDominantFilter), is(true));

    }

    @Test
    public void testNotEquals() {
        GeneInheritanceFilter recessiveFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        GeneInheritanceFilter dominantFilter = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        assertThat(recessiveFilter.equals(dominantFilter), is(false));

    }

    @Test
    public void testNotEqualOtherObject() {
        GeneInheritanceFilter instance = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        String string = "string";
        assertThat(instance.equals(string), is(false));
        assertThat(string.equals(instance), is(false));

    }
    
    @Test
    public void testNotEqualNull() {
        GeneInheritanceFilter instance = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        Object object = null;
        
        assertThat(instance.equals(object), is(false));
    }
    
    @Test
    public void testToString() {
        GeneInheritanceFilter instance = new GeneInheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        assertThat(instance.toString(), equalTo("Inheritance filter: ModeOfInheritance=AUTOSOMAL_RECESSIVE"));
    }

}
