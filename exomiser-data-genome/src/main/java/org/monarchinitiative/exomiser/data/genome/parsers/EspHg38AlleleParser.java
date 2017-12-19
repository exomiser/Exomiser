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

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Parser to extract {@link org.monarchinitiative.exomiser.data.genome.model.Allele} objects from ESP with Hg38
 * coordinates.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspHg38AlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(EspHg38AlleleParser.class);

    private static final String UNMAPPABLE_POS = "-1";
    private static final String COMMENT_CHAR = "#";

    private final EspAlleleParser espAlleleParser = new EspAlleleParser();

    @Override
    public List<Allele> parseLine(String line) {
        if (line == null || line.startsWith(COMMENT_CHAR)) {
            return Collections.emptyList();
        }
        String[] fields = line.split("\t");
        //No, this isn't the most efficient way of doing this, but it saves writing a whole new parser
        String hg38PosLine = createHg38PosLine(fields);

        return espAlleleParser.parseLine(hg38PosLine);
    }

    private String createHg38PosLine(String[] fields) {
        //##INFO=<ID=GRCh38_POSITION,Number=.,Type=String,Description="GRCh38 chromosomal postion liftover from the original GRCh37 chromosomal position.
        // A value of -1 means the GRCh37 position can not be mapped to the GRCh38 build.">
        String hg38ChrPos = parseHg38PositionFromInfoField(fields[7]);

        if (hg38ChrPos.equals(UNMAPPABLE_POS)) {
            logger.debug("Position not mapped to Hg38: {} {} {} {} {} {}", fields[0], fields[1], fields[2], fields[3], fields[4], fields[7]);
            return COMMENT_CHAR;
        }

        //GRCh38_POSITION=17:156203
        String[] chrPos = hg38ChrPos.split(":");

        StringJoiner stringJoiner = new StringJoiner("\t");
        stringJoiner.add(chrPos[0]);
        stringJoiner.add(chrPos[1]);

        for (int i = 2; i < fields.length; i++) {
            stringJoiner.add(fields[i]);
        }

        return stringJoiner.toString();
    }

    private String parseHg38PositionFromInfoField(String info) {
        String[] infoFields = info.split(";");
        //the GRCh38_POSITION field is usually at the end of the line, so we're walking backwards through the array
        for (int i = infoFields.length - 1; i >= 0; i--) {
            String infoField = infoFields[i];
            //GRCh38_POSITION=17:156203
            if (infoField.startsWith("GRCh38_POSITION=")) {
                return infoField.substring("GRCh38_POSITION=".length());
            }
        }
        return UNMAPPABLE_POS;
    }

}
