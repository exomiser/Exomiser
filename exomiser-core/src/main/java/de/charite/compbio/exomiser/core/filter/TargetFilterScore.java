/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filter;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterScore implements FilterScore {

    private final float score;
    
    public TargetFilterScore(float score) {
        this.score = score;
    }

    @Override
    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "TargetFilterScore{" + "score=" + score + '}';
    }
    
}
