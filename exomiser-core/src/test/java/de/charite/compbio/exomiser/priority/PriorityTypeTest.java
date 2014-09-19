/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.priority;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PriorityTypeTest {
    
    public PriorityTypeTest() {
    }

    /**
     * Test of valueOfCommandLine method, of class PriorityType.
     */
    @Test
    public void testValueOfCommandLine() {
        String value = "inheritance-mode";
        PriorityType expResult = PriorityType.INHERITANCE_MODE_PRIORITY;
        PriorityType result = PriorityType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOfCommandLine method, of class PriorityType.
     */
    @Test
    public void testValueOfCommandLineUnrecognisedDefault() {
        String value = "wibble";
        PriorityType expResult = PriorityType.NOT_SET;
        PriorityType result = PriorityType.valueOfCommandLine(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class PriorityType.
     */
    @Test
    public void testToString() {
        PriorityType instance = PriorityType.EXOMISER_MOUSE_PRIORITY;
        String expResult = "exomiser-mouse";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
