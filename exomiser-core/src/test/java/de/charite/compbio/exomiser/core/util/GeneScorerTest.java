/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.FrequencyFilterScore;
import de.charite.compbio.exomiser.core.filter.PathogenicityFilterScore;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.priority.PriorityScore;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.ScoringMode;
import jannovar.common.Genotype;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import jannovar.pedigree.Pedigree;
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
        failedFrequency.addFailedFilter(FilterType.FREQUENCY_FILTER, new FrequencyFilterScore(0f));
        
        failedPathogenicity = getNewTestVariantEvaluation();
        failedPathogenicity.addFailedFilter(FilterType.PATHOGENICITY_FILTER, new PathogenicityFilterScore(0f));
        
        failedFrequencyPassedPathogenicity = getNewTestVariantEvaluation();
        failedFrequencyPassedPathogenicity.addFailedFilter(FilterType.FREQUENCY_FILTER, new FrequencyFilterScore(0f));
        failedFrequencyPassedPathogenicity.addPassedFilter(FilterType.PATHOGENICITY_FILTER, new PathogenicityFilterScore(1f));
        //these are set up so that failedFrequencyPassedPathogenicity has a higher 
        //pathogenicity score (1.0)than passedFrequencyPassedPathogenicity (0.75) to ensure that the scoring only includes variants
        //which have actually passed all the filters
        passedFrequencyPassedPathogenicity = getNewTestVariantEvaluation();
        passedFrequencyPassedPathogenicity.addPassedFilter(FilterType.FREQUENCY_FILTER, new FrequencyFilterScore(0.75f));
        passedFrequencyPassedPathogenicity.addPassedFilter(FilterType.PATHOGENICITY_FILTER, new PathogenicityFilterScore(0.75f));
    }
    
    private VariantEvaluation getNewTestVariantEvaluation() {
        //these are just a bunch of numbers required to get a stubbed VariantEvaluation
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
        
        float bestScore = passedFrequencyPassedPathogenicity.getFilterScore();
        
        float calculatedScore = GeneScorer.calculateNonAutosomalRecessiveFilterScore(variantEvaluations);
        
        assertThat(calculatedScore, equalTo(bestScore));
    }
    
    @Test
    public void testThatCalculateFilterScoreReturnsZeroWithAnEmptyVariantEvaluationList() {
        
        List<VariantEvaluation> emptyVariantEvaluations = new ArrayList<>();
                
        float calculatedScore = GeneScorer.calculateFilterScore(emptyVariantEvaluations, ModeOfInheritance.UNINITIALIZED, null);

        assertThat(calculatedScore, equalTo(0f));
    } 
    
    @Test
    public void testThatCalculateFilterScoreReturnsScoreWithAPassedVariantEvaluationInList() {
        
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        variantEvaluations.add(failedFrequency);
        variantEvaluations.add(passedFrequencyPassedPathogenicity);
                
        float bestScore = passedFrequencyPassedPathogenicity.getFilterScore();
        
        float calculatedScore = GeneScorer.calculateFilterScore(variantEvaluations, ModeOfInheritance.UNINITIALIZED, null);

        assertThat(calculatedScore, equalTo(bestScore));
    } 

//    @Test
//    public void testCalculatePriorityScore() {
//        Collection<PriorityScore> priorityScores = null;
//        float expResult = 0.0F;
//        float result = GeneScorer.calculatePriorityScore(priorityScores);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCalculateAutosomalRecessiveFilterScore() {
//        System.out.println("calculateAutosomalRecessiveFilterScore");
//        List<VariantEvaluation> variantEvaluations = null;
//        Pedigree pedigree = null;
//        float expResult = 0.0F;
//        float result = GeneScorer.calculateAutosomalRecessiveFilterScore(variantEvaluations, pedigree);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//
//    @Test
//    public void testCalculateCombinedScore() {
//        System.out.println("calculateCombinedScore");
//        float filterScore = 0.0F;
//        float priorityScore = 0.0F;
//        Set<PriorityType> prioritiesRun = null;
//        float expResult = 0.0F;
//        float result = GeneScorer.calculateCombinedScore(filterScore, priorityScore, prioritiesRun);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}