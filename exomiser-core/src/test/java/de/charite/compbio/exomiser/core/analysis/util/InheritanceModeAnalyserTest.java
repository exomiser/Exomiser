/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;

import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyserTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testAnalyseInheritanceModesSingleSampleNoVariants() {
        Gene gene = new Gene("ABC", 123);
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(pedigree, ModeOfInheritance.UNINITIALIZED);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.UNINITIALIZED), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModesSingleSampleNoPassedVariants() {
        Gene gene = new Gene("ABC", 123);
        gene.addVariant(filteredVariant(1, 1 , "A", "T", new FailFilterResult(FilterType.FREQUENCY_FILTER)));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(pedigree, ModeOfInheritance.UNINITIALIZED);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.UNINITIALIZED), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    private VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult) {
        VariantEvaluation variant  = new VariantEvaluation.VariantBuilder(chr, pos, ref, alt).build();
        variant.addFilterResult(filterResult);
        return variant;
    }

}
