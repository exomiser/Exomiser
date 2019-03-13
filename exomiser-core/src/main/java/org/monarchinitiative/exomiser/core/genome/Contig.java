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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Class for converting contig names to integer ids
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Contig {

    private static final Map<String, Integer> CONTIG_MAP;

    static  {
        ImmutableMap.Builder<String, Integer> mapBuilder = new ImmutableMap.Builder<>();

        // add autosomes 1-22
        for (int i = 1; i < 23; i++) {
            mapBuilder.put(String.valueOf(i), i);
            mapBuilder.put("chr" + i, i);
        }

        // add non-autosomes 23-25
        String[] nonAutosomalChromosomes = {"X", "Y", "M"};
        for (int i = 0; i < nonAutosomalChromosomes.length; i++) {
            String chrom = nonAutosomalChromosomes[i];
            int id = 23 + i;
            // non-standard numerical-based identifier
            mapBuilder.put(String.valueOf(id), id);
            mapBuilder.put("chr" + id, id);
            // standard letter-based identifier
            mapBuilder.put(chrom, id);
            mapBuilder.put("chr" + chrom, id);
        }
        mapBuilder.put("MT", 25);

        CONTIG_MAP = mapBuilder.build();
    }

    private Contig() {
    }

    public static int parseId(String contig) {
        return CONTIG_MAP.getOrDefault(contig, 0);
    }

}
