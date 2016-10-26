/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
        VariantEffect variantEffect = VariantEffect.MISSENSE_VARIANT;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect);
        assertThat(result, equalTo(VariantTypePathogenicityScores.DEFAULT_MISSENSE_SCORE));
    }
    
    @Test
    public void testGetPathogenicityScoreForStartLoss() {
        VariantEffect variantEffect = VariantEffect.START_LOST;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect);
        assertThat(result, equalTo(VariantTypePathogenicityScores.STARTLOSS_SCORE));
    }
    
    @Test
    public void testGetPathogenicityScoreForNonPathogenicVariantType() {
        VariantEffect variantEffect = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        float result = VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect);
        assertThat(result, equalTo(VariantTypePathogenicityScores.NON_PATHOGENIC_SCORE));
    }
    
}
