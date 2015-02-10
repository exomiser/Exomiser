/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneScorerTest {
    
    private VariantEvaluation failedFrequency;
    private VariantEvaluation failedPathogenicity;
    private VariantEvaluation failedFrequencyPassedPathogenicity;
    private VariantEvaluation passedFrequencyPassedPathogenicity;
    
    public GeneScorerTest() {
    }
    
    @Before
     public void setUp() {
     failedFrequency = getNewTestVariantEvaluation();
     failedFrequency.addFilterResult(new FrequencyFilterResult(0f, FilterResultStatus.FAIL));
    
     failedPathogenicity = getNewTestVariantEvaluation();
     failedPathogenicity.addFilterResult(new PathogenicityFilterResult(0f, FilterResultStatus.FAIL));
    
     failedFrequencyPassedPathogenicity = getNewTestVariantEvaluation();
     failedFrequencyPassedPathogenicity.addFilterResult(new FrequencyFilterResult(0f, FilterResultStatus.FAIL));
     failedFrequencyPassedPathogenicity.addFilterResult(new PathogenicityFilterResult(1f, FilterResultStatus.PASS));
     //these are set up so that failedFrequencyPassedPathogenicity has a higher
     //pathogenicity score (1.0)than passedFrequencyPassedPathogenicity (0.75) to ensure that the scoring only
     includes variants
     //which have actually passed all the filters
     passedFrequencyPassedPathogenicity = getNewTestVariantEvaluation();
     passedFrequencyPassedPathogenicity.addFilterResult(new FrequencyFilterResult(0.75f, FilterResultStatus.PASS));
     passedFrequencyPassedPathogenicity.addFilterResult(new PathogenicityFilterResult(0.75f,
     FilterResultStatus.PASS));
     }

    private VariantEvaluation getNewTestVariantEvaluation() {
        // these are just a bunch of numbers required to get a stubbed VariantEvaluation
        int testQuality = 2;
        int testDepth = 7;
        GenotypeCall testGenotypeCall = new GenotypeCall(Genotype.UNINITIALIZED, testQuality, testDepth);
        byte testChr = 1;
        Variant testVariant = new Variant(testChr, 1, "A", "T", testGenotypeCall, 2.2f, "");
        return new VariantEvaluation(testVariant);
    }

    @Test
    public void testScoreGenesWithRawScoreMode() {
        List<Gene> geneList = new ArrayList<>();
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        Pedigree pedigree = null;
        ScoringMode scoringMode = ScoringMode.RAW_SCORE;
        GeneScorer.scoreGenes(geneList, modeOfInheritance, pedigree, scoringMode);

    }

    @Test
    public void testScoreGenesWithRankScoreMode() {
        List<Gene> geneList = new ArrayList<>();
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        Pedigree pedigree = null;
        ScoringMode scoringMode = ScoringMode.RANK_BASED;
        GeneScorer.scoreGenes(geneList, modeOfInheritance, pedigree, scoringMode);

    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsZeroIfVariantListIsEmpty() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        float calculatedScore = GeneScorer.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsZeroIfAllVariantsInVariantListHaveFailedFiltering() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(failedPathogenicity);
        variantEvaluations.add(failedFrequencyPassedPathogenicity);

        float calculatedScore = GeneScorer.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testCalculateNonAutosomalRecessiveFilterScoreReturnsHighestFilterScore() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(failedFrequencyPassedPathogenicity);
        variantEvaluations.add(passedFrequencyPassedPathogenicity);

        float bestScore = passedFrequencyPassedPathogenicity.getVariantScore();

        float calculatedScore = GeneScorer.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);

        assertThat(calculatedScore, equalTo(bestScore));
    }

    @Test
    public void testThatCalculateFilterScoreReturnsZeroWithAnEmptyVariantEvaluationList() {

        List<VariantEvaluation> emptyVariantEvaluations = new ArrayList<>();

        float calculatedScore = GeneScorer.calculateFilterScore(emptyVariantEvaluations,
                ModeOfInheritance.UNINITIALIZED, null);

        assertThat(calculatedScore, equalTo(0f));
    }

    @Test
    public void testThatCalculateFilterScoreReturnsScoreWithAPassedVariantEvaluationInList() {

        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(passedFrequencyPassedPathogenicity);

        float bestScore = passedFrequencyPassedPathogenicity.getVariantScore();

        float calculatedScore = GeneScorer.calculateFilterScore(variantEvaluations, ModeOfInheritance.UNINITIALIZED,
                null);

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
