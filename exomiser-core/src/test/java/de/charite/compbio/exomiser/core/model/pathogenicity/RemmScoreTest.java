/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RemmScoreTest {
    
    RemmScore instance = new RemmScore(1f);
    
    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.REMM));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("REMM: 1.000"));
    }
    
}
