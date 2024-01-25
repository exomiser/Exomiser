/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspHg19AlleleParser extends VcfAlleleParser {

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        String[] infoFields = info.split(";");

        Map<AlleleProperty, Float> minorAlleleFrequencies = parseMinorAlleleFrequencies(infoFields);
        // ##INFO=<ID=EA_AC,Number=.,Type=String,Description="European American Allele Count in the order of AltAlleles,RefAllele. For INDELs, A1, A2, or An refers to the N-th alternate allele while R refers to the reference allele.">
        // ##INFO=<ID=AA_AC,Number=.,Type=String,Description="African American Allele Count in the order of AltAlleles,RefAllele. For INDELs, A1, A2, or An refers to the N-th alternate allele while R refers to the reference allele.">
        // ##INFO=<ID=TAC,Number=.,Type=String,Description="Total Allele Count in the order of AltAlleles,RefAllele For INDELs, A1, A2, or An refers to the N-th alternate allele while R refers to the reference allele.">
        List<AlleleProto.Frequency> eaFreqs = parseAlleleFrequencies("EA_AC", infoFields);
        List<AlleleProto.Frequency> aaFreqs = parseAlleleFrequencies("AA_AC", infoFields);
        List<AlleleProto.Frequency> allFreqs = parseAlleleFrequencies("TAC", infoFields);
        // EA_AC=313,6535;AA_AC=14,3808;TAC=327,10343;MAF=4.5707,0.3663,3.0647;
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            if (eaFreqs.get(i).getAc() != 0) {
                allele.addFrequency(eaFreqs.get(i));
            }
            if (aaFreqs.get(i).getAc() != 0) {
                allele.addFrequency(aaFreqs.get(i));
            }
            if (allFreqs.get(i).getAc() != 0) {
                allele.addFrequency(allFreqs.get(i));
            }
        }
        return alleles;
    }

    private List<AlleleProto.Frequency> parseAlleleFrequencies(String key, String[] infoFields) {
        for (String infoField : infoFields) {
            if (infoField.startsWith(key)) {
                // ##INFO=<ID=AA_AC,Number=.,Type=String,Description="African American Allele Count in the order of AltAlleles,RefAllele.
                return parseAlleleFreqField(freqSource(key), infoField);
            }
        }
        return List.of();
    }

    private AlleleProto.FrequencySource freqSource(String key) {
        return switch (key) {
            case "EA_AC" -> AlleleProto.FrequencySource.ESP_EA;
            case "AA_AC" -> AlleleProto.FrequencySource.ESP_AA;
            case "TAC" -> AlleleProto.FrequencySource.ESP_ALL;
            default -> throw new IllegalArgumentException(key + ": is not a legal ESP INFO key");
        };
    }

    private List<AlleleProto.Frequency> parseAlleleFreqField(AlleleProto.FrequencySource frequencySource, String infoField) {
        String[] fields = infoField.split("=")[1].split(",");
        int an = Integer.parseInt(fields[fields.length - 1]);
        List<AlleleProto.Frequency> freqs = new ArrayList<>();
        for (int i = 0; i < fields.length - 1; i++) {
            int ac = Integer.parseInt(fields[i]);
            freqs.add(AlleleData.frequencyOf(frequencySource, ac, an));
        }
        return freqs;
    }

    public Map<AlleleProperty, Float> parseMinorAlleleFrequencies(String[] infoFields) {
        for (String infoField : infoFields) {
            if (infoField.startsWith("MAF=")) {
               // ##INFO=<ID=MAF,Number=.,Type=String,Description="Minor Allele Frequency in percent in the order of EA,AA,All">
                return parseMafField(infoField);
            }
        }
        return Collections.emptyMap();
    }

    private Map<AlleleProperty, Float> parseMafField(String infoField) {
        Map<AlleleProperty, Float> frequencies = new EnumMap<>(AlleleProperty.class);
        //MAF=44.9781,47.7489,45.9213
        String[] minorAlleleFreqs = infoField.substring(4).split(",");
        for (MAF_FIELD field : MAF_FIELD.values()) {
            String freq = minorAlleleFreqs[field.ordinal()];
            if (!"0.0".equals(freq)) {
                frequencies.put(AlleleProperty.valueOf(field.name()), Float.parseFloat(freq));
            }
        }
        return frequencies;
    }

    private enum MAF_FIELD {
        //##INFO=<ID=MAF,Number=.,Type=String,Description="Minor Allele Frequency in percent in the order of EA,AA,All">
        ESP_EA, ESP_AA, ESP_ALL;
    }
}
