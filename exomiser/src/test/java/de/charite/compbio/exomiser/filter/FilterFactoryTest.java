///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package de.charite.compbio.exomiser.filter;
//
//import de.charite.compbio.exomiser.util.ExomiserSettings;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import static org.junit.Assert.*;
//import org.junit.Test;
//
///**
// * Tests for FilterFactory
// * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
// */
//public class FilterFactoryTest {
//    
//    public FilterFactoryTest() {
//    }
//
//    @Test
//    public void testMakeFilters() {
//        System.out.println("makeFilters");
//        //make a new Settings object specifying a PathogenicityFilter and a FrequencyFilter
//        ExomiserSettings settings = new ExomiserSettings.Builder().includePathogenic(true).maximumFrequency(0.25f).build();
//        FilterFactory instance = new FilterFactory();
//        List<Filter> expResult = new ArrayList<>();
//        
//        List<Filter> result = instance.makeFilters(settings);
//        assertEquals(expResult, result);
//    }
//
//    @Test
//    public void testGetTargetFilter() {
//        System.out.println("getTargetFilter");
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getTargetFilter();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetFrequencyFilter() {
//        System.out.println("getFrequencyFilter");
//        float maxFrequency = 0.0F;
//        boolean filterOutAllDbsnp = false;
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getFrequencyFilter(maxFrequency, filterOutAllDbsnp);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetQualityFilter() {
//        System.out.println("getQualityFilter");
//        float quality_threshold = 0;
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getQualityFilter(quality_threshold);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetPathogenicityFilter() {
//        System.out.println("getPathogenicityFilter");
//        boolean filterOutNonpathogenic = false;
//        boolean removeSynonomousVariants = false;
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getPathogenicityFilter(filterOutNonpathogenic, removeSynonomousVariants);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetLinkageFilter() {
//        System.out.println("getLinkageFilter");
//        String interval = "";
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getIntervalFilter(interval);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetBedFilter() {
//        System.out.println("getBedFilter");
//        Set<String> commalist = null;
//        FilterFactory instance = new FilterFactory();
//        Filter expResult = null;
//        Filter result = instance.getBedFilter(commalist);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
//}
