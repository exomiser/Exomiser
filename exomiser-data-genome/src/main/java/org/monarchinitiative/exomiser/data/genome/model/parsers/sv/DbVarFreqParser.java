/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers.sv;

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;
import org.monarchinitiative.svart.VariantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbVarFreqParser implements Parser<SvFrequency> {

    private static final Logger logger = LoggerFactory.getLogger(DbVarFreqParser.class);

    @Override
    public List<SvFrequency> parseLine(String line) {
        if (line.startsWith("#")) {
            return List.of();
        }
        String[] tokens = line.split("\t");
        int chr = Contigs.parseId(tokens[0]);
        int start = Integer.parseInt(tokens[1]);
        String id = tokens[2];
        String infoField = tokens[7];

        int end = 0;

        int svLen = 0;
        VariantType svType = VariantType.parseType(tokens[4]);

        int alleleCount = 0;
        float alleleFreq = 0f;
        int alleleNum = 0;

        for (String keyValue : infoField.split(";")) {
            String[] keyValues = keyValue.split("=");
            if (keyValues.length == 2) {
                // SVLEN=.
                // CIPOS=0,.
                // CIEND=.,0
                if ("AF".equals(keyValues[0])) {
                    alleleFreq = Float.parseFloat(keyValues[1]);
                } else if ("AN".equals(keyValues[0])) {
                    alleleNum = parseIntOrDefault(keyValues[1], 0);
                } else if ("AC".equals(keyValues[0])) {
                    alleleCount = parseIntOrDefault(keyValues[1], 0);
                } else if ("SVLEN".equals(keyValues[0])) {
                    svLen = parseIntOrDefault(keyValues[1], 0);
                } else if ("END".equals(keyValues[0])) {
                    end = Integer.parseInt(keyValues[1]);
                }
            }
        }

        if (alleleCount == 0 && alleleNum == 0) {
            return List.of();
        }
        return List.of(new SvFrequency(chr, start, end, svLen, svType, "", "DBVAR", id, alleleCount, alleleNum));
    }

    private int parseIntOrDefault(String value, int defaultInt) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultInt;
        }
    }

    private static class InfoField {

        private final Map<String, String> fields;

        private InfoField(Map<String, String> fields) {
            this.fields = fields;
        }

        public static InfoField parse(String infoField) {
            Objects.requireNonNull(infoField);
            Map<String, String> fieldMap = new HashMap<>();
            for (String keyValue : infoField.split(";")) {
                String[] keyValues = keyValue.split("=");
                if (keyValues.length == 2) {
                    fieldMap.put(keyValues[0], keyValues[1]);
                }
            }
            return new InfoField(fieldMap);
        }
    }
}
