/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.frequency;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jdk.nashorn.internal.objects.NativeArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frequency data for the variant from the Thousand Genomes and the Exome Server
 * Project.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyData {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyData.class);
    
    private final RsId rsId;

    private final EnumMap<FrequencySource, Frequency> knownFrequencies;

    public FrequencyData(RsId rsId, Frequency... frequency) {
        this(rsId, new HashSet<>(Arrays.asList(frequency)));
    } 

    public FrequencyData(RsId rsId, Set<Frequency> frequencies) {
        this.rsId = rsId;
        knownFrequencies = new EnumMap(FrequencySource.class);
        for (Frequency frequency : frequencies) {
            knownFrequencies.put(frequency.getSource(), frequency);
        }
    }

    //TODO: RSID ought to belong to the Variant, not the frequencyData 
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
    public boolean representedInDatabase() {
        if (rsId != null) {
            return true;
        }
        return !knownFrequencies.isEmpty();

    }

    public boolean hasDbSnpData() {
        return knownFrequencies.containsKey(THOUSAND_GENOMES);
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
                case EXAC_FINISH:
                case EXAC_NON_FINISH_EUROPEAN:
                case EXAC_OTHER:
                case EXAC_SOUTH_ASIAN:
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of Frequency objects. If there is no known frequency data
     * then an empty list will be returned.
     *
     * @return a List of Frequency data
     */
    public List<Frequency> getKnownFrequencies() {
        return new ArrayList(knownFrequencies.values());
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
        if (!Objects.equals(this.knownFrequencies, other.knownFrequencies)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FrequencyData{" + "rsId=" + rsId + ", knownFrequencies=" + knownFrequencies + '}';
    }
    
}
