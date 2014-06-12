/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.filter;

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
        String name = "FREQUENCY_FILTER";
        FilterType expResult = FilterType.FREQUENCY_FILTER;
        FilterType result = FilterType.valueOf(name);
        assertEquals(expResult, result);

    }

    /**
     * Test of getCommandLineValue method, of class FilterType.
     */
    @Test
    public void testGetCommandLineValue() {
        FilterType instance = FilterType.FREQUENCY_FILTER;
        String expResult = "max-freq";
        String result = instance.getCommandLineValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOfCommandLine method, of class FilterType.
     */
    @Test
    public void testValueOfCommandLineUnrecognisedDefault() {
        String value = "pweep!";
        FilterType expResult = FilterType.FREQUENCY_FILTER;
        FilterType result = FilterType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }
 
    
    /**
     * Test of valueOfCommandLine method, of class FilterType.
     */
    @Test
    public void testValueOfCommandLine() {
        String value = "restrict-interval";
        FilterType expResult = FilterType.INTERVAL_FILTER;
        FilterType result = FilterType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }

}
