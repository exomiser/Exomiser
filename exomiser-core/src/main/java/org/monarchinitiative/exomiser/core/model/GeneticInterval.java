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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import org.monarchinitiative.exomiser.core.genome.Contigs;

import java.util.regex.Pattern;

/**
 * A simple genetic interval defined as "The spatial continuous physical entity
 * which contains ordered genomic sets(DNA, RNA, Allele, Marker,etc.) between
 * and including two points (Nucleic Acid Base Residue) that have a liner
 * primary sequence structure. It is either a proper part of an chromosome or a
 * RNA molecule or an artificial genetic interval."
 *
 * @author Jules Jacobsen
 */
public record GeneticInterval(int contigId, int start, int end) implements ChromosomalRegion {

    public GeneticInterval {
        if (start > end) {
            throw new IllegalArgumentException(String.format("Start %d position defined as occurring after end position %d. Please check your positions", start, end));
        }
    }

    /**
     * Returns a new GeneticInterval from the parsed string. Strings are to be
     * of the format: <li>chr1:123-456 chrY:1234-1220 chr19:345-567</li>
     *
     * @param interval
     * @return
     */
    public static GeneticInterval parseGeneticInterval(String interval) {

        String intervalPattern = "(chr)?(1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X|Y|M|MT):[\\d]+-[\\d]+";
        if (!Pattern.matches(intervalPattern, interval)) {
            throw new IllegalArgumentException(String.format("Genetic interval %s does not match expected pattern %s", interval, intervalPattern));
        }

        String[] intervalSections = interval.split(":");
        int localChr = Contigs.parseId(intervalSections[0]);
        String positions = intervalSections[1];
        String[] startEnd = positions.split("-");

        int localStart = Integer.parseInt(startEnd[0]);
        int localEnd = Integer.parseInt(startEnd[1]);

        return new GeneticInterval(localChr, localStart, localEnd);
    }

    @Override
    public String toString() {
        return String.format("chr%s:%d-%d", toChrString(contigId), start, end);
    }

    private String toChrString(int chromosome) {
        String chr = Contigs.toString(chromosome);
        return "MT".equals(chr) ? "M" : chr;
    }
}
