/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filters.FilterResult;
import static de.charite.compbio.exomiser.core.filters.FilterResultStatus.FAIL;
import static de.charite.compbio.exomiser.core.filters.FilterResultStatus.PASS;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.QualityFilterResult;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for non-bean (i.e. logic-containing) methods in
 * {@code VariantEvaluation} class
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantEvaluationTest {

    private VariantEvaluation instance;
    
    private static final Integer QUALITY = 2;
    private static final Integer READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;
    private static final String GENE1_GENE_SYMBOL = "GENE1";
    private static final int GENE1_ENTREZ_GENE_ID = 1234567;

    private static final String GENE2_GENE_SYMBOL = "GENE2";
    private static final int GENE2_ENTREZ_GENE_ID = 7654321;

    private static final FilterResult FAIL_FREQUENCY_RESULT = new FrequencyFilterResult(0.1f, FAIL);
    private static final FilterResult PASS_FREQUENCY_RESULT = new FrequencyFilterResult(0.1f, PASS);

    private static final FilterResult PASS_QUALITY_RESULT = new QualityFilterResult(0.45f, PASS);

    @Mock
    private Variant variant;

    @Mock
    private Variant variantInTwoGeneRegions;

    @Mock
    private Variant variantWithNullGeneSymbol;

    @Mock
    private Variant unAnnotatedVariant;

    @Before
    public void setUp() {
        // build HTSJDK genotype call to return
        GenotypeBuilder gtBuilder = new GenotypeBuilder();
        gtBuilder.alleles(Arrays.asList(Allele.create("C", true), Allele.create("T", false)));

        final GenomePosition gPos = new GenomePosition(HG19RefDictBuilder.build(), Strand.FWD, 1, 1, PositionType.ONE_BASED);
        final GenomeChange change = new GenomeChange(gPos, "C", "T");
        
        Mockito.when(variant.getGeneSymbol()).thenReturn(GENE1_GENE_SYMBOL);
        Mockito.when(variant.getEntrezGeneID()).thenReturn(GENE1_ENTREZ_GENE_ID);
        Mockito.when(variant.getChromosome()).thenReturn(1);
        Mockito.when(variant.getPosition()).thenReturn(1);
        Mockito.when(variant.getRef()).thenReturn("A");
        Mockito.when(variant.getAlt()).thenReturn("T");
        Mockito.when(variant.getGenotype()).thenReturn(gtBuilder.make());
//        Mockito.when(variant.getGenomeChange()).thenReturn(change);
        Mockito.when(variant.getPhredScore()).thenReturn(2.2);
        Mockito.when(variant.getReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(variant.getAnnotations()).thenReturn(Arrays.asList("variant annotations..."));

        Mockito.when(variantInTwoGeneRegions.getGeneSymbol()).thenReturn(GENE2_GENE_SYMBOL + "," + GENE1_GENE_SYMBOL);
        Mockito.when(variantInTwoGeneRegions.getEntrezGeneID()).thenReturn(GENE2_ENTREZ_GENE_ID);
        Mockito.when(variantInTwoGeneRegions.getChromosome()).thenReturn(1);
        Mockito.when(variantInTwoGeneRegions.getPosition()).thenReturn(1);
        Mockito.when(variantInTwoGeneRegions.getRef()).thenReturn("C");
        Mockito.when(variantInTwoGeneRegions.getAlt()).thenReturn("G");
        Mockito.when(variantInTwoGeneRegions.getGenotype()).thenReturn(gtBuilder.make());
//        Mockito.when(variantInTwoGeneRegions.getGenomeChange()).thenReturn(change);
        Mockito.when(variantInTwoGeneRegions.getPhredScore()).thenReturn(2.2);
        Mockito.when(variantInTwoGeneRegions.getReadDepth()).thenReturn(READ_DEPTH);

        Mockito.when(variantWithNullGeneSymbol.getGeneSymbol()).thenReturn(null);

        // This is hard-coding Jannovar's return values be aware this could change
        Mockito.when(unAnnotatedVariant.getAnnotations()).thenReturn(Arrays.<String> asList());

        instance = new VariantEvaluation(variant);
    }

    @Test
    public void testGetRef() {
        assertThat(instance.getRef(), equalTo("A"));
    }

    @Test
    public void testGetAlt() {
        assertThat(instance.getAlt(), equalTo("T"));

    }

    @Test
    public void testGetVariantReadDepth() {
        assertThat(instance.getReadDepth(), equalTo(READ_DEPTH));
    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo(GENE1_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsOnlyFirstGeneSymbol() {
        instance = new VariantEvaluation(variantInTwoGeneRegions);
        assertThat(instance.getGeneSymbol(), equalTo(GENE2_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsADotIfVariantReturnsANull() {
        instance = new VariantEvaluation(variantWithNullGeneSymbol);
        assertThat(instance.getGeneSymbol(), equalTo("."));
    }

    @Test
    public void canGetEntrezGeneID() {
        assertThat(instance.getEntrezGeneID(), equalTo(GENE1_ENTREZ_GENE_ID));
    }

    @Test
    public void testThatTheConstructorDoesNotSetAFrequencyDataObject() {
        FrequencyData frequencyData = instance.getFrequencyData();
        assertThat(frequencyData, nullValue());
    }

    @Test
    public void testThatTheConstructorCreatesAnEmptyPathogenicityDataObject() {
        PathogenicityData pathogenicityData = instance.getPathogenicityData();
        assertThat(pathogenicityData, notNullValue());
        assertThat(pathogenicityData.getMutationTasterScore(), nullValue());
        assertThat(pathogenicityData.getPolyPhenScore(), nullValue());
        assertThat(pathogenicityData.getSiftScore(), nullValue());
        assertThat(pathogenicityData.getCaddScore(), nullValue());
        assertThat(pathogenicityData.hasPredictedScore(), is(false));
    }

    @Test
    public void testThatAddingAFilterResultUpdatesVariantScore() {

        assertThat(instance.getVariantScore(), equalTo(1.0f));

        // adding a FilterResult also updates the score of the VariantEvaluation
        instance.addFilterResult(PASS_QUALITY_RESULT);

        assertThat(instance.getFilterResults().size(), equalTo(1));
        assertThat(instance.getVariantScore(), equalTo(PASS_QUALITY_RESULT.getScore()));
    }

    @Test
    public void testThatAddingTwoPassFilterResultsUpdatesVariantScore() {

        assertThat(instance.getVariantScore(), equalTo(1.0f));

        float expectedScore = instance.getVariantScore();

        expectedScore *= PASS_QUALITY_RESULT.getScore();
        expectedScore *= PASS_FREQUENCY_RESULT.getScore();
        // adding a FilterResult also updates the score of the VariantEvaluation
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(PASS_FREQUENCY_RESULT);

        assertThat(instance.getFilterResults().size(), equalTo(2));
        assertThat(instance.getVariantScore(), equalTo(expectedScore));
    }

    @Test
    public void testThatAddingPassAndFailFilterResultsUpdatesVariantScore() {

        assertThat(instance.getVariantScore(), equalTo(1.0f));

        float expectedScore = instance.getVariantScore();

        expectedScore *= PASS_QUALITY_RESULT.getScore();
        expectedScore *= FAIL_FREQUENCY_RESULT.getScore();
        // adding a FilterResult also updates the score of the VariantEvaluation
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        // assertThat(instance.getFilterResults().size(), equalTo(2));
        assertThat(instance.getVariantScore(), equalTo(expectedScore));
    }

    @Test
    public void testGetFailedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void testGetFailedFilterTypesDontContainPassedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        instance.addFilterResult(PASS_QUALITY_RESULT);

        assertThat(instance.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void testFailsFiltersWhenFailedFilterResultAdded() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testVariantScoreIsUpdatedWhenFailedFilterResultAdded() {

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.getVariantScore(), equalTo(FAIL_FREQUENCY_RESULT.getScore()));
    }

    @Test
    public void testVariantScoreIsUpdatedWhenPassedAndFailedFilterResultAdded() {
        float qualScore = PASS_QUALITY_RESULT.getScore();
        // adding a FilterResult also updates the score of the VariantEvaluation
        instance.addFilterResult(PASS_QUALITY_RESULT);
        assertThat(instance.getVariantScore(), equalTo(qualScore));

        float freqScore = 0.1f;
        FilterResult frequencyScore = new FrequencyFilterResult(freqScore, FAIL);
        // adding a failed FilterResult also updates the score of the VariantEvaluation
        instance.addFilterResult(frequencyScore);

        assertThat(instance.getVariantScore(), equalTo(qualScore * freqScore));
    }

    @Test
    public void testPassesFiltersWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getFilterResults().isEmpty(), is(true));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFiltersWhenOnlyPassedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(PASS_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getFilterResults().isEmpty(), is(false));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testFailsFiltersWhenPassedAndFailedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(false));
        assertThat(instance.getFilterResults().isEmpty(), is(false));
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testFilterStatusWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedAndPassed() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.PASSED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedAndFailed() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedWithPassAndFailedResults() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void testPassesFilterIsTrueWhenPassedFilterResultAdded() {
        FilterType passedFilterType = PASS_QUALITY_RESULT.getFilterType();

        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.passedFilter(passedFilterType), is(true));
    }

    @Test
    public void testPassesFilterIsFalseWhenFailedFilterResultAdded() {
        FilterType filterType = FAIL_FREQUENCY_RESULT.getFilterType();

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.passedFilter(filterType), is(false));
    }

    @Test
    public void testGetFilterResultOfFailedFilterIsNull() {
        FilterType filterType = FAIL_FREQUENCY_RESULT.getFilterType();

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.getFilterResult(filterType), nullValue());
    }

    @Test
    public void testGetFilterResultOfPassedFilter() {
        FilterType filterType = PASS_FREQUENCY_RESULT.getFilterType();

        instance.addFilterResult(PASS_FREQUENCY_RESULT);

        assertThat(instance.getFilterResult(filterType), equalTo(PASS_FREQUENCY_RESULT));
    }

    @Test
    public void testHasAnnotationsIsFalse() {
        VariantEvaluation varEvalWithNoVariantAnnotations = new VariantEvaluation(unAnnotatedVariant);
        assertThat(varEvalWithNoVariantAnnotations.hasAnnotations(), is(false));
    }

    @Test
    public void testHasAnnotationsIsTrue() {
        assertThat(instance.hasAnnotations(), is(true));
    }
}
