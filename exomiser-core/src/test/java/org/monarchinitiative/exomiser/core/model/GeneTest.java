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
package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.prioritisers.ExomeWalkerPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
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

    @Before
    public void setUp() {
        // variant1 is the first one in in FGFR2 gene
        variantEvaluation1 = new VariantEvaluation.Builder(10, 123353320, "C", "G").build();
        // variant2 is the second one in in FGFR2 gene
        variantEvaluation2 = new VariantEvaluation.Builder(10, 123353325, "T", "A").build();

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

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksForNull() {
        new Gene(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksForNullGeneIdentifierGeneSymbol() {
        new Gene(GeneIdentifier.builder().geneSymbol(null).entrezId(GENE1_GENE_ID).geneId(GENE1_GENE_ID).build());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksForNullGeneIdentifierGeneId() {
        new Gene(GeneIdentifier.builder().geneSymbol(GENE1_SYMBOL).geneId(null).entrezId(GENE1_GENE_ID).build());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksForNullGeneIdentifierEntrezId() {
        new Gene(GeneIdentifier.builder().geneSymbol(GENE1_SYMBOL).geneId(GENE1_GENE_ID).entrezId(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorChecksForEmptyGeneIdentifierGeneSymbol() {
        new Gene(GeneIdentifier.builder().geneSymbol("").geneId(GENE1_GENE_ID).entrezId(GENE1_GENE_ID).build());
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

        assertThat(instance.getFilterScore(), equalTo(0f));
        assertThat(instance.getPriorityScore(), equalTo(0f));
        assertThat(instance.getCombinedScore(), equalTo(0f));
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

        assertThat(emptyGene.getFilterScore(), equalTo(0f));
        assertThat(emptyGene.getPriorityScore(), equalTo(0f));
        assertThat(emptyGene.getCombinedScore(), equalTo(0f));
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

        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);

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

        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);

        assertTrue(gene1.compareTo(gene2) > 0);
        assertTrue(gene2.compareTo(gene1) < 0);
    }

    @Test
    public void testPassesFilters_TrueWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFilters_TrueWhenPassesGeneFilterOnly() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFilters_FalseWhenFailsGeneFilterOnly() {
        instance.addFilterResult(FAIL_GENE_FILTER_RESULT);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFilters_TrueWhenPassesGeneAndVariantFilters() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassedFilters_FalseWhenFailsGeneFilterButPassesVariantFilter() {
        instance.addFilterResult(FAIL_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFilters_FalseWhenPassesGeneFilterButFailsVariantFilters() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassedFilters_TrueWhenPassesGeneFilterAndAtLeastOneVariantFilter() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        variantEvaluation2.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);
        assertThat(instance.passedFilters(), is(true));
    }
//TODO: behaviour under consideration - better in the gene or the gene filter runner? Should it apply to all gene filters?
//    @Test
//    public void testAddingFilterResultToGeneAppliesThatResultToAllVariantsOfTheGene() {
//        //set-up gene
//        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
//        instance.addVariant(variantEvaluation1);
//        //simluate filtering
//        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
//        
//        //test the variant still fails the original filter
//        assertThat(variantEvaluation1.passedFilter(FAIL_VARIANT_FILTER_RESULT.getFilterType()), is(false));
//        //but that the variant also passes the gene filter - this is OK behaviour as Variants fail fast - i.e. we really only care if a variant passed ALL filters
//        assertThat(variantEvaluation1.passedFilter(PASS_GENE_FILTER_RESULT.getFilterType()), is(true));
//    }
    
    @Test 
    public void testPassedFilter_TrueWhenGenePassesAndVariantsFailFilterOfThatType() {
        instance.addFilterResult(PASS_GENE_FILTER_RESULT);
        variantEvaluation1.addFilterResult(FAIL_GENE_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(PASS_GENE_FILTER_RESULT.getFilterType()), is(true));
    }
    
    @Test 
    public void testPassedFilter_TrueWhenGeneUnfilteredAndVariantPassesFilterOfThatType() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
    }
    
    @Test 
    public void testPassedFilter_FalseWhenGeneUnfilteredAndVariantsFailFilterOfThatType() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilter(FAIL_VARIANT_FILTER_RESULT.getFilterType()), is(false));
    }
    
    @Test 
    public void testPassedFilter_TrueWhenGeneUnfilteredAndAtLeastOneVariantPassesFilterOfThatType() {
        variantEvaluation1.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        variantEvaluation2.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);
        assertThat(instance.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
    }

    @Test
    public void testPassesFilters_TrueWhenVariantPassesFilter() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFilters_TrueWhenAtLeastOneVariantPassesFilter() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation1);

        variantEvaluation2.addFilterResult(FAIL_VARIANT_FILTER_RESULT);
        instance.addVariant(variantEvaluation2);

        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFilters_FalseWhenVariantFailsFilter() {
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

        List<VariantEvaluation> passedVariantEvaluations = Arrays.asList(variantEvaluation1);

        assertThat(instance.getPassedVariantEvaluations(), equalTo(passedVariantEvaluations));
    }

    @Test
    public void testAddVariant_AfterGeneIsFilteredAppliesPassGeneFilterResultsToVariant() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addFilterResult(FilterResult.pass(FilterType.PRIORITY_SCORE_FILTER));
        
        instance.addVariant(variantEvaluation1);
        
        assertThat(variantEvaluation1.passedFilters(), is(true));
        assertThat(variantEvaluation1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));
    }
    
    @Test
    public void testAddVariant_AfterGeneIsFilteredAppliesFailGeneFilterResultsToVariant() {
        variantEvaluation1.addFilterResult(PASS_VARIANT_FILTER_RESULT);
        instance.addFilterResult(FilterResult.fail(FilterType.PRIORITY_SCORE_FILTER));
        
        instance.addVariant(variantEvaluation1);
        
        assertThat(variantEvaluation1.passedFilters(), is(false));
        assertThat(variantEvaluation1.passedFilter(PASS_VARIANT_FILTER_RESULT.getFilterType()), is(true));
        assertThat(variantEvaluation1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
    }
    
    @Test
    public void testAddVariant_AfterGeneIsFilteredDoesNotApplyInheritanceFilterResultToVariant() {
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
    public void canInheritanceModes() {
        assertThat(instance.getInheritanceModes(), notNullValue());
        assertThat(instance.getInheritanceModes().isEmpty(), is(true));
    }

    @Test
    public void canSetAndGetInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_DOMINANT);

        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.getInheritanceModes(), equalTo(inheritanceModes));
    }

    @Test
    public void isConsistentWithInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE,
                ModeOfInheritance.X_RECESSIVE);

        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWithDominant(), is(true));
        assertThat(instance.isCompatibleWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(true));
    }

    @Test
    public void isConsistentWithDominantInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWithDominant(), is(true));
        assertThat(instance.isCompatibleWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(false));
    }

    @Test
    public void isConsistentWithRecessiveInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWithDominant(), is(false));
        assertThat(instance.isCompatibleWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(false));
    }

    @Test
    public void isConsistentWithXRecessiveInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.X_RECESSIVE);

        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWithDominant(), is(false));
        assertThat(instance.isCompatibleWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(true));
    }

    @Test
    public void testIsCompatibleWithX_falseWhenVariantsIsEmpty() {
        instance = newGeneOne();
        assertThat(instance.isXChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithX_falseWhenVariantIsNotCompatibleWithX() {
        instance = newGeneOne();
        instance.addVariant(new VariantEvaluation.Builder(1, 1, "A", "T").build());
        assertThat(instance.isXChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithX_trueWhenVariantIsCompatibleWithX() {
        int X_CHROMOSOME = 23;
        instance = newGeneOne();
        instance.addVariant(new VariantEvaluation.Builder(X_CHROMOSOME, 1, "A", "T").build());
        assertThat(instance.isXChromosomal(), is(true));
    }

    @Test
    public void testIsCompatibleWithY_falseWhenVariantsIsEmpty() {
        instance = newGeneOne();
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithY_falseWhenVariantIsNotCompatibleWithX() {
        instance = newGeneOne();
        instance.addVariant(new VariantEvaluation.Builder(1, 1, "A", "T").build());
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsCompatibleWithY_trueWhenVariantIsCompatibleWithX() {
        int Y_CHROMOSOME = 24;
        instance = newGeneOne();
        instance.addVariant(new VariantEvaluation.Builder(Y_CHROMOSOME, 1, "A", "T").build());
        assertThat(instance.isYChromosomal(), is(true));
    }

    @Test
    public void testCanSetAndChangePriorityScore() {
        float firstScore = 0f;
        instance.setPriorityScore(firstScore);
        assertThat(instance.getPriorityScore(), equalTo(firstScore));

        float secondScore = 1.0f;
        instance.setPriorityScore(secondScore);
        assertThat(instance.getPriorityScore(), equalTo(secondScore));
    }

    @Test
    public void testCanSetAndChangeFilterScore() {
        float firstScore = 0f;
        instance.setFilterScore(firstScore);
        assertThat(instance.getFilterScore(), equalTo(firstScore));

        float secondScore = 1.0f;
        instance.setFilterScore(secondScore);
        assertThat(instance.getFilterScore(), equalTo(secondScore));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }
}
