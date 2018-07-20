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
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeAnnotatorTest {

    private Pedigree singleAffectedSample(String id) {
        return Pedigree.of(Individual.builder().id(id).status(Status.AFFECTED).build());
    }

    @Test
    public void testAnalyseInheritanceModesEmptyInput() {
        Pedigree pedigree = singleAffectedSample("Nemo");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());

        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(Collections.emptyList());
        assertThat(results, equalTo(Collections.emptyMap()));
    }

    @Test
    public void testAutosomalDominantIndividual() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);
        VariantEvaluation variant = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = singleAffectedSample("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Pedigree pedigree = singleAffectedSample("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Pedigree pedigree = singleAffectedSample("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.AFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual sisterIndividual = Individual.builder().id("Rachel").fatherId("Adam").motherId("Eve").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.AFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual, sisterIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Pedigree pedigree = singleAffectedSample("Adam");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.AFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Pedigree pedigree = singleAffectedSample("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne, alleleTwo)
        );

        assertThat(results, equalTo(expected));
    }

    @Test
    public void testCompHetAutosomalRecessiveIndividualWithFrequencyThreshold() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(1f, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        alleleTwo.setFrequencyData(FrequencyData.of(Frequency.valueOf(1f, FrequencySource.LOCAL)));

        Pedigree pedigree = singleAffectedSample("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        //Both alleles are potentially dominant, but they are deemed too common in the population to be causative of a rare disease
        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
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

        Pedigree pedigree = singleAffectedSample("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.UNAFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual, brotherIndividual);

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
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

        Pedigree pedigree = singleAffectedSample("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults());
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividualWithSubModeOfInheritanceFrequencies() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        float alleleOneMaxFreq = 1.0f;
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(alleleOneMaxFreq, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        alleleTwo.setFrequencyData(FrequencyData.of(Frequency.valueOf(alleleOneMaxFreq / 2f, FrequencySource.LOCAL)));

        Pedigree pedigree = singleAffectedSample("Cain");

        ImmutableMap<SubModeOfInheritance, Float> maxMafMap = ImmutableMap.of(
                //Set the max MAF to be under that of this allele - it should not be seen as being compatible with this mode
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, alleleOneMaxFreq - 0.1f
        );
        InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.of(maxMafMap);
        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, inheritanceModeOptions);

        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividualWithModeOfInheritanceFrequencies() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        float alleleOneMaxFreq = 1.0f;
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(alleleOneMaxFreq, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = singleAffectedSample("Cain");

        ImmutableMap<SubModeOfInheritance, Float> maxMafMap = ImmutableMap.of(
                //Set the max MAF to be under that of this allele - it should not be seen as being compatible with this mode
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, alleleOneMaxFreq - 0.1f
        );
        InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.of(maxMafMap);
        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, inheritanceModeOptions);

        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleTwo),
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividualWithEmptyMaxFrequencies() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        //Set the frequency data to be over that of the default frequency value
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(1f, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = singleAffectedSample("Cain");

        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.empty());

        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividualWithJustAnyMaxFrequencies() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        //Set the frequency data to be over that of the default frequency value
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(1f, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = singleAffectedSample("Cain");

        ImmutableMap<SubModeOfInheritance, Float> modeMaxMafs = ImmutableMap.of(SubModeOfInheritance.ANY, 2.0f);
        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.of(modeMaxMafs));

        Map<SubModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceSubModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<SubModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                SubModeOfInheritance.ANY, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleOne, alleleTwo),
                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }

    @Test
    public void testAutosomalDominantHetAltIndividualWithModeOfInheritanceAnyFrequencies() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);
        VariantEvaluation alleleOne = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);
        float alleleOneMaxFreq = 1.0f;
        alleleOne.setFrequencyData(FrequencyData.of(Frequency.valueOf(alleleOneMaxFreq, FrequencySource.LOCAL)));

        VariantEvaluation alleleTwo = filteredVariant(1, 12345, "A", "C", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext);

        Pedigree pedigree = singleAffectedSample("Cain");

        ImmutableMap<SubModeOfInheritance, Float> maxMafMap = ImmutableMap.of(
                SubModeOfInheritance.ANY, 2.0f,
                //Set the max MAF to be under that of this allele - it should not be seen as being compatible with this mode
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, alleleOneMaxFreq - 0.1f
        );
        InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.of(maxMafMap);
        InheritanceModeAnnotator instance = new InheritanceModeAnnotator(pedigree, inheritanceModeOptions);

        Map<ModeOfInheritance, List<VariantEvaluation>> results = instance.computeCompatibleInheritanceModes(ImmutableList.of(alleleOne, alleleTwo));

        Map<ModeOfInheritance, List<VariantEvaluation>> expected = ImmutableMap.of(
                ModeOfInheritance.ANY, ImmutableList.of(alleleOne, alleleTwo),
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(alleleTwo),
                ModeOfInheritance.AUTOSOMAL_RECESSIVE, ImmutableList.of(alleleOne, alleleTwo)
        );
        assertThat(results, equalTo(expected));
    }
}