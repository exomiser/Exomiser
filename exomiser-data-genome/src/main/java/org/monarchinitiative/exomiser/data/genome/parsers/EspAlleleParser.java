/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.parsers;

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleParser extends VcfAlleleParser {

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<AlleleProperty, Float> minorAlleleFrequencies = parseMinorAlleleFrequencies(info);

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            allele.getValues().putAll(minorAlleleFrequencies);
        }
        return alleles;
    }

    private Map<AlleleProperty, Float> parseMinorAlleleFrequencies(String info) {
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            if (infoField.startsWith("MAF=")) {
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
