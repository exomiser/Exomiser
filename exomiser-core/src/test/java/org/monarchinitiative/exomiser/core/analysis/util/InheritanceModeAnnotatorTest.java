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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.PedPerson;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Sex;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeAnnotatorTest {

    @Test
    public void testAnalyseInheritanceModesEmptyInput() {
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Nemo");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);

        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(Collections.emptyList());
        assertThat(results, equalTo(Collections.emptyMap()));
    }

    @Test
    public void testAutosomalDominantIndividual() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        VariantEvaluation variant = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(variant)
                );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalRecessiveIndividual() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        VariantEvaluation variant = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(variant)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testXDominantXRecessiveIndividual() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(1));

        VariantContext variantContext = buildVariantContext(23, 12345, alleles, genotype);
        VariantEvaluation variant = filteredVariant(23, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.X_DOMINANT, ImmutableList.of(variant),
                ModeOfInheritance.X_RECESSIVE, ImmutableList.of(variant)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testXDominantFamily() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(0));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(0));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));

        VariantContext variantContext = buildVariantContext(23, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation variant = filteredVariant(23, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.AFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.X_DOMINANT, ImmutableList.of(variant)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testXRecessiveFamily() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(0));
        Genotype sister = buildUnPhasedSampleGenotype("Rachel", alleles.get(0), alleles.get(1));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(23, 12345, alleles, proband, brother, sister, mother, father);
        VariantEvaluation variant = filteredVariant(23, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.AFFECTED,Collections.emptyList());
        PedPerson sisterPerson = new PedPerson("Family", "Rachel", "Adam", "Eve", Sex.FEMALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, sisterPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.X_RECESSIVE, ImmutableList.of(variant)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testMitochondrialIndividual() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(25, 12345, alleles, genotype);
        VariantEvaluation variant = filteredVariant(25, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.MITOCHONDRIAL, ImmutableList.of(variant)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testMitochondrialFamily() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(0));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));

        VariantContext variantContext = buildVariantContext(25, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation variant = filteredVariant(25, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(variant));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.MITOCHONDRIAL, ImmutableList.of(variant)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalRecessiveFamilyOneAlt() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(1));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalRecessiveFamilyTwoAlts() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(2));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne, alleleTwo)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantFamilyOneAlt() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(0));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(0));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne)
        );

        assertThat(results, equalTo(expected));
    }

    /**
     * This is currently @Ignored due to there being a Jannovar issue with heterozygous alt alleles.
     */
    @Ignore
    @Test
    public void testAutosomalDominantFamilyTwoAlts() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(2), alleles.get(2));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(2));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testCompHetAutosomalRecessiveIndividual() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne, alleleTwo)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testCompHetAutosomalRecessiveIndividualSubModes() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testCompHetAutosomalRecessiveFamily() {

        List<Allele> alleles = buildAlleles("A", "T");
        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(0));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);


        List<Allele> alleles2 = buildAlleles("A", "C");
        Genotype proband2 = buildUnPhasedSampleGenotype("Cain", alleles2.get(0), alleles2.get(1));
        Genotype brother2 = buildUnPhasedSampleGenotype("Abel", alleles2.get(0), alleles2.get(1));
        Genotype mother2 = buildUnPhasedSampleGenotype("Eve", alleles2.get(0), alleles2.get(0));
        Genotype father2 = buildUnPhasedSampleGenotype("Adam", alleles2.get(0), alleles2.get(1));

        VariantContext variantContext2 = buildVariantContext(1, 12355, alleles2, proband2, brother2, mother2, father2);
        VariantEvaluation alleleTwo = filteredVariant(1, 12355, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext2);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, Set<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableSet.of(alleleOne, alleleTwo)
        );

        for (Map.Entry<SubModeOfInheritance, Set<VariantEvaluation>> entry : expected.entrySet()) {
            assertThat(expected.keySet().contains(entry.getKey()), is(true));
            List<VariantEvaluation> resultValue = results.get(entry.getKey());
            //In this case the order the results are returned in is not guaranteed by jannovar. Consider returning a Set in the first place?
            assertThat(entry.getValue(), equalTo(new HashSet<>(resultValue)));
        }
    }

    /**
     * This is currently @Ignored due to there being a Jannovar issue with heterozygous alt alleles.
     */
    @Ignore
    @Test
    public void testCompHetAutosomalRecessiveFamilyTwoAlts() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(2));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, Collections.emptyList());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED,Collections.emptyList());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        //TODO Something's up with alt alleles with a 0/2 genotype - the 0/1 genotype shouldn't be HET in this case?
        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );

        assertThat(results, equalTo(expected));
    }

    /**
     * This is currently @Ignored due to there being a Jannovar issue with heterozygous alt alleles.
     */
    @Ignore
    @Test
    public void testAutosomalDominantHetAltFamily() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        Genotype brother = buildPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(1));
        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.fail(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        PedPerson probandPerson = new PedPerson("Family", "Cain", "Adam", "Eve", Sex.MALE, Disease.AFFECTED, new ArrayList<String>());
        PedPerson brotherPerson = new PedPerson("Family", "Abel", "Adam", "Eve", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson motherPerson = new PedPerson("Family", "Eve", "0", "0", Sex.FEMALE, Disease.UNAFFECTED, new ArrayList<String>());
        PedPerson fatherPerson = new PedPerson("Family", "Adam", "0", "0", Sex.MALE, Disease.UNAFFECTED, new ArrayList<String>());
        Pedigree pedigree = buildPedigree(probandPerson, brotherPerson, motherPerson, fatherPerson);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividual() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree);
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }
}