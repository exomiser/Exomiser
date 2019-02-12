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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ChromosomeParser {

    private static final Logger logger = LoggerFactory.getLogger(ChromosomeParser.class);

    private ChromosomeParser() {
        //static utility class
    }

    public static byte parseChr(String field) {
        if (field.startsWith("chr")) {
            return chr(field.substring(3));
        }
        return chr(field);
    }

    private static byte chr(String field) {
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
                    //hg38 alternate scaffolds will throw these all the time, so its on debug
                    logger.debug("Unable to parse chromosome: '{}'.", field, e);
                }
                return 0;
        }
    }
}
