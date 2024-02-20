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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Frequency {

    private final FrequencySource source;
    private final float value;
    private final int ac;
    private final int an;
    private final int homs;

    public static Frequency of(FrequencySource source, float value) {
        return new Frequency(source, value, 0, 0, 0);
    }

    public static Frequency of(FrequencySource source, int ac, int an, int homs) {
        if (an <= 0 || ac > an || homs > ac) {
            throw new IllegalArgumentException(source + " AN must be > 0, AC must be < AN and HOM must be < AC. Got AC=" + ac + ", AN=" + an + ", hom=" + homs);
        }
        return new Frequency(source, percentageFrequency(ac, an), ac, an, homs);
    }

    // package private to work with FrequencyData *ONLY* where allele frequency has already been calculated and input
    // values have already been validated
    static Frequency of(FrequencySource source, float af, int ac, int an, int homs) {
        return new Frequency(source, af, ac, an, homs);
    }

    private Frequency(FrequencySource source, float value, int ac, int an, int homs) {
        this.source = source;
        this.value = value;
        this.ac = ac;
        this.an = an;
        this.homs = homs;
    }

    @JsonProperty
    public FrequencySource source() {
        return source;
    }

    @JsonProperty
    public float frequency() {
        return value;
    }

    @JsonProperty
    public int ac() {
        return ac;
    }

    @JsonProperty
    public int an() {
        return an;
    }

    @JsonProperty
    public int homs() {
        return homs;
    }

    public boolean isOverThreshold(float threshold) {
        return value > threshold;
    }

    /**
     * Returns the frequency of ac/an as a percentage frequency.
     *
     * @param ac    Allele Count - the number of observed alleles.
     * @param an    Allele Number - size of the population in which the AC was observed.
     * @return the frequency of ac/an as a percentage frequency in the range 0..100
     */
    public static float percentageFrequency(int ac, int an) {
        return 100f * (ac / (float) an);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frequency frequency = (Frequency) o;
        return Float.compare(value, frequency.value) == 0 && ac == frequency.ac && an == frequency.an && homs == frequency.homs && source == frequency.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, value, ac, an, homs);
    }


    @Override
    public String toString() {
        return "Frequency{" + source +
               '=' + value + (an > 0 ? formatAcAn() : "") +
               '}';
    }

    private String formatAcAn() {
        return "(" + ac + '|' + an + '|' + homs + ')';
    }
}
