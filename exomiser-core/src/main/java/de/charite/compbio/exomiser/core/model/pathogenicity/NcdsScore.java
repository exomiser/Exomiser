/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 * NCDS info - see {@link ...}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NcdsScore extends BasePathogenicityScore {
        
    public NcdsScore(float score) {
        super(score, PathogenicitySource.NCDS);
    }

    @Override
    public String toString() {
        return String.format("NCDS: %.3f", score);
    }
    
}
