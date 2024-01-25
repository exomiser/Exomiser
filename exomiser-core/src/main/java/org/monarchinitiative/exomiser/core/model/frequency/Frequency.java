/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model.frequency;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Frequency {

    private final FrequencySource source;
    private final float value;

    public static Frequency of(FrequencySource source, float value) {
        return new Frequency(source, value);
    }

    private Frequency(FrequencySource source, float value) {
        this.source = source;
        this.value = value;
    }

    public float getFrequency() {
        return value;
    }

    public FrequencySource getSource() {
        return source;
    }
    
    public boolean isOverThreshold(float threshold) {
        return value > threshold;
    }

    /**
     * Returns the frequency of ac/an as a percentage value.
     *
     * @param ac    Allele Count - the number of observed alleles.
     * @param an    Allele Number - size of the population in which the AC was observed.
     * @return the frequency of ac/an as a percentage value in the range 0..100
     */
    public static float percentageFrequency(int ac, int an) {
        return 100f * (ac / (float) an);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Frequency)) {
            return false;
        }
        Frequency other = (Frequency) o;
        if (source != other.source) {
            return false;
        }
        return Float.compare(other.value, value) == 0;
    }

    @Override
    public String toString() {
        return "Frequency{" + source + "=" + value + '}';
    }

}
