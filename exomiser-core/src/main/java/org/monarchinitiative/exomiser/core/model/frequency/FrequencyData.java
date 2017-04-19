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

package org.monarchinitiative.exomiser.core.model.frequency;

import java.util.*;

/**
 * Frequency data for the variant from the Thousand Genomes, the Exome Server
 * Project and Broad ExAC datasets.
 *
 * Note that the frequency data are expressed as percentages.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyData {

    public static final FrequencyData EMPTY_DATA = new FrequencyData();

    private final RsId rsId;
    private final Map<FrequencySource, Frequency> knownFrequencies;

    private FrequencyData() {
        this.rsId = null;
        this.knownFrequencies = Collections.emptyMap();
    }

    public FrequencyData(RsId rsId, Frequency... frequency) {
        this(rsId, new HashSet<>(Arrays.asList(frequency)));
    } 

    public FrequencyData(RsId rsId, Collection<Frequency> frequencies) {
        this.rsId = rsId;
        knownFrequencies = new EnumMap<>(FrequencySource.class);
        for (Frequency frequency : frequencies) {
            knownFrequencies.put(frequency.getSource(), frequency);
        }
    }

    //RSID ought to belong to the Variant, not the frequencyData, but its here for convenience
    public RsId getRsId() {
        return rsId;
    }

    public Frequency getFrequencyForSource(FrequencySource source) {
        return knownFrequencies.get(source);
    }

    /**
     * @return true if this variant is at all represented in dbSNP or ESP data,
     * regardless of frequency. That is, if the variant has an RS id in dbSNP or
     * any frequency data at all, return true, otherwise false.
     */
    public boolean isRepresentedInDatabase() {
        if (rsId != null) {
            return true;
        }
        return !knownFrequencies.isEmpty();
    }

    public boolean hasDbSnpData() {
        return knownFrequencies.containsKey(FrequencySource.THOUSAND_GENOMES);
    }

    public boolean hasDbSnpRsID() {
        return rsId != null;
    }

    public boolean hasEspData() {
        for (FrequencySource dataSource : knownFrequencies.keySet()) {
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
    
    public boolean hasExacData() {
        for (FrequencySource dataSource : knownFrequencies.keySet()) {
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

    public boolean hasKnownFrequency() {
        return !knownFrequencies.isEmpty();
    }

    /**
     * Returns a list of Frequency objects. If there is no known frequency data
     * then an empty list will be returned.
     *
     * @return a List of Frequency data
     */
    public List<Frequency> getKnownFrequencies() {
        return new ArrayList<>(knownFrequencies.values());
    }

    /**
     * Returns a the maximum frequency - if there are no known frequencies/ no
     * frequency data it will return 0.
     *
     * @return
     */
    public float getMaxFreq() {
        //TODO this is analagous to PathogenicityData.getMostPathogenicScore()
        //TODO so should really return a Frequency object...
        float maxFreq = 0f;
        for (Frequency freq : knownFrequencies.values()) {
            //TODO ...but frequency needs to implement comparable first
            maxFreq = Math.max(maxFreq, freq.getFrequency());
        }
        return maxFreq;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.rsId);
        hash = 29 * hash + Objects.hashCode(this.knownFrequencies);
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
        final FrequencyData other = (FrequencyData) obj;
        if (!Objects.equals(this.rsId, other.rsId)) {
            return false;
        }
        return Objects.equals(this.knownFrequencies, other.knownFrequencies);
    }

    @Override
    public String toString() {
        return "FrequencyData{" + "rsId=" + rsId + ", knownFrequencies=" + knownFrequencies.values() + '}';
    }

    private static final float VERY_RARE_SCORE = 1f;
    private static final float NOT_RARE_SCORE = 0f;

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
            return 1f - (0.13533f * (float) Math.exp(max));
        }
    }

}
