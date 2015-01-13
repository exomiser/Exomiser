/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 * CADD info - see {@link http://cadd.gs.washington.edu/info}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CaddScore extends AbstractPathogenicityScore {

    public CaddScore(float score) {
        super(score);
    }

    @Override
    public String toString() {
        return "CADD: " + score;
    }
    
    
}
