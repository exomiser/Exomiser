/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.FrequencyFilterScore;
import de.charite.compbio.exomiser.core.filter.QualityFilterScore;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import jannovar.common.Genotype;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import java.util.EnumSet;
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

    VariantEvaluation instance;
    private static final Integer QUALITY = 2;
    private static final Integer READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;

    public VariantEvaluationTest() {

    }

    @Before
    public void setUp() {
        GenotypeCall genotypeCall = new GenotypeCall(HETEROZYGOUS, QUALITY, READ_DEPTH);
        byte chr = 1;
        Variant variant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f, "");
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
        assertThat(instance.getVariantReadDepth(), equalTo(READ_DEPTH));
    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo("."));
    }

    @Test
    public void testGetVariantStartPosition() {
        assertThat(instance.getVariantStartPosition(), equalTo(1));
    }

    @Test
    public void testGetVariantEndPosition() {
        assertThat(instance.getVariantEndPosition(), equalTo(1));
    }

    @Test
    public void testThatTheConstructorCreatesAnEmptyFrequencyDataObject() {
        FrequencyData frequencyData = instance.getFrequencyData();
        assertThat(frequencyData, notNullValue());
        assertThat(frequencyData.getDbSnpMaf(), nullValue());
        assertThat(frequencyData.getEspAaMaf(), nullValue());
        assertThat(frequencyData.getEspAllMaf(), nullValue());
        assertThat(frequencyData.getEspEaMaf(), nullValue());
        assertThat(frequencyData.getRsId(), nullValue());
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
    public void testThatAddingAFilterScoreUpdatesVariantEvaluationFilterScore() {

        assertThat(instance.getFilterScore(), equalTo(1.0f));

        float qScore = 0.45f;
        FilterScore qualityScore = new QualityFilterScore(qScore);
        //adding a FilterScore also updates the score of the VariantEvaluation 
        instance.addPassedFilter(FilterType.QUALITY_FILTER, qualityScore);
        assertThat(instance.getFilterScoreMap().size(), equalTo(1));
        assertThat(instance.getFilterScore(), equalTo(qScore));
    }

    @Test
    public void testThatAddingTwoFilterScoresUpdatesVariantEvaluationFilterScore() {

        assertThat(instance.getFilterScore(), equalTo(1.0f));

        float expectedScore = instance.getFilterScore();

        float qScore = 0.45f;
        FilterScore qualityScore = new QualityFilterScore(qScore);
        expectedScore *= qScore;
        //adding a FilterScore also updates the score of the VariantEvaluation 
        instance.addPassedFilter(FilterType.QUALITY_FILTER, qualityScore);

        float fScore = 0.2f;
        FilterScore frequencyScore = new FrequencyFilterScore(fScore);
        expectedScore *= fScore;
        //adding a FilterScore also updates the score of the VariantEvaluation 
        instance.addPassedFilter(FilterType.FREQUENCY_FILTER, frequencyScore);
        assertThat(instance.getFilterScoreMap().size(), equalTo(2));
        assertThat(instance.getFilterScore(), equalTo(expectedScore));
    }

    @Test
    public void testGetFilterScore() {
        float qScore = 0.45f;
        FilterScore qualityScore = new QualityFilterScore(qScore);
        //adding a FilterScore also updates the score of the VariantEvaluation 
        instance.addPassedFilter(FilterType.QUALITY_FILTER, qualityScore);
        assertThat(instance.getFilterScore(), equalTo(qScore));
    }

    @Test
    public void testGetFailedFilters() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterScore frequencyScore = new FrequencyFilterScore(0.1f);

        Set<FilterType> expectedFilters = EnumSet.of(filterType);
        
        instance.addFailedFilter(filterType, frequencyScore);
        assertThat(instance.getFailedFilters(), equalTo(expectedFilters));
    }

    @Test
    public void testFailsFiltersWhenFailedFilterAdded() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterScore frequencyScore = new FrequencyFilterScore(0.1f);
        
        instance.addFailedFilter(filterType, frequencyScore);
        assertThat(instance.passesFilters(), is(false));
    }

    @Test
    public void testVariantFilterScoreIsUpdatedWhenFailedFilterAdded() {
        float score = 0.1f;
        
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterScore frequencyScore = new FrequencyFilterScore(score);
        
        instance.addFailedFilter(filterType, frequencyScore);
        assertThat(instance.getFilterScore(), equalTo(score));
    }
    
    @Test
    public void testVariantFilterScoreIsUpdatedWhenPassedAndFailedFilterAdded() {
        float qualScore = 0.45f;
        FilterScore qualityScore = new QualityFilterScore(qualScore);
        //adding a FilterScore also updates the score of the VariantEvaluation 
        instance.addPassedFilter(FilterType.QUALITY_FILTER, qualityScore);
        assertThat(instance.getFilterScore(), equalTo(qualScore));
        
        float freqScore = 0.1f;
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterScore frequencyScore = new FrequencyFilterScore(freqScore);
        //adding a failed FilterScore also updates the score of the VariantEvaluation         
        instance.addFailedFilter(filterType, frequencyScore);
        
        assertThat(instance.getFilterScore(), equalTo(qualScore * freqScore));
    } 
    
    @Test
    public void testPassesFilters() {
        assertThat(instance.passesFilters(), is(true));
    }

    @Test
    public void testPassesFilterIsTrue() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterType passedFilterType = FilterType.QUALITY_FILTER;
        
        instance.addPassedFilter(passedFilterType, new QualityFilterScore(3.0f));
        instance.addFailedFilter(filterType, new FrequencyFilterScore(0.1f));
        
        assertThat(instance.passedFilter(passedFilterType), is(true));
    }

    @Test
    public void testPassesFilterIsFalse() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;
        FilterScore frequencyScore = new FrequencyFilterScore(0.1f);

        instance.addFailedFilter(filterType, frequencyScore);
        
        assertThat(instance.passedFilter(filterType), is(false));
    }

}
