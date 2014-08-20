package de.charite.compbio.exomiser.core.model;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package de.charite.compbio.exomiser.exome;
//
//import de.charite.compbio.exomiser.filter.FilterType;
//import de.charite.compbio.exomiser.priority.PriorityScore;
//import de.charite.compbio.exomiser.priority.PriorityType;
//import jannovar.common.ModeOfInheritance;
//import jannovar.pedigree.Pedigree;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
///**
// *
// * @author jj8
// */
//public class GeneTest {
//    
//    public GeneTest() {
//    }
//
//    @Test
//    public void testSetPedigree() {
//        System.out.println("setPedigree");
//        Pedigree ped = null;
//        Gene.setPedigree(ped);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testDownrankGeneIfMoreVariantsThanThreshold() {
//        System.out.println("downrankGeneIfMoreVariantsThanThreshold");
//        int threshold = 0;
//        Gene instance = null;
//        instance.downrankGeneIfMoreVariantsThanThreshold(threshold);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNumberOfVariants() {
//        System.out.println("getNumberOfVariants");
//        Gene instance = null;
//        int expResult = 0;
//        int result = instance.getNumberOfVariants();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testDownWeightGeneWithManyVariants() {
//        System.out.println("downWeightGeneWithManyVariants");
//        int threshold = 0;
//        Gene instance = null;
//        instance.downWeightGeneWithManyVariants(threshold);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNthVariant() {
//        System.out.println("getNthVariant");
//        int n = 0;
//        Gene instance = null;
//        VariantEvaluation expResult = null;
//        VariantEvaluation result = instance.getNthVariant(n);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testAddVariant() {
//        System.out.println("addVariant");
//        VariantEvaluation var = null;
//        Gene instance = null;
//        instance.addVariant(var);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testAddRelevanceScore() {
//        System.out.println("addRelevanceScore");
//        PriorityScore rel = null;
//        PriorityType type = null;
//        Gene instance = null;
//        instance.addRelevanceScore(rel, type);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetRelevanceScore() {
//        System.out.println("getPriorityScore");
//        PriorityType type = null;
//        Gene instance = null;
//        float expResult = 0.0F;
//        float result = instance.getPriorityScore(type);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetVariantList() {
//        System.out.println("getVariantList");
//        Gene instance = null;
//        List<VariantEvaluation> expResult = null;
//        List<VariantEvaluation> result = instance.getVariantList();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testResetRelevanceScore() {
//        System.out.println("resetPriorityScore");
//        PriorityType type = null;
//        float newval = 0.0F;
//        Gene instance = null;
//        instance.resetPriorityScore(type, newval);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetEntrezGeneID() {
//        System.out.println("getEntrezGeneID");
//        Gene instance = null;
//        int expResult = 0;
//        int result = instance.getEntrezGeneID();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetRelevanceMap() {
//        System.out.println("getPriorityScoreMap");
//        Gene instance = null;
//        Map<PriorityType, PriorityScore> expResult = null;
//        Map<PriorityType, PriorityScore> result = instance.getPriorityScoreMap();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetGeneSymbol() {
//        System.out.println("getGeneSymbol");
//        Gene instance = null;
//        String expResult = "";
//        String result = instance.getGeneSymbol();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCalculateFilteringScore() {
//        System.out.println("calculateFilteringScore");
//        ModeOfInheritance mode = null;
//        Gene instance = null;
//        instance.calculateFilteringScore(mode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCalculatePriorityScore() {
//        System.out.println("calculatePriorityScore");
//        Gene instance = null;
//        instance.calculatePriorityScore();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGet_ordered_variant_list() {
//        System.out.println("get_ordered_variant_list");
//        Gene instance = null;
//        List<VariantEvaluation> expResult = null;
//        List<VariantEvaluation> result = instance.get_ordered_variant_list();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testIs_consistent_with_recessive() {
//        System.out.println("is_consistent_with_recessive");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.is_consistent_with_recessive();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testIs_consistent_with_dominant() {
//        System.out.println("is_consistent_with_dominant");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.is_consistent_with_dominant();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testIs_consistent_with_X() {
//        System.out.println("is_consistent_with_X");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.is_consistent_with_X();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testIs_X_chromosomal() {
//        System.out.println("isXChromosomal");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.isXChromosomal();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testIs_Y_chromosomal() {
//        System.out.println("isYChromosomal");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.isYChromosomal();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetCombinedScore() {
//        System.out.println("getCombinedScore");
//        Gene instance = null;
//        float expResult = 0.0F;
//        float result = instance.getCombinedScore();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetPriorityScore() {
//        System.out.println("getPriorityScore");
//        Gene instance = null;
//        float expResult = 0.0F;
//        float result = instance.getPriorityScore();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testSetPriorityScore() {
//        System.out.println("setPriorityScore");
//        float score = 0.0F;
//        Gene instance = null;
//        instance.setPriorityScore(score);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetFilterScore() {
//        System.out.println("getFilterScore");
//        Gene instance = null;
//        float expResult = 0.0F;
//        float result = instance.getFilterScore();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCalculateGeneAndVariantScores() {
//        System.out.println("calculateGeneAndVariantScores");
//        ModeOfInheritance mode = null;
//        Gene instance = null;
//        instance.calculateGeneAndVariantScores(mode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCompareTo() {
//        System.out.println("compareTo");
//        Gene other = null;
//        Gene instance = null;
//        int expResult = 0;
//        int result = instance.compareTo(other);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetVariantEvaluationIterator() {
//        System.out.println("getVariantEvaluationIterator");
//        Gene instance = null;
//        Iterator<VariantEvaluation> expResult = null;
//        Iterator<VariantEvaluation> result = instance.getVariantEvaluationIterator();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testAddFailedFilter() {
//        System.out.println("addFailedFilter");
//        FilterType filterType = null;
//        Gene instance = null;
//        instance.addFailedFilter(filterType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetFailedFilters() {
//        System.out.println("getFailedFilters");
//        Gene instance = null;
//        Set<FilterType> expResult = null;
//        Set<FilterType> result = instance.getFailedFilters();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testPassesFilters() {
//        System.out.println("passesFilters");
//        Gene instance = null;
//        boolean expResult = false;
//        boolean result = instance.passesFilters();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
//}
