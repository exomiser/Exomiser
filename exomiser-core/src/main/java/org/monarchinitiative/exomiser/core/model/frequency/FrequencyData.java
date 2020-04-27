/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model.frequency;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Frequency data for the variant from the Thousand Genomes, the Exome Server
 * Project and Broad ExAC datasets.
 * <p>
 * Note that the frequency data are expressed as percentages.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyData {

    private static final String VCF_EMPTY_VALUE = ".";
    private static final FrequencyData EMPTY_DATA = new FrequencyData("", Collections.emptyList());

    private static final int NUM_FREQ_SOURCES = FrequencySource.values().length;

    private static final float VERY_RARE_SCORE = 1f;
    private static final float NOT_RARE_SCORE = 0f;

    private final String rsId;

    private final int size;
    private final FrequencySource[] sources;
    private final float[] values;

    public static FrequencyData of(String rsId, Collection<Frequency> frequencies) {
        return validate(rsId, frequencies);
    }

    public static FrequencyData of(Collection<Frequency> frequencies) {
        return validate("", frequencies);
    }

    public static FrequencyData of(String rsId, Frequency frequency) {
        return of(rsId, Collections.singletonList(frequency));
    }

    public static FrequencyData of(String rsId, Frequency... frequency) {
        return of(rsId, Arrays.asList(frequency));
    }

    public static FrequencyData of(Frequency... frequency) {
        return of(Arrays.asList(frequency));
    }

    public static FrequencyData of(Frequency frequency) {
        return of(Collections.singletonList(frequency));
    }

    public static FrequencyData empty() {
        return EMPTY_DATA;
    }

    private static FrequencyData validate(String rsId, Collection<Frequency> frequencies) {
        Objects.requireNonNull(frequencies, "frequency data cannot be null");

        if (isEmptyValue(rsId) && frequencies.isEmpty()) {
            return FrequencyData.empty();
        }

        for (Frequency frequency : frequencies) {
            Objects.requireNonNull(frequency, "frequency data cannot contain null element");
        }
        return new FrequencyData(rsId == null ? "" : rsId, frequencies);
    }

    private static boolean isEmptyValue(String id) {
        return id == null || id.isEmpty() || VCF_EMPTY_VALUE.equals(id);
    }

    private FrequencyData(String rsId, Collection<Frequency> frequencies) {
        this.rsId = rsId;
        // use natural ordering by FrequencySource - this class is a kind of EnumMap, but we're using primitives to store
        // the values. This means that duplicated FrequencySource will be overwritten. In practice this shouldn't happen
        // as they are extracted from a map instance.
        Frequency[] sorted = orderByFrequencySource(frequencies);

        this.size = countNotNullFrequencies(sorted);
        this.sources = new FrequencySource[size];
        this.values = new float[size];
        // TODO add AN and AC to Frequency

        int pos = 0;
        for (Frequency entry : sorted) {
            if (entry != null) {
                sources[pos] = entry.getSource();
                values[pos] = entry.getFrequency();
                pos++;
            }
        }
    }

    private Frequency[] orderByFrequencySource(Collection<Frequency> frequencies) {
        Frequency[] sorted = new Frequency[NUM_FREQ_SOURCES];
        for (Frequency frequency : frequencies) {
            FrequencySource frequencySource = frequency.getSource();
            sorted[frequencySource.ordinal()] = frequency;
        }
        return sorted;
    }

    private int countNotNullFrequencies(Frequency[] sorted) {
        int notNull = 0;
        for (Frequency frequency : sorted) {
            if (frequency != null) {
                notNull++;
            }
        }
        return notNull;
    }

    public String getRsId() {
        return rsId;
    }

    @Nullable
    public Frequency getFrequencyForSource(FrequencySource source) {
        for (int i = 0; i < size; i++) {
            if (sources[i] == source) {
                float value = values[i];
                return Frequency.of(source, value);
            }
        }
        return null;
    }

    /**
     * @return true if this variant is at all represented in dbSNP or ESP data,
     * regardless of frequency. That is, if the variant has an RS id in dbSNP or
     * any frequency data at all, return true, otherwise false.
     */
    @JsonIgnore
    public boolean isRepresentedInDatabase() {
        return hasDbSnpRsID() || hasKnownFrequency();
    }

    @JsonIgnore
    public boolean hasDbSnpData() {
        for (FrequencySource dataSource : sources) {
            if (dataSource == FrequencySource.THOUSAND_GENOMES) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean hasDbSnpRsID() {
        return !rsId.isEmpty();
    }

    @JsonIgnore
    public boolean hasEspData() {
        for (FrequencySource dataSource : sources) {
            switch (dataSource) {
                case ESP_AFRICAN_AMERICAN:
                case ESP_EUROPEAN_AMERICAN:
                case ESP_ALL:
                    return true;
                default:
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean hasExacData() {
        for (FrequencySource dataSource : sources) {
            switch (dataSource) {
                case EXAC_AFRICAN_INC_AFRICAN_AMERICAN:
                case EXAC_AMERICAN:
                case EXAC_EAST_ASIAN:
                case EXAC_FINNISH:
                case EXAC_NON_FINNISH_EUROPEAN:
                case EXAC_OTHER:
                case EXAC_SOUTH_ASIAN:
                    return true;
                default:
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean hasKnownFrequency() {
        return size != 0;
    }

    /**
     * This function tests whether or not this {@code FrequencyData} object contains a {@code Frequency} object which has
     * a frequency greater than the maximum frequency provided. This method does not check any ranges so it is advised
     * that the user checks the frequency type in advance of calling this method. By default exomiser expresses the
     * frequencies as a <b>percentage</b> value.
     *
     * @param maxFreq the maximum frequency threshold against which the {@code Frequency} objects are tested
     * @return true if the object contains a {@code Frequency} over the provided percentage value, otherwise returns false.
     * @since 10.1.0
     */
    public boolean hasFrequencyOverPercentageValue(float maxFreq) {
        for (int i = 0; i < size; i++) {
            if (values[i] > maxFreq) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of {@code Frequency} objects. If there is no known frequency data then an empty list will be returned.
     * This method will return a mutable copy of the underlying data.
     *
     * @return a mutable copy of the {@code Frequency} data
     */
    public List<Frequency> getKnownFrequencies() {
        List<Frequency> freqs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            freqs.add(Frequency.of(sources[i], values[i]));
        }
        return freqs;
    }

    /**
     * Returns a the maximum frequency - if there are no known frequencies/ no
     * frequency data it will return 0.
     *
     * @return
     */
    @JsonIgnore
    public float getMaxFreq() {
        float max = 0f;
        for (int i = 0; i < size; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    /**
     * @return returns a numerical value that is closer to one, the rarer
     * the variant is. If a variant is not entered in any of the data
     * sources, it returns one (highest score). Otherwise, it identifies the
     * maximum MAF in any of the databases, and returns a score that depends on
     * the MAF. Note that the frequency is expressed as a percentage.
     */
    public float getScore() {

        float max = getMaxFreq();

        if (max <= 0) {
            return VERY_RARE_SCORE;
        } else if (max > 2) {
            return NOT_RARE_SCORE;
        } else {
            return 1.13533f - (0.13533f * (float) Math.exp(max));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrequencyData that = (FrequencyData) o;
        return Objects.equals(rsId, that.rsId) &&
                size == that.size &&
                Arrays.equals(sources, that.sources) &&
                Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rsId, size);
        result = 31 * result + Arrays.hashCode(sources);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        boolean first = true;

        for (int i = 0; i < size; i++) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(sources[i]).append('=').append(values[i]);
        }
        sb.append('}');
        return "FrequencyData{" + "rsId=" + rsId + ", knownFrequencies=" + sb.toString() + '}';
    }

}
