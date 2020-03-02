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

package org.monarchinitiative.exomiser.core.model;

import java.util.Objects;

/**
 * Class for computing and holding minimised single allele variant coordinates. Normalisation is not complete in that
 * it will not left align the variants, but it will right then left trim and adjust the position accordingly. The coordinates
 * are expected to follow the VCF spec i.e. use 1-based, inclusive positions.
 * <p>
 * It will not accept multiple allele VCF strings and it will not split MNV into SNP.
 * <p>
 * Minimisation follows the specification of Tan et al. 2015 https://dx.doi.org/10.1093/bioinformatics/btv112
 * Further details here: http://genome.sph.umich.edu/wiki/Variant_Normalization
 * and as discussed here: https://macarthurlab.org/2014/04/28/converting-genetic-variants-to-their-minimal-representation
 * <p>
 * A variant is considered minimised if:
 * 1. it has no common nucleotides on the left or right side
 * 2. each allele does not end with the same type of nucleotide, or the shortest allele has length 1
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePosition {

    private final int start;
    private final String ref;
    private final String alt;

    /**
     * @param start
     * @param ref
     * @param alt
     * @return an exact representation of the input coordinates.
     */
    public static AllelePosition of(int start, String ref, String alt) {
        Objects.requireNonNull(ref, "REF string cannot be null");
        Objects.requireNonNull(alt, "ALT string cannot be null");
        return new AllelePosition(start, ref, alt);
    }

    /**
     * Trims the right, then left side of the given variant allele.
     *
     * @param start
     * @param ref
     * @param alt
     * @return a minimised representation of the input coordinates.
     */
    public static AllelePosition trim(int start, String ref, String alt) {
        Objects.requireNonNull(ref, "REF string cannot be null");
        Objects.requireNonNull(alt, "ALT string cannot be null");

        if (cantTrim(ref, alt)) {
            return new AllelePosition(start, ref, alt);
        }

        // copy these here in order not to change input params
        int trimStart = start;
        String trimRef = ref;
        String trimAlt = alt;

        // Can't do left alignment as have no reference seq and are assuming this has happened already.
        // Therefore check the sequence is first right trimmed, then left trimmed as per the wiki link above.
        if (canRightTrim(trimRef, trimAlt)) {
            int rightIdx = trimRef.length();
            int diff = trimRef.length() - trimAlt.length();
            // scan from right to left, ensure right index > 1 so as not to fall off the left end
            while (rightIdx > 1 && rightIdx - diff > 0 && trimRef.charAt(rightIdx - 1) == trimAlt.charAt(rightIdx - 1 - diff)) {
                rightIdx--;
            }

            trimRef = trimRef.substring(0, rightIdx);
            trimAlt = trimAlt.substring(0, rightIdx - diff);
        }

        if (canLeftTrim(trimRef, trimAlt)) {
            int leftIdx = 0;
            // scan from left to right
            while (leftIdx < trimRef.length() && leftIdx < trimAlt.length() && trimRef.charAt(leftIdx) == trimAlt.charAt(leftIdx)) {
                leftIdx++;
            }
            // correct index so as not to fall off the right end
            if (leftIdx > 0 && leftIdx == trimRef.length() || leftIdx == trimAlt.length()) {
                leftIdx -= 1;
            }
            trimStart += leftIdx;
            trimRef = trimRef.substring(leftIdx);
            trimAlt = trimAlt.substring(leftIdx);
        }

        return new AllelePosition(trimStart, trimRef, trimAlt);
    }

    public static boolean isSnv(String ref, String alt) {
        return ref.length() == 1 && alt.length() == 1;
    }

    public static boolean isDeletion(String ref, String alt) {
        return ref.length() > alt.length();
    }

    public static boolean isInsertion(String ref, String alt) {
        return ref.length() < alt.length();
    }

    /**
     *
     * @since 12.0.0
     * @param ref the reference allele
     * @param alt the alternate allele
     * @return true if the ref or alt allele is considered symbolic
     */
    public static boolean isSymbolic(String ref, String alt) {
        // The VCF spec only mentions alt alleles as having symbolic characters, so check these first then check the ref
        // just in case.
        return isSymbolic(alt) || isSymbolic(ref);
    }

    private static boolean isSymbolic(String allele) {
        // shamelessly copied from HTSJDK Allele via Jannovar
        if (allele.length() <= 1)
            return false;
        return (allele.charAt(0) == '<' || allele.charAt(allele.length() - 1) == '>') || // symbolic or large insertion
                (allele.charAt(0) == '.' || allele.charAt(allele.length() - 1) == '.') || // single breakend
                (allele.contains("[") || allele.contains("]")); // mated breakend
    }

    private static boolean cantTrim(String ref, String alt) {
        return ref.length() == 1 || alt.length() == 1;
    }

    private static boolean canRightTrim(String ref, String alt) {
        int refLength = ref.length();
        int altLength = alt.length();
        return refLength > 1 && altLength > 1 && ref.charAt(refLength - 1) == alt.charAt(altLength - 1);
    }

    private static boolean canLeftTrim(String ref, String alt) {
        return ref.length() > 1 && alt.length() > 1 && ref.charAt(0) == alt.charAt(0);
    }

    private AllelePosition(int start, String ref, String alt) {
        this.start = start;
        this.ref = ref;
        this.alt = alt;
    }

    public int getStart() {
        return start;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public boolean isSymbolic() {
        return isSymbolic(ref, alt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllelePosition that = (AllelePosition) o;
        return start == that.start &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(alt, that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, ref, alt);
    }

    @Override
    public String toString() {
        return "AllelePosition{" +
                "start=" + start +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }

}
