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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspHg19AlleleParser extends VcfAlleleParser {

    private static final Logger logger  = LoggerFactory.getLogger(EspHg19AlleleParser.class);

    private int errorCount = 0;

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        String[] infoFields = info.split(";");

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
        // ##INFO=<ID=AA_AC,Number=.,Type=String,Description="African American Allele Count in the order of AltAlleles,RefAllele.
        // 1       69428   rs140739101     T       G       .       PASS    EA_AC=313,6535;AA_AC=14,3808;TAC=327,10343;MAF=4.5707,0.3663,3.0647
        // 1	201383643	rs2026594	A	C	.	PASS	EA_AC=7330,1256;AA_AC=3334,1070;TAC=10664,2326;MAF=14.6285,24.2961,17.9061;
        // Y	14954404	~rs151160568	C	CT,CTT	.	PASS	EA_AC=1602,162,2;AA_AC=498,39,0;TAC=2100,201,2;MAF=9.2865,7.2626,8.8146;
        // n.b. in cases where the REF allele is actually the minor allele (i.e. where the REF AC is smaller than the ALT AC),
        // then the AF will be 100 - MAF.
        String[] fields = infoField.split("=")[1].split(",");
        int[] alleleCounts = toAlleleCounts(fields);
        int an = sumAC(alleleCounts);
        List<AlleleProto.Frequency> freqs = new ArrayList<>();
        for (int i = 0; i < alleleCounts.length - 1; i++) {
            int ac = alleleCounts[i];
            freqs.add(AlleleData.frequencyOf(frequencySource, ac, an));
        }
        return freqs;
    }

    private int sumAC(int[] alleleCounts) {
        int an = 0;
        for (int alleleCount : alleleCounts) {
            an += alleleCount;
        }
        return an;
    }

    private int[] toAlleleCounts(String[] acFields) {
        int[] alleleCounts = new int[acFields.length];
        for (int i = 0; i < acFields.length; i++) {
            alleleCounts[i] = Integer.parseInt(acFields[i]);
        }
        return alleleCounts;
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
