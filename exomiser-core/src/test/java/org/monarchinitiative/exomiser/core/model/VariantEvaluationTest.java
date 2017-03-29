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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

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

    private static final FilterResult FAIL_FREQUENCY_RESULT = FilterResult.fail(FilterType.FREQUENCY_FILTER);
    private static final FilterResult PASS_FREQUENCY_RESULT = FilterResult.pass(FilterType.FREQUENCY_FILTER);

    private static final FilterResult PASS_QUALITY_RESULT = FilterResult.pass(FilterType.QUALITY_FILTER);


    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = SiftScore.valueOf(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = SiftScore.valueOf(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = PolyPhenScore.valueOf(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = PolyPhenScore.valueOf(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = MutationTasterScore.valueOf(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = MutationTasterScore.valueOf(MTASTER_FAIL_SCORE);

    @Before
    public void setUp() {
        instance = new VariantEvaluation.Builder(CHROMOSOME, POSITION, REF, ALT)
                .quality(QUALITY)
                .geneSymbol(GENE1_GENE_SYMBOL)
                .geneId(GENE1_ENTREZ_GENE_ID)
                .build();
    }

    private static VariantEvaluation.Builder testVariantBuilder() {
        return new VariantEvaluation.Builder(CHROMOSOME, POSITION, REF, ALT);
    }
    
    @Test
    public void testGetChromosome() {
        assertThat(instance.getChromosome(), equalTo(CHROMOSOME));
    }

    @Test
    public void testGetChromosomeName() {
        assertThat(instance.getChromosomeName(), equalTo(CHROMOSOME_NAME));
    }

    @Test
    public void testGetChromosomePosition() {
        assertThat(instance.getPosition(), equalTo(POSITION));
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
    public void testThatTheConstructorCreatesAnEmptyFrequencyDataObject() {
        FrequencyData frequencyData = instance.getFrequencyData();
        assertThat(frequencyData, equalTo(FrequencyData.EMPTY_DATA));
    }
    
    @Test
    public void testThatTheBuilderCanSetAFrequencyDataObject() {
        FrequencyData frequencyData = new FrequencyData(RsId.valueOf(12345), Frequency.valueOf(0.1f, FrequencySource.LOCAL));
        instance = testVariantBuilder().frequencyData(frequencyData).build();
        assertThat(instance.getFrequencyData(), equalTo(frequencyData));
    }

    @Test
    public void testCanSetFrequencyDataAfterConstruction() {
        FrequencyData frequencyData = new FrequencyData(RsId.valueOf(12345), Frequency.valueOf(0.1f, FrequencySource.LOCAL));
        instance.setFrequencyData(frequencyData);
        assertThat(instance.getFrequencyData(), equalTo(frequencyData));
    }

    @Test
    public void testGetFrequencyScoreNoFrequencyDataSet() {
        assertThat(instance.getFrequencyScore(), equalTo(1f));
    }
    
    @Test
    public void testThatTheConstructorCreatesAnEmptyPathogenicityDataObject() {
        PathogenicityData pathogenicityData = instance.getPathogenicityData();
        assertThat(pathogenicityData, equalTo(new PathogenicityData()));
        assertThat(pathogenicityData.getMutationTasterScore(), nullValue());
        assertThat(pathogenicityData.getPolyPhenScore(), nullValue());
        assertThat(pathogenicityData.getSiftScore(), nullValue());
        assertThat(pathogenicityData.getCaddScore(), nullValue());
        assertThat(pathogenicityData.hasPredictedScore(), is(false));
    }
    
    @Test
    public void testThatTheBuilderCanSetAPathogenicityDataObject() {
        PathogenicityData pathData = new PathogenicityData(PolyPhenScore.valueOf(1.0f));
        instance = testVariantBuilder().pathogenicityData(pathData).build();
        assertThat(instance.getPathogenicityData(), equalTo(pathData));
    }

    @Test
    public void testCanSetPathogenicityDataAfterConstruction() {
        PathogenicityData pathData = new PathogenicityData(PolyPhenScore.valueOf(1.0f));
        instance.setPathogenicityData(pathData);
        assertThat(instance.getPathogenicityData(), equalTo(pathData));
    }

    @Test
    public void testGetPathogenicityScore_UnknownVariantEffectNoPathogenicityPredictions() {
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
        assertThat(instance.getPathogenicityScore(), equalTo(0f));
    }

    @Test
    public void testGetPathogenicityScore_NonMissenseVariantNoPredictions() {
        VariantEffect type = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        instance = testVariantBuilder().variantEffect(type).build();

        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScore_NonMissenseVariantWithPredictions() {
        VariantEffect type = VariantEffect.REGULATORY_REGION_VARIANT;
        PathogenicityData pathData = new PathogenicityData(CaddScore.valueOf(1f));
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        assertThat(instance.getPathogenicityScore(), equalTo(pathData.getScore()));
    }

    @Test
    public void testGetPathogenicityScore_MissenseVariantNoPredictions() {
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().variantEffect(type).build();

        float expected = VariantTypePathogenicityScores.getPathogenicityScoreOf(type);
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScore_MissenseSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_FAIL, MTASTER_FAIL, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        float expected = 1 - SIFT_PASS.getScore();
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScore_MissensePolyPhenAndSiftPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_FAIL, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        float expected = 1 - SIFT_PASS.getScore();
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScore_MissensePolyPhenSiftAndMutTasterPass() {
        PathogenicityData pathData = new PathogenicityData(POLYPHEN_PASS, MTASTER_PASS, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        float expected = MTASTER_PASS.getScore();
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
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
    public void testBuilderFilterResultsGetFailedFilterTypesDontContainPassedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());
        VariantEvaluation variantEvaluation = testVariantBuilder()
                .filterResults(FAIL_FREQUENCY_RESULT)
                .build();

        assertThat(variantEvaluation.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void testBuilderFilterResults_AddPassAndFailedFilters() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());
        Set<FilterType> passedFilters = EnumSet.of(PASS_QUALITY_RESULT.getFilterType());

        VariantEvaluation variantEvaluation = testVariantBuilder()
                .filterResults(FAIL_FREQUENCY_RESULT, PASS_QUALITY_RESULT)
                .build();

        assertThat(variantEvaluation.getFailedFilterTypes(), equalTo(expectedFilters));
        assertThat(variantEvaluation.getPassedFilterTypes(), equalTo(passedFilters));
    }

    @Test
    public void testGetVariantScoreWithEmptyFreqAndPathData() {
        instance = testVariantBuilder()
                .frequencyData(FrequencyData.EMPTY_DATA)
                .pathogenicityData(PathogenicityData.EMPTY_DATA)
                .build();
        assertThat(instance.getVariantScore(), equalTo(0f));
    }

    @Test
    public void testVariantScoreIsIndependentOfFilterStatus() {
        instance = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .frequencyData(FrequencyData.EMPTY_DATA)
                //PolyPhen of 1 is predicted as highly pathogenic
                .pathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1f)))
                .build();
        assertThat(instance.getVariantScore(), equalTo(1f));
        assertThat(instance.passedFilters(), is(true));

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.getVariantScore(), equalTo(1f));
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassesFiltersWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(true));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testFailsFiltersWhenFailedFilterResultAdded() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassesFiltersWhenOnlyPassedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(PASS_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(false));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testFailsFiltersWhenPassedAndFailedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(false));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(false));
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
        instance = new VariantEvaluation.Builder(chrX, 1, "A", "T").build();
        assertThat(instance.isXChromosomal(), is(true));
    }

    @Test
    public void testIsYChromosomal_notYchromosomal() {
        assertThat(instance.isYChromosomal(), is(false));
    }

    @Test
    public void testIsYChromosomal_isYchromosomal() {
        int chrY = 24;
        instance = new VariantEvaluation.Builder(chrY, 1, "A", "T").build();
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
        instance = new VariantEvaluation.Builder(23, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("X"));
    }

    @Test
    public void testGetChromosomeName_24isY() {
        instance = new VariantEvaluation.Builder(24, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("Y"));
    }

    @Test
    public void testGetChromosomeName_25isM() {
        instance = new VariantEvaluation.Builder(25, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("M"));
    }

    @Test
    public void testGetGenotype_Het() {
        instance = new VariantEvaluation.Builder(25, 1, "A", "T").build();
        assertThat(instance.getGenotypeString(), equalTo("0/1"));
    }

    @Test
    public void getVariantContext() {
        VariantContext builtContext = instance.getVariantContext();
        assertThat(builtContext.getContig(), equalTo("chr" + CHROMOSOME_NAME));
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
    public void testGetVariantEffect_defaultValue() {
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
    }

    @Test
    public void testIsPredictedPathogenic_falseByDefault() {
        assertThat(instance.isPredictedPathogenic(), is(false));
    }
    @Test
    public void testIsPredictedPathogenic_missenseVariant() {
        instance = testVariantBuilder().variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        assertThat(instance.isPredictedPathogenic(), is(true));
    }

    @Test
    public void testStopGainVariantIsPredictedPathogenicIsTrue() {
        instance = testVariantBuilder().variantEffect(VariantEffect.STOP_GAINED).build();
        assertThat(instance.isPredictedPathogenic(), is(true));
    }

    @Test
    public void testDownstreamVariantIsPredictedPathogenicIsFalse() {
        instance = testVariantBuilder().variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT).build();
        assertThat(instance.isPredictedPathogenic(), is(false));
    }

    @Test
    public void testDoesNotContributeToGeneScoreByDefault() {
        instance = testVariantBuilder().build();
        assertThat(instance.contributesToGeneScore(), is(false));
    }

    @Test
    public void testCanSetAsContributingToGeneScore() {
        instance = testVariantBuilder().build();
        instance.setAsContributingToGeneScore();
        assertThat(instance.contributesToGeneScore(), is(true));
    }

    @Test
    public void testCompatibleInheritanceModes() {
        Set<ModeOfInheritance> compatibleModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        instance.setInheritanceModes(compatibleModes);
        assertThat(instance.getInheritanceModes(), equalTo(compatibleModes));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(false));
    }
    
    @Test
    public void testCompareTo() {
        //variants are sorted according to chromosome, position  ref and alt.
        VariantEvaluation zero = new VariantEvaluation.Builder(1, 1, "A", "C").build();
        VariantEvaluation one = new VariantEvaluation.Builder(1, 2, "A", "G").build();
        VariantEvaluation two = new VariantEvaluation.Builder(1, 2, "AC", "G").build();
        VariantEvaluation three = new VariantEvaluation.Builder(2, 1, "C", "T").build();
        VariantEvaluation four = new VariantEvaluation.Builder(2, 1, "C", "TT").build();

        List<VariantEvaluation> variants = new ArrayList<>();
        variants.add(zero);
        variants.add(one);
        variants.add(two);
        variants.add(three);
        variants.add(four);
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("chr: %2d pos: %2d ref: %-2s alt: %-2s%n", variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        Collections.sort(variants);

        List<VariantEvaluation> expected = new ArrayList<>();
        expected.add(zero);
        expected.add(one);
        expected.add(two);
        expected.add(three);
        expected.add(four);

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("chr: %2d pos: %2d ref: %-2s alt: %-2s%n", variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(expected));
    }

    private List<VariantEvaluation> scoredVariantsInDescendingRankOrder() {
        VariantEvaluation zero = new VariantEvaluation.Builder(2, 1, "C", "TT")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .pathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1.0f)))
                .build();
        zero.setAsContributingToGeneScore();
        VariantEvaluation one = new VariantEvaluation.Builder(2, 1, "C", "T")
                .variantEffect(VariantEffect.STOP_GAINED)
                .frequencyData(new FrequencyData(null, Frequency.valueOf(0.02f, FrequencySource.ESP_ALL)))
                .pathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1.0f)))
                .build();
        one.setAsContributingToGeneScore();
        VariantEvaluation two = new VariantEvaluation.Builder(1, 2, "A", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        VariantEvaluation three = new VariantEvaluation.Builder(1, 2, "AC", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        VariantEvaluation four = new VariantEvaluation.Builder(1, 1, "A", "C")
                .variantEffect(VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT)
                .build();

        List<VariantEvaluation> variants = new ArrayList<>();
        variants.add(zero);
        variants.add(one);
        variants.add(two);
        variants.add(three);
        variants.add(four);
        return variants;
    }

    @Test
    public void testVariantRankComparator() {
        //variants are sorted according to whether they are contributing to the gene score, variant score, position  ref and alt.
        List<VariantEvaluation> variants = scoredVariantsInDescendingRankOrder();
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        variants.sort(new VariantEvaluation.RankBasedComparator());

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(scoredVariantsInDescendingRankOrder()));
    }

    @Test
    public void testVariantCompareByRank() {
        //variants are sorted according to whether they are contributing to the gene score, variant score, position  ref and alt.
        List<VariantEvaluation> variants = scoredVariantsInDescendingRankOrder();
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        variants.sort(VariantEvaluation::compareByRank);

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(scoredVariantsInDescendingRankOrder()));
    }

    @Test
    public void testToString() {
        String expected = "VariantEvaluation{chr=1 pos=1 ref=C alt=T qual=2.2 SEQUENCE_VARIANT score=0.0 UNFILTERED failedFilters=[] passedFilters=[] compatibleWith=[]}";
        System.out.println(instance);
        assertThat(instance.toString(), equalTo(expected));
    }

    @Test
    public void testToStringVariant_ContributesToGeneScore() {
        String expected = "VariantEvaluation{chr=1 pos=1 ref=C alt=T qual=2.2 SEQUENCE_VARIANT * score=0.0 UNFILTERED failedFilters=[] passedFilters=[] compatibleWith=[]}";
        instance.setAsContributingToGeneScore();
        System.out.println(instance);
        assertThat(instance.toString(), equalTo(expected));
    }
}
