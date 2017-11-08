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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExomiserAlleleParser extends VcfAlleleParser {

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<AlleleProperty, Float> values = parseInfoLine(info.trim());
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            allele.getValues().putAll(values);
        }
        return alleles;
    }

    private Map<AlleleProperty, Float> parseInfoLine(String info) {
        Map<AlleleProperty, Float> values = new EnumMap<>(AlleleProperty.class);
        if (".".equals(info)) {
            return values;
        }
        String[] fields = info.split(";");
        for (int i = 0; i < fields.length; i++) {
            String[] fieldValues = fields[i].split("=");
            values.put(AlleleProperty.valueOf(fieldValues[0]), Float.parseFloat(fieldValues[1]));
        }
        return values;
    }

}
