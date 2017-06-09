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
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.PedPerson;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Sex;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyserTest {

    @Test
    public void testAnalyseInheritanceModes_SingleSample_NoVariants() {
        Gene gene = newGene();
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.ANY, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_NoPassedVariants() {
        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 1 , "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER)));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.ANY, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HET() {
        List<Allele> alleles = buildAlleles("A", "T");
        // build Genotype
        Genotype genotype = buildSampleGenotype("Adam", alleles.get(0), alleles.get(1));
        assertThat(genotype.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");


        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_REF_ShouldBeIncompatibleWith_RECESIVE() {
        List<Allele> alleles = buildAlleles("A", "T");

        //HomRef 0/0 or 0|0 variants really shouldn't be causing rare diseases so we need to ensure these are removed
        Genotype genotype = buildSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_REF_ShouldBeIncompatibleWith_DOMINANT() {
        List<Allele> alleles = buildAlleles("A", "T");

        //HomRef 0/0 or 0|0 variants really shouldn't be causing rare diseases so we need to ensure these are removed
        Genotype genotype = buildSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes().isEmpty(), is(true)));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_VAR() {
        List<Allele> alleles = buildAlleles("A", "T");
        //HOM_ALT
        Genotype genotype = buildSampleGenotype("Adam", alleles.get(1), alleles.get(1));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes().isEmpty(), is(true)));
    }

    @Test
    public void testAnalyseInheritanceModes_MultiSample_OnePassedVariant_HOM_VAR_shouldBeCompatibelWith_RECESSIVE() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
    }

    @Test
    public void testAnalyseInheritanceModes_MultiSample_OnePassedVariant_HOM_REF_shouldNotBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildSampleGenotype("Cain", alleles.get(0), alleles.get(0));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_REF));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes().isEmpty(), is(true)));
    }

    @Test
    public void testAnalyseInheritanceModes_MultiSample_OnePassedVariant_HET_shouldBeCompatibleWith_AD() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(0));
        assertThat(mother.getType(), equalTo(GenotypeType.HOM_REF));

        Genotype father = buildSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT)));
    }

    @Test
    public void testAnalyseInheritanceModes_MultiSample_MultiAllelic_TwoPassedVariant_HOM_VAR_shouldBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
                });
    }

    private String variantString(VariantEvaluation variant) {
        return String.format("%s\t%s\t%s\t%s\t%s\tcompatibleWith=%s",variant.getChromosome(), variant.getRef(), variant.getAlt(), variant.getAltAlleleId(), variant.getGenotypeString(), variant.getInheritanceModes());
    }

    /**
     * Currently ignored as Jannovar multi-allelic inheritance compatibility is broken for multi-sample VCF.
     */
    @Ignore
    @Test
    public void testAnalyseInheritanceModes_MultiSample_MultiAllelic_OnePassedVariant_HOM_VAR_altAllele2_shouldBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(1), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype father = buildSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
                });
    }

    /**
     * Currently ignored as Jannovar multi-allelic inheritance compatibility is broken for multi-sample VCF.
     */
    @Ignore
    @Test
    public void testAnalyseInheritanceModes_MultiSample_MultiAllelic_OnePassedVariant_HET_shouldBeCompatibleWith_AD() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype brother = buildSampleGenotype("Abel", alleles.get(1), alleles.get(1));
        assertThat(brother.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
                });
    }

    private Gene newGene() {
        return new Gene("ABC", 123);
    }


    @Test
    public void testAnalyseInheritanceModes_SingleSample_MultiAllelic_OnePassedVariant_HET_shouldBeCompatibleWith_AD() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
                });
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_MultiAllelic_TwoPassedVariant_HET_shouldBeCompatibleWith_AD() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
                });
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_MultiAllelic_TwoPassedVariant_HOM_VAR_Allele2_shouldBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
                });
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_MultiAllelic_TwoPassedVariant_HOM_VAR_Allele1_shouldBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
                });
    }

    @Test
    public void testFindCompHetCompatibleAllelesTwoAffectedSibsUnaffectedMotherhMissingFater() {
        Gene gene = newGene();

        //1 98518687 . T A 733 . GT 1|0 1|0 1|0
        List<Allele> alleles = buildAlleles("T", "A");

        Genotype proband = buildSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype brother = buildSampleGenotype("Abel", alleles.get(0), alleles.get(1));
        assertThat(brother.getType(), equalTo(GenotypeType.HET));

        Genotype mother = buildSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        VariantContext vc98518687 = buildVariantContext(1, 98518687, alleles, proband, brother, mother);
        VariantEvaluation var98518687 = filteredVariant(1, 98518687, "T", "A", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc98518687);
        System.out.println("Built allele " + var98518687);
        gene.addVariant(var98518687);

        //1 98518683 . T A 733 . GT 1|0 1|0 1|0
        List<Allele> alleles98518683 = buildAlleles("T", "A");

        Genotype proband98518683 = buildSampleGenotype("Cain", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(proband98518683.getType(), equalTo(GenotypeType.HET));

        Genotype brother98518683 = buildSampleGenotype("Abel", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(brother98518683.getType(), equalTo(GenotypeType.HET));

        Genotype mother98518683 = buildSampleGenotype("Eve", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(mother98518683.getType(), equalTo(GenotypeType.HET));

        VariantContext vc98518683 = buildVariantContext(1, 98518683, alleles98518683, proband98518683, brother98518683, mother98518683);
        VariantEvaluation var98518683 = filteredVariant(1, 98518683, "T", "A", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc98518683);
        System.out.println("Built allele " + var98518683);
        gene.addVariant(var98518683);

        //1 97723020 . A G 1141 . GT 1/0 0/0 1/0
        List<Allele> alleles97723020 = buildAlleles("A", "G");

        Genotype proband97723020 = buildSampleGenotype("Cain", alleles97723020.get(0), alleles97723020.get(1));
        assertThat(proband97723020.getType(), equalTo(GenotypeType.HET));

        Genotype brother97723020 = buildSampleGenotype("Abel", alleles97723020.get(0), alleles97723020.get(1));
        assertThat(brother97723020.getType(), equalTo(GenotypeType.HET));

        Genotype mother97723020 = buildSampleGenotype("Eve", alleles97723020.get(0), alleles97723020.get(0));
        assertThat(mother97723020.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext vc97723020 = buildVariantContext(1, 97723020, alleles97723020, proband97723020, brother97723020, mother97723020);
        VariantEvaluation var97723020 = filteredVariant(1, 97723020, "A", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc97723020);
        System.out.println("Built allele " + var97723020);
        gene.addVariant(var97723020);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "0", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "0", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
        List<List<VariantEvaluation>> compHetAlleles = instance.findCompatibleCompHetAlleles(gene.getPassedVariantEvaluations());

        assertThat(compHetAlleles.size(), equalTo(2));
        assertThat(compHetAlleles.get(0), equalTo(Arrays.asList(var98518687, var97723020)));
        assertThat(compHetAlleles.get(1), equalTo(Arrays.asList(var98518683, var97723020)));
    }
}
