/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
 * Class for converting contig names to integer ids.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 12.0.0
 */
public class Contigs {

    // this is a bit lke the ReferenceDictionary but could be part of GenomeAssembly
    private static final Map<String, Integer> CONTIG_MAP;

    static {
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

        // RefSeq and GenBank NC / CM accessions for chromosomes 1-24
        for (int i = 1; i < 25; i++) {
            mapBuilder.put(GenomeAssembly.HG19.getRefSeqAccession(i), i);
            mapBuilder.put(GenomeAssembly.HG19.getGenBankAccession(i), i);
            mapBuilder.put(GenomeAssembly.HG38.getRefSeqAccession(i), i);
            mapBuilder.put(GenomeAssembly.HG38.getGenBankAccession(i), i);
        }
        // Mitochondrial chromosome has same RefSeq and GenBank accession
        mapBuilder.put(GenomeAssembly.HG19.getRefSeqAccession(25), 25);
        mapBuilder.put(GenomeAssembly.HG19.getGenBankAccession(25), 25);

        CONTIG_MAP = mapBuilder.build();
    }

    private Contigs() {
    }

    /**
     * Converts the string-based chromosome representation to an integer value in the range of 0-25 where 0 represents
     * unknown or unplaced contigs. 1-22 are the autosomes, 23 = X, 24 = Y and 25 = M.
     * <p>
     * This method will support numeric, chr-prefixed or NC accessions as valid input. For example 1, chr1, NC_000001.10
     * or NC_000001.11 will return the value 1.
     * <p>
     * Valid sex chromosomes are 23, X, chrX, 24, Y, chrY or the NC accessions. Valid mitochondrial values are M, MT,
     * chrM, 25 or NC_012920.1. Unrecognised and unplaced contigs will have the value 0 returned.
     *
     * @param contigName the string-based representation of the chromosome
     * @return the integer value of the chromosome
     * @since 12.0.0
     */
    public static int parseId(String contigName) {
        return CONTIG_MAP.getOrDefault(contigName, 0);
    }

    /**
     * Converts the integer-based chromosome representation to a string value. Non-autosomal chromosomes are represented
     * using X, Y or MT. Unplaced contigs should be represented as 0.
     *
     * @param chr the integer-based representation of the chromosome
     * @return the string value of the chromosome
     * @throws IllegalArgumentException for integers outside of the range 0-25
     * @since 13.0.0
     */
    public static String toString(int chr) {
        if (chr < 0 || chr > 25) {
            // Exomiser uses '0' to represent unplaced contigs
            throw new IllegalArgumentException("Unsupported chromosome number: " + chr);
        }
        return switch (chr) {
            case 23 -> "X";
            case 24 -> "Y";
            case 25 -> "MT";
            default -> Integer.toString(chr);
        };
    }
}
