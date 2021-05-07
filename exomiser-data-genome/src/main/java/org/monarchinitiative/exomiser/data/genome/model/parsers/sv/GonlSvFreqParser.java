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
public class GonlSvFreqParser implements Parser<SvFrequency> {

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
        int svLen = parseSvLen(info.get("SVLEN"), info.get("MEICLASS"));
        int end = parseEnd(start, svLen, info.get("END"));

        VariantType variantType = parseVariantType(fields[4], info.get("SVTYPE"), info.get("MEICLASS"));

        String id = fields[2];
        int ac = parseIntField(info, "AC");

        int an = parseIntField(info, "AN");

        String dbVarId = info.getOrDefault("DGV_ID", "");
        return List.of(new SvFrequency(chr, start, end, svLen, variantType, dbVarId, "GONL", id.equals(".") ? "" : id, ac, an));
    }

    private int parseSvLen(String svlen, String meiClass) {
        if (svlen != null) {
            return Integer.parseInt(svlen);
        }
        if (meiClass != null) {
            return parseMeiClassLength(meiClass);
        }
        return 1;
    }

    private int parseMeiClassLength(String meiClass) {
        switch (meiClass) {
            case "ALU":
                return 300;
            case "SVA":
                return 2000;
            case "L1":
                return 6000;
            case "HERV":
                return 9500;
            default:
                return 1;
        }
    }

    private VariantType parseVariantType(String alt, String svTypeValue, String meiClass) {
        if (!VariantType.isSymbolic(alt)) {
            return VariantType.parseType(svTypeValue);
        }
        VariantType variantType = VariantType.parseType(alt);
        if (variantType == VariantType.INS_ME) {
            return parseMeiClassType(meiClass);
        }
        return variantType;
    }

    private VariantType parseMeiClassType(String meiClass) {
        switch (meiClass) {
            case "ALU":
                return VariantType.INS_ME_ALU;
            case "SVA":
                return VariantType.INS_ME_SVA;
            case "L1":
                return VariantType.INS_ME_LINE1;
            case "HERV":
                return VariantType.INS_ME_HERV;
            default:
                return VariantType.INS_ME;
        }
    }

    private int parseIntField(Map<String, String> info, String key) {
        String acStr = info.get(key);
        return acStr == null ? 0 : Integer.parseInt(acStr);
    }

    private int parseEnd(int start, int svLen, String end) {
        if (end == null) {
            // for insertions where the end is not known create a region of 1
            return svLen > 0 ? start : start + Math.abs(svLen);
        }
        return Integer.parseInt(end);
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
