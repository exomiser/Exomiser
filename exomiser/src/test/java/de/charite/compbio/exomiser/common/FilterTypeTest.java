/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterTypeTest {
    
    public FilterTypeTest() {
    }

    /**
     * Test of valueOf method, of class FilterType.
     */
    @Test
    public void testValueOf() {
        String name = "PHENODIGM_MGI_PRIORITY";
        FilterType expResult = FilterType.PHENODIGM_MGI_PRIORITY;
        FilterType result = FilterType.valueOf(name);
        assertEquals(expResult, result);

    }

    /**
     * Test of getCommandLineValue method, of class FilterType.
     */
    @Test
    public void testGetCommandLineValue() {
        FilterType instance = FilterType.BED_FILTER;
        String expResult = "bed";
        String result = instance.getCommandLineValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOfCommandLine method, of class FilterType.
     */
    @Test
    public void testValueOfCommandLineUnrecognisedDefault() {
        String value = "pweep!";
        FilterType expResult = FilterType.PHENODIGM_MGI_PRIORITY;
        FilterType result = FilterType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }
 
    
    /**
     * Test of valueOfCommandLine method, of class FilterType.
     */
    @Test
    public void testValueOfCommandLine() {
        String value = "boqa";
        FilterType expResult = FilterType.BOQA_PRIORITY;
        FilterType result = FilterType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }

}
