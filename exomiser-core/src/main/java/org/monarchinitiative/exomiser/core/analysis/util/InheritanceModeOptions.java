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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;

import java.util.*;

/**
 * Immutable data class for holding the maximum minor allele frequency allowed for the {@code ModeOfInheritance} or
 * {@code SubModeOfInheritance}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class InheritanceModeOptions {

    private static final Map<SubModeOfInheritance, Float> DEFAULT_FREQ = new EnumMap<>(SubModeOfInheritance.class);
    static {
        DEFAULT_FREQ.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f);
        DEFAULT_FREQ.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);
        DEFAULT_FREQ.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, 1.0f); //presumably hom alts need to be a lot rarer

        DEFAULT_FREQ.put(SubModeOfInheritance.X_DOMINANT, 0.1f);
        DEFAULT_FREQ.put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, 2.0f);
        DEFAULT_FREQ.put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, 1.0f);

        DEFAULT_FREQ.put(SubModeOfInheritance.MITOCHONDRIAL, 0.2f);
    }

    private static final InheritanceModeOptions DEFAULT = new InheritanceModeOptions(DEFAULT_FREQ);
    private static final InheritanceModeOptions EMPTY = new InheritanceModeOptions(Collections.emptyMap());

    private final Map<SubModeOfInheritance, Float> subMoiMaxFreqs;
    private final Map<ModeOfInheritance, Float> moiMaxFreqs;

    public static InheritanceModeOptions defaults() {
        return DEFAULT;
    }

    public static InheritanceModeOptions empty() {
        return EMPTY;
    }

    public static InheritanceModeOptions of(Map<SubModeOfInheritance, Float> values) {
        Objects.requireNonNull(values);
        if (values.keySet().contains(SubModeOfInheritance.ANY)) {
            throw new IllegalArgumentException("values cannot contain key " + SubModeOfInheritance.ANY);
        }
        return new InheritanceModeOptions(values);
    }

    public static InheritanceModeOptions defaultForModes(ModeOfInheritance... modesOfInheritance) {
        Objects.requireNonNull(modesOfInheritance);
        Map<SubModeOfInheritance, Float> translated = new EnumMap<>(SubModeOfInheritance.class);
        for (ModeOfInheritance mode : modesOfInheritance) {
            switch (mode) {
                case AUTOSOMAL_DOMINANT:
                    translated.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, DEFAULT_FREQ.get(SubModeOfInheritance.AUTOSOMAL_DOMINANT));
                    break;
                case AUTOSOMAL_RECESSIVE:
                    translated.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, DEFAULT_FREQ.get(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET));
                    translated.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, DEFAULT_FREQ.get(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT));
                    break;
                case X_DOMINANT:
                    translated.put(SubModeOfInheritance.X_DOMINANT, DEFAULT_FREQ.get(SubModeOfInheritance.X_DOMINANT));
                    break;
                case X_RECESSIVE:
                    translated.put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, DEFAULT_FREQ.get(SubModeOfInheritance.X_RECESSIVE_COMP_HET));
                    translated.put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, DEFAULT_FREQ.get(SubModeOfInheritance.X_RECESSIVE_HOM_ALT));
                    break;
                case MITOCHONDRIAL:
                    translated.put(SubModeOfInheritance.MITOCHONDRIAL, DEFAULT_FREQ.get(SubModeOfInheritance.MITOCHONDRIAL));
                    break;
                case ANY:
                    throw new IllegalArgumentException(ModeOfInheritance.ANY + " is not a valid argument");
            }
        }
        return new InheritanceModeOptions(translated);
    }

    private InheritanceModeOptions(Map<SubModeOfInheritance, Float> values) {
        this.subMoiMaxFreqs = Maps.immutableEnumMap(values);
        this.subMoiMaxFreqs.forEach(InheritanceModeOptions::checkBounds);
        this.moiMaxFreqs = createInheritanceModeMaxFreqs(subMoiMaxFreqs);
    }

    private static void checkBounds(SubModeOfInheritance key, Float value) {
        try {
            assert (100f >= value && value >= 0f);
        } catch (AssertionError ex) {
            String message = key + " requires a percentage. Value " + value + " is not valid";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Transforms the {@code SubModeOfInheritance} maximum minor allele frequencies into the corresponding map for
     * {@code ModeOfInheritance}. The values will reflect the more permissive of the {@code SubModeOfInheritance} when
     * it comes to recessive modes so that during the initial, less specific assignment of alleles, the variants compatible
     * with a mode with a higher frequency cutoff are not removed.
     *
     * @param subModeMaxFreqs
     * @return a map of {@code ModeOfInheritance} and their corresponding maximum minor allele frequency.
     */
    private Map<ModeOfInheritance, Float> createInheritanceModeMaxFreqs(Map<SubModeOfInheritance, Float> subModeMaxFreqs) {
        Map<ModeOfInheritance, Float> maxFreqs = new EnumMap<>(ModeOfInheritance.class);

        for (Map.Entry<SubModeOfInheritance, Float> entry : subModeMaxFreqs.entrySet()) {
            SubModeOfInheritance subMode = entry.getKey();
            Float maxFreq = entry.getValue();
            switch (subMode) {
                case AUTOSOMAL_DOMINANT:
                    maxFreqs.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, maxFreq);
                    break;
                case AUTOSOMAL_RECESSIVE_COMP_HET:
                    maxFreqs.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, maxFreq);
                    break;
                case X_DOMINANT:
                    maxFreqs.put(ModeOfInheritance.X_DOMINANT, maxFreq);
                    break;
                case X_RECESSIVE_COMP_HET:
                    maxFreqs.put(ModeOfInheritance.X_RECESSIVE, maxFreq);
                    break;
                case MITOCHONDRIAL:
                    maxFreqs.put(ModeOfInheritance.MITOCHONDRIAL, maxFreq);
                    break;
                default:
                    //don't add the value
                    break;
            }
        }
        return Maps.immutableEnumMap(maxFreqs);
    }

    /**
     * @param modeOfInheritance The {@code ModeOfInheritance} for which a cutoff value is desired.
     * @return the maximum minor allele frequency value for this {@code ModeOfInheritance}.
     */
    public float getMaxFreqForMode(ModeOfInheritance modeOfInheritance) {
        return moiMaxFreqs.getOrDefault(modeOfInheritance, Float.MAX_VALUE);
    }

    /**
     * Returns the maximum minor allele frequency as a percentage value
     *
     * @param subModeOfInheritance The {@code SubModeOfInheritance} for which a cutoff value is desired.
     * @return the maximum minor allele frequency value for this {@code SubModeOfInheritance}.
     */
    public float getMaxFreqForSubMode(SubModeOfInheritance subModeOfInheritance) {
        return subMoiMaxFreqs.getOrDefault(subModeOfInheritance, Float.MAX_VALUE);
    }

    public Set<ModeOfInheritance> getDefinedModes() {
        return Sets.immutableEnumSet(moiMaxFreqs.keySet());
    }

    public boolean isEmpty() {
        return subMoiMaxFreqs.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InheritanceModeOptions that = (InheritanceModeOptions) o;
        return Objects.equals(subMoiMaxFreqs, that.subMoiMaxFreqs) &&
                Objects.equals(moiMaxFreqs, that.moiMaxFreqs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subMoiMaxFreqs, moiMaxFreqs);
    }

    @Override
    public String toString() {
        return "InheritanceModeMaxFrequencies" + subMoiMaxFreqs;
    }
}
