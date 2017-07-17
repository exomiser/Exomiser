package org.monarchinitiative.exomiser.core.model;

import java.util.Objects;

/**
 * Class for computing and holding minimised single allele variant coordinates. Normalisation is not complete in that
 * it will not left align the variants, but it will right then left trim and adjust the position accordingly. The coordinates
 * are expected to follow the VCF spec i.e. use 1-based, inclusive positions.
 * <p>
 * It will not accept multiple allele VCF strings and it will not split MNV into SNP.
 * <p>
 * Minimisation follows the specification detailed here: http://genome.sph.umich.edu/wiki/Variant_Normalization
 * and as discussed here: https://macarthurlab.org/2014/04/28/converting-genetic-variants-to-their-minimal-representation
 * <p>
 * A variant is considered minimised if:
 * 1. it has no common nucleotides on the left or right side
 * 2. each allele does not end with the same type of nucleotide, or the shortest allele has length 1
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePosition {

    private final int pos;
    private final String ref;
    private final String alt;

    /**
     * @param pos
     * @param ref
     * @param alt
     * @return an exact representation of the input coordinates.
     */
    public static AllelePosition of(int pos, String ref, String alt) {
        Objects.requireNonNull(ref, "REF string cannot be null");
        Objects.requireNonNull(alt, "ALT string cannot be null");
        return new AllelePosition(pos, ref, alt);
    }

    /**
     * Trims the right, then left side of the given variant allele.
     *
     * @param pos
     * @param ref
     * @param alt
     * @return a minimised representation of the input coordinates.
     */
    public static AllelePosition trim(int pos, String ref, String alt) {
        Objects.requireNonNull(ref, "REF string cannot be null");
        Objects.requireNonNull(alt, "ALT string cannot be null");

        if (cantTrim(ref, alt)) {
            return new AllelePosition(pos, ref, alt);
        }

        // Can't do left alignment as have no reference seq and are assuming this has happened already.
        // Therefore check the sequence is first right trimmed, then left trimmed as per the wiki link above.
        if (needsRightTrim(ref, alt)) {
            int rightIdx = ref.length();
            int diff = ref.length() - alt.length();
            // scan from right to left, ensure right index > 1 so as not to fall off the left end
            while (rightIdx > 1 && rightIdx - diff > 0 && ref.charAt(rightIdx - 1) == alt.charAt(rightIdx - 1 - diff)) {
                rightIdx--;
            }

            ref = ref.substring(0, rightIdx);
            alt = alt.substring(0, rightIdx - diff);
        }

        if (needsLeftTrim(ref, alt)) {
            int leftIdx = 0;
            // scan from left to right
            while (leftIdx < ref.length() && leftIdx < alt.length() && ref.charAt(leftIdx) == alt.charAt(leftIdx)) {
                leftIdx++;
            }
            // correct index so as not to fall off the right end
            if (leftIdx > 0 && leftIdx == ref.length() || leftIdx == alt.length()) {
                leftIdx -= 1;
            }
            pos += leftIdx;
            ref = ref.substring(leftIdx);
            alt = alt.substring(leftIdx);
        }

        return new AllelePosition(pos, ref, alt);
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

    private static boolean cantTrim(String ref, String alt) {
        return ref.length() == 1 || alt.length() == 1;
    }

    private static boolean needsRightTrim(String ref, String alt) {
        int refLength = ref.length();
        int altLength = alt.length();
        return refLength > 1 && altLength > 1 && ref.charAt(refLength - 1) == alt.charAt(altLength - 1);
    }

    private static boolean needsLeftTrim(String ref, String alt) {
        return ref.length() > 1 && alt.length() > 1 && ref.charAt(0) == alt.charAt(0);
    }

    private AllelePosition(int pos, String ref, String alt) {
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllelePosition that = (AllelePosition) o;
        return pos == that.pos &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(alt, that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, ref, alt);
    }

    @Override
    public String toString() {
        return "AllelePosition{" +
                "pos=" + pos +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }

}
