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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GnomadSvVcfFreqParser implements Parser<SvFrequency> {

    @Override
    public List<SvFrequency> parseLine(String line) {
        if (line.startsWith("#")) {
            return List.of();
        }
        String[] fields = line.trim().split("\t");
        if (fields.length != 8 || !fields[6].equals("PASS")) {
            // incorrect number of fields / non-pass record
            return List.of();
        }

        Map<String, String> info = parseInfo(fields[7]);

        int chr = Contigs.parseId(fields[0]);
        int start = Integer.parseInt(fields[1]);
        int end = Integer.parseInt(info.get("END"));

        VariantType variantType = VariantType.parseType(fields[4]);

        int svLen = parseSvLen(variantType, info.get("SVLEN"));

        String gnomadId = fields[2];
        String acStr = info.get("AC");
        int ac = acStr == null ? 0 : Integer.parseInt(acStr);

        String anStr = info.get("AN");
        int an = anStr == null ? 0 : Integer.parseInt(anStr);

        return List.of(new SvFrequency(chr, start, end, svLen, variantType, "", "GNOMAD-SV", gnomadId, ac, an));
    }

    private int parseSvLen(VariantType variantType, String svlen) {
        int svLen = Integer.parseInt(svlen);
        return variantType.baseType() == VariantType.DEL ? -svLen : svLen;
    }

    private Map<String, String> parseInfo(String field) {
        String[] entries = field.split(";");
        Map<String, String> infoMap = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            String[] kv = entries[i].split("=");
            if (kv.length == 2) {
                infoMap.put(kv[0], kv[1]);
            }
        }
        return infoMap;
    }
}
