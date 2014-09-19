/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceFilterTest {

    private Gene compatibleWithAutosomalDominant;
    private Gene compatibleWithAutosomalRecessive;
    private Gene compatibleWithXLinked;

    @Mock
    private Variant variant;

    public InheritanceFilterTest() {
    }

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        Mockito.when(variant.getGeneSymbol()).thenReturn("mockGeneId");
        Mockito.when(variant.getEntrezGeneID()).thenReturn(12345);

        compatibleWithAutosomalDominant = new Gene(new VariantEvaluation(variant));
        compatibleWithAutosomalDominant.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        compatibleWithAutosomalRecessive = new Gene(new VariantEvaluation(variant));
        compatibleWithAutosomalRecessive.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        compatibleWithXLinked = new Gene(new VariantEvaluation(variant));
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
    public void testToString() {
        InheritanceFilter instance = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        assertThat(instance.toString(), equalTo("Inheritance filter: ModeOfInheritance=AUTOSOMAL_RECESSIVE"));
    }

}
