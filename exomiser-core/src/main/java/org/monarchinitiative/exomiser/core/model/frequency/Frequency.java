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
 * Value class representing an allele frequency for a given {@link FrequencySource}. The static factory methods support
 * creation of either a frequency (as a percent) only value or as a gnomAD style frequency with AN, AC and Hom counts.
 * The latter case will calculate the allele frequency from the AN and AC values.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record Frequency(FrequencySource source, float frequency, int ac, int an, int homs) {

    public Frequency {
        Objects.requireNonNull(source, "source cannot be null");
        if (an < 0 || ac > an || homs > ac) {
            throw new IllegalArgumentException(source + " AN must be >= 0, AC must be < AN and HOM must be < AC. Got AC=" + ac + ", AN=" + an + ", hom=" + homs);
        }
        // allow a frequency with no an, ac or homs as these are not available for some frequency sources
        float expectedFreq = percentageFrequency(ac, an);
        if (frequency > 0f && an > 0 && Float.compare(frequency, expectedFreq) != 0) {
            throw new IllegalArgumentException(source + " frequency does not match given AC/AN! Expected " + ac + "/" + an + " = " + expectedFreq + "%, got " + frequency + "%");
        }
    }

    public static Frequency of(FrequencySource source, float freq) {
        return new Frequency(source, freq, 0, 0, 0);
    }

    public static Frequency of(FrequencySource source, int ac, int an, int homs) {
        return new Frequency(source, percentageFrequency(ac, an), ac, an, homs);
    }

    // package private to work with FrequencyData *ONLY* where allele frequency has already been calculated and input
    // values have already been validated
    static Frequency of(FrequencySource source, float af, int ac, int an, int homs) {
        return new Frequency(source, af, ac, an, homs);
    }

    public boolean isOverThreshold(float threshold) {
        return frequency > threshold;
    }

    /**
     * Returns the frequency of ac/an as a percentage frequency.
     *
     * @param ac Allele Count - the number of observed alleles.
     * @param an Allele Number - size of the population in which the AC was observed.
     * @return the frequency of ac/an as a percentage frequency in the range 0..100
     */
    public static float percentageFrequency(int ac, int an) {
        return (ac == an && an == 0) ? 0 : 100f * (ac / (float) an);
    }


    @Override
    public String toString() {
        return "Frequency{" + source +
               '=' + frequency + (an > 0 ? formatAcAn() : "") +
               '}';
    }

    private String formatAcAn() {
        return "(" + ac + '|' + an + '|' + homs + ')';
    }
}
