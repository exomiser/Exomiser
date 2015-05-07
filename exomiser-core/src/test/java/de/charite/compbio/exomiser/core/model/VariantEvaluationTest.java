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
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.jannovar.pedigree.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for non-bean (i.e. logic-containing) methods in
 * {@code VariantEvaluation} class
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEvaluationTest {

    private VariantEvaluation instance;

    private static final int CHROMOSOME = 1;
    private static final String CHROMOSOME_NAME = "1";
    private static final int POSITION = 1;
    private static final String REF = "C";
    private static final String ALT = "T";

    private static final double QUALITY = 2.2;
    private static final int READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;
    private static final String GENE1_GENE_SYMBOL = "GENE1";
    private static final int GENE1_ENTREZ_GENE_ID = 1234567;

    private static final String GENE2_GENE_SYMBOL = "GENE2";
    private static final int GENE2_ENTREZ_GENE_ID = 7654321;

    private static final FilterResult FAIL_FREQUENCY_RESULT = new FrequencyFilterResult(0.1f, FAIL);
    private static final FilterResult PASS_FREQUENCY_RESULT = new FrequencyFilterResult(1.0f, PASS);

    private static final FilterResult PASS_QUALITY_RESULT = new QualityFilterResult(0.45f, PASS);

    @Before
    public void setUp() {
        instance = new VariantEvaluation.VariantBuilder(CHROMOSOME, POSITION, REF, ALT)
                .quality(QUALITY)
                .geneSymbol(GENE1_GENE_SYMBOL)
                .geneId(GENE1_ENTREZ_GENE_ID)
                .build();
    }

    private static VariantEvaluation.VariantBuilder testVariantBuilder() {
        return new VariantEvaluation.VariantBuilder(CHROMOSOME, POSITION, ALT, REF);
    }

    @Test
    public void testgetChromosome() {
        assertThat(instance.getChromosome(), equalTo(CHROMOSOME));
    }

    @Test
    public void testgetChromosomeName() {
        assertThat(instance.getChromosomeName(), equalTo(CHROMOSOME_NAME));
    }

    @Test
    public void testGetRef() {
        assertThat(instance.getRef(), equalTo(REF));
    }

    @Test
    public void testGetAlt() {
        assertThat(instance.getAlt(), equalTo(ALT));

    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo(GENE1_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsOnlyFirstGeneSymbol() {
        instance = testVariantBuilder()
                .geneSymbol(GENE2_GENE_SYMBOL + "," + GENE1_GENE_SYMBOL)
                .build();
        assertThat(instance.getGeneSymbol(), equalTo(GENE2_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsADotIfGeneSymbolNotDefined() {
        instance = testVariantBuilder().build();
        assertThat(instance.getGeneSymbol(), equalTo("."));
    }

    @Test
    public void testGetNumIndividuals_EqualsOneIfNotSet() {
        assertThat(instance.getNumberOfIndividuals(), equalTo(1));
    }

    @Test
    public void testGetNumIndividuals_EqualsBuilderValue() {
        int builderValue = 2;
        instance = testVariantBuilder().numIndividuals(builderValue).build();
        assertThat(instance.getNumberOfIndividuals(), equalTo(builderValue));
    }

    @Test
    public void canGetEntrezGeneID() {
        assertThat(instance.getEntrezGeneId(), equalTo(GENE1_ENTREZ_GENE_ID));
    }

    @Test
    public void testThatTheConstructorDoesNotSetAFrequencyDataObject() {
        FrequencyData frequencyData = instance.getFrequencyData();
        assertThat(frequencyData, nullValue());
    }
    
    @Test
    public void testThatTheBuilderCanSetAFrequencyDataObject() {
        FrequencyData frequencyData = new FrequencyData(new RsId(12345), new Frequency(0.1f, FrequencySource.LOCAL));
        instance = testVariantBuilder().frequencyData(frequencyData).build();
        assertThat(instance.getFrequencyData(), equalTo(frequencyData));
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
    public void testThatTheBuilderCanSetAPathogenicityDataObject() {
        PathogenicityData pathData = new PathogenicityData(new PolyPhenScore(1.0f), null, null, null);
        instance = testVariantBuilder().pathogenicityData(pathData).build();
        assertThat(instance.getPathogenicityData(), equalTo(pathData));
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
    public void testHasAnnotationsIsFalseByDefault() {
        assertThat(instance.hasAnnotations(), is(false));
    }

    @Test
    public void testIsXChromosomal_notXchromosomal() {
        assertThat(instance.isXChromosomal(), is(false));
    }

    @Test
    public void testIsXChromosomal_isXchromosomal() {
        int chrX = 23;
        instance = new VariantEvaluation.VariantBuilder(chrX, 1, "A", "T").build();
        assertThat(instance.isXChromosomal(), is(true));
    }

    @Test
    public void testIsYChromosomal_notYchromosomal() {
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsYChromosomal_isYchromosomal() {
        int chrY = 24;
        instance = new VariantEvaluation.VariantBuilder(chrY, 1, "A", "T").build();
        assertThat(instance.isYChromosomal(), is(true));
    }

    @Test
    public void testIsOffExome_isFalseByDefault() {
        assertThat(instance.isOffExome(), is(false));
    }
    
    @Test
    public void testIsOffExome_EqualsBuilderValue() {
        instance = testVariantBuilder().isOffExome(true).build();
        assertThat(instance.isOffExome(), is(true));
    }
    
    @Test
    public void testGetChromosomeName_23isX() {
        instance = new VariantEvaluation.VariantBuilder(23, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("X"));
    }

    @Test
    public void testGetChromosomeName_24isY() {
        instance = new VariantEvaluation.VariantBuilder(24, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("Y"));
    }

    @Test
    public void testGetChromosomeName_25isM() {
        instance = new VariantEvaluation.VariantBuilder(25, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("M"));
    }

    @Test
    public void getVariantContext() {
        VariantContext builtContext = instance.getVariantContext();
        assertThat(builtContext.getChr(), equalTo("chr" + CHROMOSOME_NAME));
        assertThat(builtContext.getStart(), equalTo(POSITION));
        assertThat(builtContext.getEnd(), equalTo(POSITION));
        assertThat(builtContext.getNAlleles(), equalTo(2));
        assertThat(builtContext.getReference().getBaseString(), equalTo(instance.getRef()));
        assertThat(builtContext.getAlternateAllele(instance.getAltAlleleId()).getBaseString(), equalTo(instance.getAlt()));
        assertThat(builtContext.getNSamples(), equalTo(instance.getNumberOfIndividuals()));
    }

    @Test
    public void getAltAlleleId_EqualsZeroWhenNotSet() {
        assertThat(instance.getAltAlleleId(), equalTo(0));
    }

    @Test
    public void getAltAlleleId_EqualsBuilderValue() {
        int altAlleleId = 2;
        instance = testVariantBuilder().altAlleleId(altAlleleId).build();
        assertThat(instance.getAltAlleleId(), equalTo(altAlleleId));
    }

    @Test
    public void testCompareTo() {
        //variants are sorted according to score. The score is calculated from the filter results where 1 is best and 0 is worst.
        VariantEvaluation first = new VariantEvaluation.VariantBuilder(CHROMOSOME, 3, REF, ALT).build();
        first.addFilterResult(PASS_FREQUENCY_RESULT);
        VariantEvaluation last = new VariantEvaluation.VariantBuilder(CHROMOSOME, 2, "C", "T").build();
        last.addFilterResult(FAIL_FREQUENCY_RESULT);

        List<VariantEvaluation> variants = new ArrayList<>();
        variants.add(last);
        variants.add(first);
        Collections.sort(variants);

        List<VariantEvaluation> expected = new ArrayList<>();
        expected.add(first);
        expected.add(last);

        assertThat(variants, equalTo(expected));
    }

    @Test
    public void testToString() {
        String expected = "chr=1 pos=1 ref=C alt=T qual=2.2 score=1.0 failedFilterTypes=[]";
        System.out.println(instance);
        assertThat(instance.toString(), equalTo(expected));
    }
}
