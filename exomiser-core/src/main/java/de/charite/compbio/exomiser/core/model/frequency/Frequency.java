/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.frequency;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Frequency {

    private final float frequency;
    private final FrequencySource source;
    
    public Frequency(float frequency) {
        this.frequency = frequency;
        this.source = FrequencySource.UNKNOWN;
    }

    public Frequency(float frequency, FrequencySource source) {
        this.frequency = frequency;
        this.source = source;
    }

    public float getFrequency() {
        return frequency;
    }

    public FrequencySource getSource() {
        return source;
    }
    
    public boolean isOverThreshold(float threshold) {
        return frequency > threshold;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Float.floatToIntBits(this.frequency);
        hash = 67 * hash + Objects.hashCode(this.source);
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
        if (this.source != other.source) {
            return false;
        }
        return Float.floatToIntBits(this.frequency) == Float.floatToIntBits(other.frequency);
    }

    @Override
    public String toString() {
        return "Frequency{" + frequency + " source=" + source + '}';
    }
    
}
