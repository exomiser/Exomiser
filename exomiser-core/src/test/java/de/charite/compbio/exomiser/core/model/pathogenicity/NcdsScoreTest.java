/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NcdsScoreTest {
    
    NcdsScore instance = new NcdsScore(1f);
    
    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.NCDS));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("NCDS: 1.000"));
    }
    
}
