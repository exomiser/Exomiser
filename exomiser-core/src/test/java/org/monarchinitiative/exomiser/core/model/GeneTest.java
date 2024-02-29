/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.writers.JsonVariantMixin;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneTest {

    private Gene instance;

    private static final String GENE1_SYMBOL = "GENE1";
    private static final String GENE1_GENE_ID = "1234567";
    private static final int GENE1_ENTREZ_GENE_ID = 1234567;
    private static final GeneIdentifier GENE1_GENE_IDENTIFIER = GeneIdentifier.builder()
            .geneSymbol(GENE1_SYMBOL)
            .geneId(GENE1_GENE_ID)
            .entrezId(GENE1_GENE_ID)
            .build();

    private VariantEvaluation variantEvaluation1;
    private VariantEvaluation variantEvaluation2;

    private static final FilterResult PASS_VARIANT_FILTER_RESULT = FilterResult.pass(FilterType.FREQUENCY_FILTER);
    private static final FilterResult FAIL_VARIANT_FILTER_RESULT = FilterResult.fail(FilterType.FREQUENCY_FILTER);
    //there's nothing magical about a FilterResult being a Gene or Variant filter result, it's where/how they are used which makes the difference.
    //their type is used for reporting which filter was passed or failed.
    private static final FilterResult PASS_GENE_FILTER_RESULT = FilterResult.pass(FilterType.INHERITANCE_FILTER);
    private static final FilterResult FAIL_GENE_FILTER_RESULT = FilterResult.fail(FilterType.INHERITANCE_FILTER);

    @BeforeEach
    public void setUp() {
        // variant1 is the first one in in FGFR2 gene
        variantEvaluation1 = TestFactory.variantBuilder(10, 123353320, "C", "G").build();
        // variant2 is the second one in in FGFR2 gene
        variantEvaluation2 = TestFactory.variantBuilder(10, 123353325, "T", "A").build();

        instance = newGeneOne();
    }

    private Gene newGeneOne() {
        return new Gene(GENE1_GENE_IDENTIFIER);
    }

    private Gene newGeneTwo() {
        GeneIdentifier geneIdentifier = GeneIdentifier.builder()
                .geneSymbol("GENE2")
                .geneId("654321")
                .entrezId("654321")
                .build();
        return new Gene(geneIdentifier);
    }

    @Test
    public void testConstructorWithGeneIdentifier() {
        Gene gene = new Gene(GENE1_GENE_IDENTIFIER);
        assertThat(gene.getGeneSymbol(), equalTo(GENE1_SYMBOL));
        assertThat(gene.getEntrezGeneID(), equalTo(GENE1_ENTREZ_GENE_ID));
        assertThat(gene.getGeneIdentifier(), equalTo(GENE1_GENE_IDENTIFIER));
    }

    @Test
    public void testAlternateConstructor() {
        Gene gene = new Gene(GENE1_SYMBOL, GENE1_ENTREZ_GENE_ID);
        assertThat(gene.getGeneSymbol(), equalTo(GENE1_SYMBOL));
        assertThat(gene.getEntrezGeneID(), equalTo(GENE1_ENTREZ_GENE_ID));
        assertThat(gene.getGeneIdentifier(), equalTo(GENE1_GENE_IDENTIFIER));
    }

    @Test
    public void testConstructorChecksForNull() {
        assertThrows(NullPointerException.class, () -> new Gene(null));
    }

    @Test
    public void testConstructorChecksForNullGeneIdentifierGeneSymbol() {
        assertThrows(NullPointerException.class, () ->
                new Gene(GeneIdentifier.builder().geneSymbol(null).entrezId(GENE1_GENE_ID).geneId(GENE1_GENE_ID).build())
        );
    }

    @Test
    public void testConstructorChecksForNullGeneIdentifierGeneId() {
        assertThrows(NullPointerException.class, () ->
                new Gene(GeneIdentifier.builder().geneSymbol(GENE1_SYMBOL).geneId(null).entrezId(GENE1_GENE_ID).build())
        );
    }

    @Test
    public void testConstructorChecksForNullGeneIdentifierEntrezId() {
        assertThrows(NullPointerException.class, () ->
                new Gene(GeneIdentifier.builder().geneSymbol(GENE1_SYMBOL).geneId(GENE1_GENE_ID).entrezId(null).build())
        );
    }

    @Test
    public void testConstructorChecksForEmptyGeneIdentifierGeneSymbol() {
        assertThrows(IllegalArgumentException.class, () ->
                new Gene(GeneIdentifier.builder().geneSymbol("").geneId(GENE1_GENE_ID).entrezId(GENE1_GENE_ID).build())
        );
    }

    @Test
    public void canGetGeneIdentifier() {
        assertThat(instance.getGeneIdentifier(), equalTo(GENE1_GENE_IDENTIFIER));
    }

    @Test
    public void canGetGeneId() {
        assertThat(instance.getGeneId(), equalTo(String.valueOf(GENE1_ENTREZ_GENE_ID)));
    }

    @Test
    public void testConstructorSetsInstanceVariables() {
        instance.addVariant(variantEvaluation1);

        List<VariantEvaluation> expectedVariantEvaluations = new ArrayList<>();
        expectedVariantEvaluations.add(variantEvaluation1);

        assertThat(instance.getGeneSymbol(), equalTo(GENE1_SYMBOL));
        assertThat(instance.getEntrezGeneID(), equalTo(GENE1_ENTREZ_GENE_ID));
        assertThat(instance.getVariantEvaluations(), equalTo(expectedVariantEvaluations));
        assertThat(instance.getNumberOfVariants(), equalTo(1));
        assertThat(instance.hasVariants(), is(true));

        assertThat(instance.passedFilters(), is(true));
        assertThat(instance.getPriorityResults().isEmpty(), is(true));

        assertThat(instance.getGeneScores().isEmpty(), is(true));
    }

    @Test
    public void testConstructorSetsInstanceVariablesNoVariant() {
        Gene emptyGene = new Gene(GENE1_GENE_IDENTIFIER);
        List<VariantEvaluation> expectedVariantEvaluations = new ArrayList<>();

        assertThat(emptyGene.getGeneSymbol(), equalTo(GENE1_SYMBOL));
        assertThat(emptyGene.getEntrezGeneID(), equalTo(GENE1_ENTREZ_GENE_ID));
        assertThat(emptyGene.getVariantEvaluations(), equalTo(expectedVariantEvaluations));
        assertThat(emptyGene.getNumberOfVariants(), equalTo(0));
        assertThat(emptyGene.hasVariants(), is(false));

        assertThat(emptyGene.passedFilters(), is(true));
        assertThat(emptyGene.getPriorityResults().isEmpty(), is(true));

        assertThat(instance.getGeneScores().isEmpty(), is(true));
    }

    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual() {
        Gene gene1 = newGeneOne();
        Gene gene2 = newGeneTwo();

        assertTrue(gene1.compareTo(gene2) < 0);
        assertTrue(gene2.compareTo(gene1) > 0);
    }

    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByCombinedScore() {
        Gene gene1 = newGeneOne();
        Gene gene2 = newGeneTwo();

        gene1.addGeneScore(GeneScore.builder().combinedScore(0.5f).build());
        gene2.addGeneScore(GeneScore.builder().combinedScore(1).build());

        assertTrue(gene1.compareTo(gene2) > 0);
        assertTrue(gene2.compareTo(gene1) < 0);
    }

    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual() {
        Gene gene1 = newGeneOne();
        Gene gene2 = newGeneOne();

        assertTrue(gene1.compareTo(gene2) == 0);
        assertTrue(gene2.compareTo(gene1) == 0);
    }

    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByCombinedScore() {
        Gene gene1 = newGeneOne();
        Gene gene2 = newGeneOne();

        gene1.addGeneScore(GeneScore.builder().combinedScore(0.5f).build());
        gene2.addGeneScore(GeneScore.builder().combinedScore(1).build());

        assertTrue(gene1.compareTo(gene2) > 0);
        assertTrue(gene2.compareTo(gene1) < 0);
    }

    @Test
    public void testPassesFiltersTrueWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFiltersTrueWhenPassesGeneFilterOnly() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFiltersFalseWhenFailsGeneFilterOnly() {
        instance.addFilterResult(FAIL_GENE_FILTER_RESULT);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFiltersTrueWhenPassesGeneAndVariantFilters() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFiltersFalseWhenFailsGeneFilterButPassesVariantFilter() {
        instance.addFilterResult(FAIL_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFiltersFalseWhenPassesGeneFilterButFailsVariantFilters() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFiltersTrueWhenPassesGeneFilterAndAtLeastOneVariantFilter() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        variantEvaluation2.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFilterTrueWhenGenePassesAndVariantsFailFilterOfThatType() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_GENE_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(PASS_GENE_FILTER_RESULT.getFilterType()), is(true));
    }

    @Test
    public void testPassedFilterTrueWhenGeneUnfilteredAndVariantPassesFilterOfThatType() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
    }

    @Test
    public void testPassedFilterFalseWhenGeneUnfilteredAndVariantsFailFilterOfThatType() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(FAIL_VARIANT_FILTER_RESULT.getFilterType()), is(false));
    }

    @Test
    public void testPassedFilterTrueWhenGeneUnfilteredAndAtLeastOneVariantPassesFilterOfThatType() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        variantEvaluation2.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);
        assertThat(instance.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
    }

    @Test
    public void testPassesFiltersTrueWhenVariantPassesFilter() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFiltersTrueWhenAtLeastOneVariantPassesFilter() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);

        variantEvaluation2.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);

        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFiltersFalseWhenVariantFailsFilter() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testGetPassedVariantEvaluationsIsEmptyWhenVariantFailsFilter() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.getPassedVariantEvaluations().isEmpty(), is(true));
    }

    @Test
    public void testGetPassedVariantEvaluations() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);

        variantEvaluation2.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);

        List<VariantEvaluation> passedVariantEvaluations = List.of(variantEvaluation1);

        assertThat(instance.getPassedVariantEvaluations(), equalTo(passedVariantEvaluations));
    }

    @Test
    public void testGetNonContributingPassedVariantEvaluations() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        variantEvaluation1.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        instance.addVariant(variantEvaluation1);

        variantEvaluation2.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);

        List<VariantEvaluation> nonContributingPassedVariantEvaluations = List.of(variantEvaluation2);

        assertThat(instance.getNonContributingPassedVariantEvaluations(), equalTo(nonContributingPassedVariantEvaluations));
    }


    @Test
    public void testAddVariantAfterGeneIsFilteredAppliesPassGeneFilterResultsToVariant() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addFilterResult(FilterResult.pass(FilterType.PRIORITY_SCORE_FILTER));

        instance.addVariant(variantEvaluation1);

        assertThat(variantEvaluation1.passedFilters(), is(true));
        assertThat(variantEvaluation1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));
    }

    @Test
    public void testAddVariantAfterGeneIsFilteredAppliesFailGeneFilterResultsToVariant() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addFilterResult(FilterResult.fail(FilterType.PRIORITY_SCORE_FILTER));

        instance.addVariant(variantEvaluation1);

        assertThat(variantEvaluation1.passedFilters(), is(false));
        assertThat(variantEvaluation1.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
        assertThat(variantEvaluation1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
    }

    @Test
    public void testAddVariantAfterGeneIsFilteredDoesNotApplyInheritanceFilterResultToVariant() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);

        instance.addFilterResult(FilterResult.pass(FilterType.PRIORITY_SCORE_FILTER));
        instance.addFilterResult(FilterResult.fail(FilterType.INHERITANCE_FILTER));

        instance.addVariant(variantEvaluation1);

        assertThat(variantEvaluation1.passedFilters(), is(true));
        assertThat(variantEvaluation1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));
        assertThat(variantEvaluation1.getFailedFilterTypes().contains(FilterType.INHERITANCE_FILTER), is(false));
    }

    @Test
    public void testCanAddAndRetrievePriorityScoreByPriorityType() {
        PriorityType priorityType = PriorityType.OMIM_PRIORITY;
        PriorityResult omimPriorityResult = new MockPriorityResult(priorityType, instance.getEntrezGeneID(), instance.getGeneSymbol(), 0f);

        instance.addPriorityResult(omimPriorityResult);
        instance.addPriorityResult(new ExomeWalkerPriorityResult(instance.getEntrezGeneID(), instance.getGeneSymbol(), 0.0d));
        assertThat(instance.getPriorityResult(priorityType), equalTo(omimPriorityResult));
    }

    @Test
    public void testCanAddAndRetrievePriorityScoreByPriorityClass() {
        MockPriorityResult mockPriorityResult = new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, instance.getEntrezGeneID(), instance
                .getGeneSymbol(), 1d);
        instance.addPriorityResult(mockPriorityResult);

        assertThat(instance.getPriorityResult(MockPriorityResult.class), equalTo(mockPriorityResult));
        assertThat(instance.getPriorityResult(HiPhivePriorityResult.class), equalTo(null));
    }

    @Test
    public void defaultInheritanceModesAreEmpty() {
        assertThat(instance.getCompatibleInheritanceModes(), notNullValue());
        assertThat(instance.getCompatibleInheritanceModes().isEmpty(), is(true));
    }

    @Test
    public void canSetAndGetInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_DOMINANT);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.getCompatibleInheritanceModes(), equalTo(inheritanceModes));
    }

    @Test
    public void isConsistentWithInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE,
                ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(true));
        assertThat(instance.isCompatibleWithDominant(), is(true));
        assertThat(instance.isCompatibleWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(true));
    }

    @Test
    public void isConsistentWithDominantInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(instance.isCompatibleWithDominant(), is(true));
        assertThat(instance.isCompatibleWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(false));
    }

    @Test
    public void isConsistentWithRecessiveInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(instance.isCompatibleWithDominant(), is(false));
        assertThat(instance.isCompatibleWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(false));
    }

    @Test
    public void isConsistentWithXRecessiveInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.X_RECESSIVE);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(instance.isCompatibleWithDominant(), is(false));
        assertThat(instance.isCompatibleWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(true));
    }

    @Test
    public void isConsistentWithXDominantInheritanceModes() {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.X_DOMINANT);

        instance.setCompatibleInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(true));
        assertThat(instance.isCompatibleWithDominant(), is(true));
        assertThat(instance.isCompatibleWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(true));
    }

    @Test
    public void testIsCompatibleWithXfalseWhenVariantsIsEmpty() {
        instance = newGeneOne();
        assertThat(instance.isXChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithXfalseWhenVariantIsNotCompatibleWithX() {
        instance = newGeneOne();
        instance.addVariant(TestFactory.variantBuilder(1, 1, "A", "T").build());
        assertThat(instance.isXChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithXtrueWhenVariantIsCompatibleWithX() {
        int X_CHROMOSOME = 23;
        instance = newGeneOne();
        instance.addVariant(TestFactory.variantBuilder(X_CHROMOSOME, 1, "A", "T").build());
        assertThat(instance.isXChromosomal(), is(true));
    }

    @Test
    public void testIsCompatibleWithYisFalseWhenVariantsIsEmpty() {
        instance = newGeneOne();
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithYisFalseWhenVariantIsNotCompatibleWithX() {
        instance = newGeneOne();
        instance.addVariant(TestFactory.variantBuilder(1, 1, "A", "T").build());
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithYisTrueWhenVariantIsCompatibleWithX() {
        int Y_CHROMOSOME = 24;
        instance = newGeneOne();
        instance.addVariant(TestFactory.variantBuilder(Y_CHROMOSOME, 1, "A", "T").build());
        assertThat(instance.isYChromosomal(), is(true));
    }

    @Test
    public void testGetGeneScoreForMode() {
        assertThat(instance.getGeneScores().isEmpty(), is(true));

        ModeOfInheritance modeOfInheritanceAD = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        GeneScore geneScoreAD = GeneScore.builder()
                .combinedScore(1f)
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(modeOfInheritanceAD)
                .build();
        instance.addGeneScore(geneScoreAD);

        assertThat(instance.getGeneScoreForMode(modeOfInheritanceAD), equalTo(geneScoreAD));
        assertThat(instance.getGeneScores(), equalTo(List.of(geneScoreAD)));

        ModeOfInheritance modeOfInheritanceAR = ModeOfInheritance.AUTOSOMAL_RECESSIVE;
        GeneScore geneScoreAR = GeneScore.builder()
                .combinedScore(1f)
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(modeOfInheritanceAR)
                .build();
        instance.addGeneScore(geneScoreAR);

        assertThat(instance.getGeneScores(), equalTo(List.of(geneScoreAD, geneScoreAR)));

    }

    @Test
    public void testScoresChangeWhenHigherGeneScoreAdded() {

        GeneScore defaultGeneScore = GeneScore.builder()
                .modeOfInheritance(ModeOfInheritance.ANY)
                .geneIdentifier(instance.getGeneIdentifier())
                .build();

        assertThat(instance.getTopGeneScore(), equalTo(defaultGeneScore));

        assertThat(instance.getVariantScore(), equalTo(defaultGeneScore.getVariantScore()));
        assertThat(instance.getPriorityScore(), equalTo(defaultGeneScore.getPhenotypeScore()));
        assertThat(instance.getCombinedScore(), equalTo(defaultGeneScore.getCombinedScore()));
        assertThat(instance.getGeneScores(), equalTo(List.of()));

        //test returns zero with no score
        assertThat(instance.getPriorityScoreForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(0d));

        double phenotypeScore = 1d;
        GeneScore firstGeneScore = GeneScore.builder()
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .phenotypeScore(0.5d)
                .variantScore(0.5d)
                .combinedScore(0.5d)
                .build();
        instance.addGeneScore(firstGeneScore);

        assertThat(instance.getTopGeneScore(), equalTo(firstGeneScore));

        assertThat(instance.getVariantScore(), equalTo(firstGeneScore.getVariantScore()));
        assertThat(instance.getPriorityScore(), equalTo(firstGeneScore.getPhenotypeScore()));
        assertThat(instance.getCombinedScore(), equalTo(firstGeneScore.getCombinedScore()));
        assertThat(instance.getGeneScores(), equalTo(List.of(firstGeneScore)));

        GeneScore secondGeneScore = GeneScore.builder()
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .phenotypeScore(1d)
                .variantScore(1d)
                .combinedScore(1d)
                .build();
        instance.addGeneScore(secondGeneScore);

        assertThat(instance.getTopGeneScore(), equalTo(secondGeneScore));

        assertThat(instance.getVariantScore(), equalTo(secondGeneScore.getVariantScore()));
        assertThat(instance.getPriorityScore(), equalTo(secondGeneScore.getPhenotypeScore()));
        assertThat(instance.getCombinedScore(), equalTo(secondGeneScore.getCombinedScore()));
        assertThat(instance.getGeneScores(), equalTo(List.of(firstGeneScore, secondGeneScore)));
    }

    @Test
    public void testCanSetAndChangeGeneScore() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        assertThat(instance.getGeneScores().isEmpty(), is(true));
        //test returns zero with no score
        assertThat(instance.getPriorityScoreForMode(modeOfInheritance), equalTo(0d));

        double phenotypeScore = 1d;
        GeneScore firstGeneScore = GeneScore.builder()
                .phenotypeScore(phenotypeScore)
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(modeOfInheritance)
                .build();
        instance.addGeneScore(firstGeneScore);
        assertThat(instance.getPriorityScoreForMode(modeOfInheritance), equalTo(phenotypeScore));

        double secondScore = 1d;
        GeneScore secondGeneScore = GeneScore.builder()
                .phenotypeScore(phenotypeScore)
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(modeOfInheritance)
                .build();
        instance.addGeneScore(secondGeneScore);
        assertThat(instance.getPriorityScoreForMode(modeOfInheritance), equalTo(secondScore));
    }

    @Test
    public void testInheritanceModeComparator() {
        Gene topAutosomalDominant = TestFactory.newGeneFGFR2();
        //add gene scores for mois
        GeneScore fgfr2AutoDomScore = GeneScore.builder().geneIdentifier(topAutosomalDominant.getGeneIdentifier())
                .combinedScore(1f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        topAutosomalDominant.addGeneScore(fgfr2AutoDomScore);

        GeneScore fgfr2AutoRecScore = GeneScore.builder().geneIdentifier(topAutosomalDominant.getGeneIdentifier())
                .combinedScore(0.5f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        topAutosomalDominant.addGeneScore(fgfr2AutoRecScore);

        Gene topAutosomalRecessive = TestFactory.newGeneRBM8A();
        GeneScore rbm8aAutoDomScore = GeneScore.builder().geneIdentifier(topAutosomalRecessive.getGeneIdentifier())
                .combinedScore(0.5f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        topAutosomalRecessive.addGeneScore(rbm8aAutoDomScore);

        GeneScore rbm8aAutoRecScore = GeneScore.builder().geneIdentifier(topAutosomalRecessive.getGeneIdentifier())
                .combinedScore(1f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        topAutosomalRecessive.addGeneScore(rbm8aAutoRecScore);

        Gene noScores = TestFactory.newGeneSHH();

        List<Gene> genes = Arrays.asList(topAutosomalDominant, topAutosomalRecessive, noScores);
        //test sorting by AD
        genes.sort(Gene.comparingScoreForInheritanceMode(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(genes, equalTo(Arrays.asList(topAutosomalDominant, topAutosomalRecessive, noScores)));

        // test sorting by AR
        genes.sort(Gene.comparingScoreForInheritanceMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(genes, equalTo(Arrays.asList(topAutosomalRecessive, topAutosomalDominant, noScores)));

        // test sort by ANY
        genes.sort(Gene.comparingScoreForInheritanceMode(ModeOfInheritance.ANY));
        //these should be sorted by combined score desc, gene symbols asc
        assertThat(genes, equalTo(Arrays.asList(topAutosomalDominant, topAutosomalRecessive, noScores)));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("Gene{geneSymbol='GENE1', entrezGeneId=1234567, compatibleWith=[], filterStatus=PASSED, failedFilterTypes=[], passedFilterTypes=[], combinedScore=0.0, phenotypeScore=0.0, variantScore=0.0, variants=0}"));
    }

    @Test
    void testGetAssociatedDiseases() {
        Gene instance = TestFactory.newGeneFGFR2();
        assertThat(instance.getAssociatedDiseases().isEmpty(), is(true));
        Disease pfeifferSyndrome = Disease.builder().diseaseId("OMIM:101600").diseaseName("Pfeiffer syndrome").build();
        OmimPriorityResult priorityResult = new OmimPriorityResult(instance.getEntrezGeneID(), instance.getGeneSymbol(), 1.0, List.of(pfeifferSyndrome), Map.of());
        instance.addPriorityResult(priorityResult);
        assertThat(instance.getAssociatedDiseases(), equalTo(List.of(pfeifferSyndrome)));
    }

    @Test
    public void testGetCompatibleGeneScores() throws Exception {
        Gene instance = TestFactory.newGeneFGFR2();
        // Hmm... this is a bit of a WFT - why does this need to be set rather than computed from the variants?
        //  ... because it gets set once by the InheritanceModeAnalyser after all the variants have been filtered
        instance.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(instance.getCompatibleGeneScores().isEmpty(), is(true));

        GeneScore adGeneScore = GeneScore.builder()
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .phenotypeScore(0.5d)
                .variantScore(0.5d)
                .combinedScore(0.5d)
                .pValue(0.0000001)
                .build();

        instance.addGeneScore(adGeneScore);

        ObjectWriter objectWriter = new ObjectMapper()
                .addMixIn(Variant.class, JsonVariantMixin.class)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
                .writerWithDefaultPrettyPrinter();

        assertThat(instance.getCompatibleInheritanceModes(), equalTo(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT)));
        assertThat(instance.getCompatibleGeneScores(), equalTo(List.of(adGeneScore)));
    }

    @Test
    public void testGetCompatibleGeneScoresNoCompatibleMoi() throws Exception {
        Gene instance = TestFactory.newGeneFGFR2();
        // Hmm... this is a bit of a WFT - why does this need to be set rather than computed from the variants?
        //  ... because it gets set once by the InheritanceModeAnalyser after all the variants have been filtered
//        instance.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(instance.getCompatibleGeneScores().isEmpty(), is(true));

        GeneScore anyGeneScore = GeneScore.builder()
                .geneIdentifier(instance.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .phenotypeScore(0.5d)
                .variantScore(0.5d)
                .combinedScore(0.5d)
                .pValue(0.0000001)
                .build();

        instance.addGeneScore(anyGeneScore);

        assertThat(instance.getCompatibleInheritanceModes().isEmpty(), is(true));
        assertThat(instance.getCompatibleGeneScores(), equalTo(List.of(anyGeneScore)));
    }
}
