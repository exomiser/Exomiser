/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.frequency;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Frequency {

    private final float frequency;

    public Frequency(float frequency) {
        this.frequency = frequency;
    }

    public float getFrequency() {
        return frequency;
    }

    public boolean isOverThreshold(float threshold) {
        return frequency > threshold;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Float.floatToIntBits(this.frequency);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Frequency other = (Frequency) obj;
        return Float.floatToIntBits(this.frequency) == Float.floatToIntBits(other.frequency);
    }

    
    @Override
    public String toString() {
        return Float.toString(frequency);
    }    
}
