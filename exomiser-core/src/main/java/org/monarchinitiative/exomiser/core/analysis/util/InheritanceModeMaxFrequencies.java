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
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable data class for holding the maximum minor allele frequency allowed for the {@code ModeOfInheritance} or
 * {@code SubModeOfInheritance}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class InheritanceModeMaxFrequencies {

    private static final InheritanceModeMaxFrequencies DEFAULT = defaultMap();

    private final Map<SubModeOfInheritance, Float> subMoiMaxFreqs;
    private final Map<ModeOfInheritance, Float> moiMaxFreqs;

    private static InheritanceModeMaxFrequencies defaultMap() {
        Map<SubModeOfInheritance, Float> maxFreqs = new EnumMap<>(SubModeOfInheritance.class);

        maxFreqs.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f);
        maxFreqs.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);
        maxFreqs.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, 2.0f); //presumably hom alts need to be a lot rarer

        maxFreqs.put(SubModeOfInheritance.X_DOMINANT, 0.1f);
        maxFreqs.put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, 2.0f);
        maxFreqs.put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, 2.0f);

        maxFreqs.put(SubModeOfInheritance.MITOCHONDRIAL, 0.2f);

        return new InheritanceModeMaxFrequencies(maxFreqs);
    }

    public static InheritanceModeMaxFrequencies defaultValues() {
        return DEFAULT;
    }

    public static InheritanceModeMaxFrequencies of(Map<SubModeOfInheritance, Float> values) {
        Objects.requireNonNull(values);
        return new InheritanceModeMaxFrequencies(values);
    }

    private InheritanceModeMaxFrequencies(Map<SubModeOfInheritance, Float> values) {
        this.subMoiMaxFreqs = Maps.immutableEnumMap(values);
        this.subMoiMaxFreqs.forEach(InheritanceModeMaxFrequencies::checkBounds);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InheritanceModeMaxFrequencies that = (InheritanceModeMaxFrequencies) o;
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
