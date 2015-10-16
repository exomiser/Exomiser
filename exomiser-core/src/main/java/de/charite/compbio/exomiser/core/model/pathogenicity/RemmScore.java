/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 * REMM info - see {@link ...}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RemmScore extends BasePathogenicityScore {
        
    public RemmScore(float score) {
        super(score, PathogenicitySource.REMM);
    }

    @Override
    public String toString() {
        return String.format("REMM: %.3f", score);
    }
    
}
