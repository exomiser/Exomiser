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
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyserTest {

    private Gene newGene() {
        return new Gene("ABC", 123);
    }

    private String variantString(VariantEvaluation variant) {
        return String.format("%s\t%s\t%s\t%s\t%s\tcompatibleWith=%s",variant.getChromosome(), variant.getRef(), variant.getAlt(), variant.getAltAlleleId(), variant.getGenotypeString(), variant.getInheritanceModes());
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_NoVariants() {
        Gene gene = newGene();
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.ANY), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_NoPassedVariants() {
        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 1 , "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER)));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.ANY), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HET() {
        List<Allele> alleles = buildAlleles("A", "T");
        // build Genotype
        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));
        assertThat(genotype.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");


        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_REF_ShouldBeIncompatibleWith_RECESIVE() {
        List<Allele> alleles = buildAlleles("A", "T");

        //HomRef 0/0 or 0|0 variants really shouldn't be causing rare diseases so we need to ensure these are removed
        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_REF_ShouldBeIncompatibleWith_DOMINANT() {
        List<Allele> alleles = buildAlleles("A", "T");

        //HomRef 0/0 or 0|0 variants really shouldn't be causing rare diseases so we need to ensure these are removed
        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> assertThat(variant.getInheritanceModes().isEmpty(), is(true)));
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_OnePassedVariant_HOM_VAR() {
        List<Allele> alleles = buildAlleles("A", "T");
        //HOM_ALT
        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(1));
        assertThat(genotype.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        System.out.println("Built variant context " + variantContext);

        Gene gene = newGene();
        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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
        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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
        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(0));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_REF));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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
        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(0));
        assertThat(mother.getType(), equalTo(GenotypeType.HOM_REF));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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
        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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
    public void testAnalyseInheritanceModes_MultiSample_MultiAllelic_OnePassedVariant_HOM_VAR_altAllele2_shouldBeCompatibleWith_AR() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");
        // build Genotype
        //HomVar 1/1 or 1|1 variants are a really likely candidate for recessive rare diseases
        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(1), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, motherPerson, fatherPerson);

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype brother = buildPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(1));
        assertThat(brother.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
//        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);
//        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    System.out.println(variant);
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
                });
    }

    @Test
    public void testAnalyseInheritanceModes_SingleSample_MultiAllelic_OnePassedVariant_HET_shouldBeCompatibleWith_AD() {
        Gene gene = newGene();
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(2), alleles.get(2));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
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

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        System.out.println("Built variant context " + variantContext);

        gene.addVariant(filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));
        gene.addVariant(filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext));

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE), pedigree);
        instance.analyseInheritanceModes(gene);
        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        gene.getPassedVariantEvaluations()
                .forEach(variant -> {
                    System.out.println(variantString(variant));
                    assertThat(variant.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
                });
    }

    @Test
    public void testAnalyseMultipleInheritanceModesForSingleSample() {
        List<Allele> hetAltAlleles = buildAlleles("A", "T", "C");
        Genotype hetAltGenotype = buildPhasedSampleGenotype("Cain", hetAltAlleles.get(2), hetAltAlleles.get(2));
        VariantContext variantContext = buildVariantContext(1, 12345, hetAltAlleles, hetAltGenotype);
        VariantEvaluation hetAltVar1 = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation hetAltVar2 = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        List<Allele> homAlt = buildAlleles("C", "G");
        Genotype homAltGenotype = buildPhasedSampleGenotype("Cain", homAlt.get(1), homAlt.get(1));
        VariantContext homAltVariantContext = buildVariantContext(1, 12345, homAlt, homAltGenotype);
        VariantEvaluation homAltVar = filteredVariant(1, 12355, "C", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), homAltVariantContext);

        List<Allele> het1 = buildAlleles("C", "G");
        Genotype hetGenotype1 = buildPhasedSampleGenotype("Cain", het1.get(0), het1.get(1));
        VariantContext hetVariantContext1 = buildVariantContext(1, 12365, het1, hetGenotype1);
        VariantEvaluation hetVar1 = filteredVariant(1, 12365, "C", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetVariantContext1);

        List<Allele> het2 = buildAlleles("G", "T");
        Genotype hetGenotype2 = buildPhasedSampleGenotype("Cain", het2.get(0), het2.get(1));
        VariantContext hetVariantContext2 = buildVariantContext(1, 12375, het2, hetGenotype2);
        VariantEvaluation hetVar2 = filteredVariant(1, 12375, "G", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetVariantContext2);


        Gene gene = newGene();
        gene.addVariant(hetAltVar1);
        gene.addVariant(hetAltVar2);
        gene.addVariant(homAltVar);
        gene.addVariant(hetVar1);
        gene.addVariant(hetVar2);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.AUTOSOMAL_DOMINANT), pedigree);
        instance.analyseInheritanceModes(gene);

        assertThat(gene.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(gene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        assertThat(hetAltVar1.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
        assertThat(hetAltVar2.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE)));

        assertThat(homAltVar.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE)));

        assertThat(hetVar1.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
        assertThat(hetVar2.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
    }


    @Test
    public void testAnalyseMultipleInheritanceModesMultipleGenesForSingleSample() {

        List<Allele> hetAltAlleles1 = buildAlleles("A", "T");
        Genotype hetAltGenotype1 = buildPhasedSampleGenotype("Cain", hetAltAlleles1.get(0), hetAltAlleles1.get(1));
        VariantContext hetAltVariantContext1 = buildVariantContext(1, 12345, hetAltAlleles1, hetAltGenotype1);
        VariantEvaluation hetAltVar1 = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetAltVariantContext1);
//        hetAltVar1.setSampleGenotype("Sample", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT));

        List<Allele> hetAltAlleles2 = buildAlleles("G", "C");
        Genotype hetAltGenotype2 = buildPhasedSampleGenotype("Cain", hetAltAlleles2.get(0), hetAltAlleles2.get(1));
        VariantContext hetAltVariantContext2 = buildVariantContext(1, 12355, hetAltAlleles2, hetAltGenotype2);
        VariantEvaluation hetAltVar2 = filteredVariant(1, 12355, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetAltVariantContext2);

        Gene geneRecessiveAutosomal = newGene();
        geneRecessiveAutosomal.addVariant(hetAltVar1);
        geneRecessiveAutosomal.addVariant(hetAltVar2);


        List<Allele> homAlt = buildAlleles("C", "G");
        Genotype homAltGenotype = buildPhasedSampleGenotype("Cain", homAlt.get(1), homAlt.get(1));
        VariantContext homAltVariantContext = buildVariantContext(23, 12345, homAlt, homAltGenotype);
        VariantEvaluation homAltXVar = filteredVariant(23, 12355, "C", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), homAltVariantContext);

        Gene geneRecessiveX = newGene();
        geneRecessiveX.addVariant(homAltXVar);

        List<Allele> het1 = buildAlleles("C", "G");
        Genotype hetGenotype1 = buildPhasedSampleGenotype("Cain", het1.get(0), het1.get(1));
        VariantContext hetVariantContext1 = buildVariantContext(22, 12365, het1, hetGenotype1);
        VariantEvaluation autoHetVar = filteredVariant(2, 12365, "C", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetVariantContext1);

        Gene geneDominantAutosomal = newGene();
        geneDominantAutosomal.addVariant(autoHetVar);


        List<Allele> het2 = buildAlleles("G", "T");
        Genotype hetGenotype2 = buildPhasedSampleGenotype("Cain", het2.get(0), het2.get(1));
        VariantContext hetVariantContext2 = buildVariantContext(25, 12375, het2, hetGenotype2);
        VariantEvaluation mitoHetVar = filteredVariant(25, 12375, "G", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), hetVariantContext2);

        Gene geneMitochondrial = newGene();
        geneMitochondrial.addVariant(mitoHetVar);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        EnumSet<ModeOfInheritance> wantedModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT, ModeOfInheritance.MITOCHONDRIAL);
        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(wantedModes, pedigree);
        instance.analyseInheritanceModes(Arrays.asList(geneRecessiveAutosomal, geneRecessiveX, geneDominantAutosomal, geneMitochondrial));

        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(geneRecessiveAutosomal.isCompatibleWith(ModeOfInheritance.MITOCHONDRIAL), is(false));

        assertThat(hetAltVar1.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE)));
        assertThat(hetAltVar2.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE)));


        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(true));
        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(geneRecessiveX.isCompatibleWith(ModeOfInheritance.MITOCHONDRIAL), is(false));

        assertThat(homAltXVar.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT)));


        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(geneDominantAutosomal.isCompatibleWith(ModeOfInheritance.MITOCHONDRIAL), is(false));

        assertThat(autoHetVar.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT)));

        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(geneMitochondrial.isCompatibleWith(ModeOfInheritance.MITOCHONDRIAL), is(true));

        assertThat(mitoHetVar.getInheritanceModes(), equalTo(EnumSet.of(ModeOfInheritance.MITOCHONDRIAL)));
    }
}
