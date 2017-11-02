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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.Variant;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantSerialiser {

    private VariantSerialiser() {
        //static utility class - not instantiable
    }

    public static String generateKey(Variant variant) {
        StringJoiner stringJoiner = new StringJoiner("-");
        //the exomiser tabix files use the integer representation of the chromosome, so don't use the chromosome name
        stringJoiner.add(String.valueOf(variant.getChromosome()));
        stringJoiner.add(String.valueOf(variant.getPosition()));
        stringJoiner.add(variant.getRef());
        stringJoiner.add(variant.getAlt());
        return stringJoiner.toString();
    }


    public static Map<String, String> infoFieldToMap(String infoField) {
        String[] infoFieldTokens = infoField.split(";");
        Map<String, String> values = new HashMap<>();
        for (String field : infoFieldTokens) {
            String[] keyValue = field.split("=");
            if (keyValue.length == 2) {
                values.put(keyValue[0], keyValue[1]);
            }
        }
        return values;
    }
}
