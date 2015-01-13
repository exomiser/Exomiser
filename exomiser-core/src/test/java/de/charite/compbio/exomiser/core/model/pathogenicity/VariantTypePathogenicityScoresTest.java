/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.pathogenicity;

import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import jannovar.common.VariantType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class VariantTypePathogenicityScoresTest {
    
    public VariantTypePathogenicityScoresTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testGetPathogenicityScoreForDefaultMissense() {
        VariantType variantType = VariantType.MISSENSE;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantType);
        assertThat(result, equalTo(VariantTypePathogenicityScores.DEFAULT_MISSENSE_SCORE));
    }
    
    @Test
    public void testGetPathogenicityScoreForStartLoss() {
        VariantType variantType = VariantType.START_LOSS;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantType);
        assertThat(result, equalTo(VariantTypePathogenicityScores.STARTLOSS_SCORE));
    }
    
    @Test
    public void testGetPathogenicityScoreForNonPathogenicVariantType() {
        VariantType variantType = VariantType.DOWNSTREAM;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantType);
        assertThat(result, equalTo(VariantTypePathogenicityScores.NON_PATHOGENIC_SCORE));
    }
    
}
