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

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class RsIdParser {

    private RsIdParser() {
        //uninstantiable static utility class
    }

    /**
     * rsIds can be merged - these are reported in the format rs200118651;rs202059104 where the first rsId is the current one,
     * the second is the rsId which was merged into the first.
     *
     * @param rsField the rs field from a VCF
     * @return The input rsId if present or '' if empty. In cases with multiple rsId separated by a ';' the first will
     * be returned. The VCF empty field character '.' will be returned as an empty string - ''
     */
    public static String parseRsId(String rsField) {
        String[] rsIds = rsField.split(";");
        if (rsIds.length >= 1) {
            String rsId = rsIds[0];
            if (rsId.equals(".")) {
                return "";
            }
            // One resource likes to occasionally add surprise '~' characters to their rsId
            if (rsId.startsWith("~")) {
                return rsId.substring(1);
            } else {
                return rsId;
            }
        }
        return "";
    }
}
