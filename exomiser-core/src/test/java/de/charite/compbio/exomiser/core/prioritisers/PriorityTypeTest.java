/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PriorityTypeTest {
    
    /**
     * Test of toString method, of class PriorityType.
     */
    @Test
    public void testToString() {
        PriorityType instance = PriorityType.PHIVE_PRIORITY;
        assertThat(instance.toString(), equalTo("PHIVE_PRIORITY"));
    }
    
}
