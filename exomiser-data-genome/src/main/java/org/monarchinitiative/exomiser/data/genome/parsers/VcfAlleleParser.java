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

import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class VcfAlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(VcfAlleleParser.class);

    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            return Collections.emptyList();
        }
        String[] fields = line.split("\t");
        List<Allele> alleles = parseAlleles(fields, line);

        if (hasNoInfoField(fields)) {
            return alleles;
        }
        String info = fields[7];

        try {
            parseInfoField(alleles, info);
        } catch (Exception e) {
            logger.error("Unable to parse info field in line '{}'", line, e);
        }

        return alleles;
    }

    private boolean hasNoInfoField(String[] fields) {
        return fields.length <= 7;
    }

    abstract List<Allele> parseInfoField(List<Allele> alleles, String info);

    private List<Allele> parseAlleles(String[] fields, String line) {

        byte chr = ChromosomeParser.parseChr(fields[0]);
        if (chr == 0) {
            return Collections.emptyList();
        }
        int pos = Integer.parseInt(fields[1]);
        //A dbSNP rsID such as rs101432848. In rare cases may be multiple e.g., rs200118651;rs202059104
        String rsId = RsIdParser.parseRsId(fields[2]);
        //Uppercasing shouldn't be necessary acccording to the VCF standard,
        //but occasionally one sees VCF files with lower case for part of the
        //sequences, e.g., to show indels.
        String ref = fields[3].toUpperCase();

        //dbSNP has introduced the concept of multiple minor alleles on the
        //same VCF line with their frequencies reported in same order in the
        //INFO field in the CAF section Because of this had to introduce a loop
        //and move the dbSNP freq parsing to here. Not ideal as ESP processing
        //also goes through this method but does not use the CAF field so
        //should be skipped
        String[] alts = fields[4].toUpperCase().split(",");

        List<Allele> alleles = new ArrayList<>();
        for (int i = 0; i < alts.length; i++) {
            Allele allele = makeAllele(chr, pos, ref, alts[i]);
            allele.setRsId(rsId);
            alleles.add(allele);
        }
        return alleles;
    }

    private Allele makeAllele(byte chr, int pos, String ref, String alt) {
        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);
        return new Allele(chr, allelePosition.getPos(), allelePosition.getRef(), allelePosition.getAlt());
    }

    /**
     * rsIds can be merged - these are reported in the format rs200118651;rs202059104 where the first rsId is the current one,
     * the second is the rsId which was merged into the first.
     *
     * @param rsIds an array of rsId. Can be empty.
     * @return The first rsId present in an array or "." if empty.
     */
    private String getCurrentRsId(String[] rsIds) {
        if (rsIds.length >= 1) {
            return rsIds[0].replaceFirst("~", "");
        }
        return ".";
    }
}
