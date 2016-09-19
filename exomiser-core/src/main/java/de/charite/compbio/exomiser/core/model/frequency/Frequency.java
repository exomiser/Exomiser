/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
public final class Frequency {

    private final float frequency;
    private final FrequencySource source;

    public static Frequency valueOf(float frequency, FrequencySource source) {
        return new Frequency(frequency, source);
    }

    private Frequency(float frequency, FrequencySource source) {
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
        return Objects.hash(frequency, source);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Frequency)) {
            return false;
        }
        Frequency frequency1 = (Frequency) o;
        if (source != frequency1.source) {
            return false;
        }
        return Float.compare(frequency1.frequency, frequency) == 0;
    }

    @Override
    public String toString() {
        return "Frequency{" + frequency + " source=" + source + '}';
    }
    
}
