/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PolyPhenScoreTest {
    
    private PolyPhenScore instance;
    private final float pathogenicScore = 1f;
    private final float nonPathogenicScore = 0f;
    
    @Before
    public void setUp() {
        instance = new PolyPhenScore(pathogenicScore);
    }

    @Test
    public void testGetSource() {
        assertThat(instance.getSource(), equalTo(PathogenicitySource.POLYPHEN));
    }
    
    @Test
    public void testCompareTo_Before() {
        PolyPhenScore nonPathogenicPolyphen = new PolyPhenScore(nonPathogenicScore);
        assertThat(instance.compareTo(nonPathogenicPolyphen), equalTo(-1));
    }
    
    @Test
    public void testCompareTo_After() {
        PolyPhenScore nonPathogenicPolyphen = new PolyPhenScore(nonPathogenicScore);
        assertThat(nonPathogenicPolyphen.compareTo(instance), equalTo(1));
    }
    
    @Test
    public void testCompareTo_Equals() {
        PolyPhenScore sameScore = new PolyPhenScore(pathogenicScore);        
        assertThat(instance.compareTo(sameScore), equalTo(0));
    }
}
