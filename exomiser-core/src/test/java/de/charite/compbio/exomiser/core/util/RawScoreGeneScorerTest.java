/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.PassFilterResult;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RawScoreGeneScorerTest {

    private RawScoreGeneScorer instance;
    
    private VariantEvaluation failedFrequency;
    private VariantEvaluation failedPathogenicity;
    private VariantEvaluation failedFrequencyPassedPathogenicity;
    private VariantEvaluation passedFrequencyPassedPathogenicity;

    @Before
    public void setUp() {
        instance = new RawScoreGeneScorer();
        
        failedFrequency = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        failedFrequency.addFilterResult(new FailFilterResult(FilterType.FREQUENCY_FILTER));

        failedPathogenicity = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        failedPathogenicity.addFilterResult(new FailFilterResult(FilterType.PATHOGENICITY_FILTER));

        failedFrequencyPassedPathogenicity = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        failedFrequencyPassedPathogenicity.addFilterResult(new FailFilterResult(FilterType.FREQUENCY_FILTER));
        failedFrequencyPassedPathogenicity.addFilterResult(new PassFilterResult(FilterType.PATHOGENICITY_FILTER));
        
        // Scoring should only includes variants which have actually passed all the filters
        passedFrequencyPassedPathogenicity = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        passedFrequencyPassedPathogenicity.addFilterResult(new PassFilterResult(FilterType.FREQUENCY_FILTER));
        passedFrequencyPassedPathogenicity.addFilterResult(new PassFilterResult(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsZeroIfVariantListIsEmpty() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        float calculatedScore = instance.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsZeroIfAllVariantsInVariantListHaveFailedFiltering() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(failedPathogenicity);
        variantEvaluations.add(failedFrequencyPassedPathogenicity);

        float calculatedScore = instance.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsHighestFilterScore() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(failedFrequencyPassedPathogenicity);
        variantEvaluations.add(passedFrequencyPassedPathogenicity);

        float bestScore = passedFrequencyPassedPathogenicity.getVariantScore();

        float calculatedScore = instance.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(bestScore));
    }

    @Test
    public void testThatCalculateFilterScoreReturnsZeroWithAnEmptyVariantEvaluationList() {

        List<VariantEvaluation> emptyVariantEvaluations = new ArrayList<>();

        float calculatedScore = instance.calculateFilterScore(emptyVariantEvaluations, ModeOfInheritance.UNINITIALIZED);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testThatCalculateFilterScoreReturnsScoreWithAPassedVariantEvaluationInList() {

        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(passedFrequencyPassedPathogenicity);

        float bestScore = passedFrequencyPassedPathogenicity.getVariantScore();

        float calculatedScore = instance.calculateFilterScore(variantEvaluations, ModeOfInheritance.UNINITIALIZED);

        assertThat(calculatedScore, equalTo(bestScore));
    }

    // FIXME(holtgrew): the following were already commented out
    // @Test
    // public void testCalculatePriorityScore() {
    // Collection<PriorityScore> priorityScores = null;
    // float expResult = 0.0F;
    // float result = GeneScorer.calculatePriorityScore(priorityScores);
    // assertEquals(expResult, result, 0.0);
    // // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
    // }
    //
    // @Test
    // public void testCalculateAutosomalRecessiveFilterScore() {
    // System.out.println("calculateAutosomalRecessiveFilterScore");
    // List<VariantEvaluation> variantEvaluations = null;
    // Pedigree pedigree = null;
    // float expResult = 0.0F;
    // float result = GeneScorer.calculateAutosomalRecessiveFilterScore(variantEvaluations, pedigree);
    // assertEquals(expResult, result, 0.0);
    // // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
    // }
    //
    //
    // @Test
    // public void testCalculateCombinedScore() {
    // System.out.println("calculateCombinedScore");
    // float filterScore = 0.0F;
    // float priorityScore = 0.0F;
    // Set<PriorityType> prioritiesRun = null;
    // float expResult = 0.0F;
    // float result = GeneScorer.calculateCombinedScore(filterScore, priorityScore, prioritiesRun);
    // assertEquals(expResult, result, 0.0);
    // // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
    // }
}
