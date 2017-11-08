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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface AlleleParser {

    Logger logger = LoggerFactory.getLogger(AlleleParser.class);

    List<Allele> parseLine(String line);

    default byte parseChr(String field, String line) {
        switch (field) {
            case "X":
            case "x":
                return 23;
            case "Y":
            case "y":
                return 24;
            case "M":
            case "MT":
            case "m":
                return 25;
            case ".":
                return 0;
            default:
                try {
                    return Byte.parseByte(field);
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse chromosome: '{}'. Error occurred parsing line: {}", field, line, e);
                }
        }
        return 0;
    }
}
