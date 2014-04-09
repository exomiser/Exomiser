/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MinorAlleleFrequency {
    
    private final int frequency;

    public MinorAlleleFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return "MinorAlleleFrequency{" + "frequency=" + frequency + '}';
    }    
}
