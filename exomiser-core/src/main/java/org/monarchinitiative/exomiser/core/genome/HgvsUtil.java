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

package org.monarchinitiative.exomiser.core.genome;

import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.VariantType;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgvsUtil {

    private HgvsUtil() {
    }

    public static String toHgvsGenomic(GenomicVariant variant) {
        // order is important here as some variants can fit into more than one category:
        // When a description is possible according to several types, the preferred description is:
        //   (1) deletion, (2) inversion, (3) duplication, (4) conversion, (5) insertion.
        // - When a variant can be described as a duplication or an insertion, prioritisation determines it should be
        //   described as a duplication.
        // - Descriptions removing part of a reference sequence replacing it with part of the same sequence are not
        //   allowed (e.g. NM_004006.2:c.[762_768del;767_774dup])
        if (isDeletion(variant)) {
            return toDeletionString(variant);
        }
        // inversion
        if (isInversion(variant)) {
            return toInversionString(variant);
        }
        // duplication
        if (isDuplication(variant)) {
            return toDuplicationString(variant);
        }
        // conversion
        // insertion
        if (isInsertion(variant)) {
            return toInsertionString(variant);
        }
        if (isSubstitution(variant)) {
            return toSubstitutionString(variant);
        }
        if (isDelIns(variant)) {
            return toDelIns(variant);
        }
        return toSubstitutionString(variant);
    }

    // Substitution
    // a sequence change where, compared to a reference sequence, one nucleotide is replaced by one other nucleotide.
    private static boolean isSubstitution(GenomicVariant variant) {
        return variant.variantType() == VariantType.SNV;
    }

    private static String toSubstitutionString(GenomicVariant variant) {
        return getPrefix(variant) + variant.start() + variant.ref() + ">" + variant.alt();
    }

    // Deletion (del):
    // a sequence change where, compared to a reference sequence, one or more nucleotides are not present (deleted).
    private static boolean isDeletion(GenomicVariant variant) {
        return variant.variantType().baseType() == VariantType.DEL && (variant.isSymbolic() || variant.ref()
                .startsWith(variant.alt()));
    }

    private static String toDeletionString(GenomicVariant variant) {
//        https://varnomen.hgvs.org/recommendations/DNA/variant/deletion/
//        Format: “prefix”“position(s)_deleted”“del”, e.g. g.123_127del
//        NOTE: the recommendation is not to describe the variant as NC_000023.11:g.33344590_33344592delGAT,
//        i.e. describe the deleted nucleotide sequence. This description is longer, it contains redundant information
//        and chances to make an error increases (e.g. NC_000023.11:g.33344590_33344592delTTA).
//        “prefix” = reference sequence used = g.
//        “position(s)_deleted” = position nucleotide or range of nucleotides deleted = 123_127
//        “del” = type of change is a deletion = del 1
        if (variant.isSymbolic()) {
            return getPrefix(variant) + variant.start() + "_" + variant.end() + "del";
        }

        int length = Math.abs(variant.changeLength());
        if (length == 1) {
            return getPrefix(variant) + (variant.start() + 1) + "del";
        }
        int start = variant.startWithCoordinateSystem(CoordinateSystem.zeroBased()) + length;
        int end = start + length - 1;
        // Need to adjust the start and end to report the DELETED section
        return getPrefix(variant) + start + "_" + end + "del";
    }

    // Duplication (dup):
    // a sequence change where, compared to a reference sequence, a copy of one or more nucleotides are inserted
    // directly 3' of the original copy of that sequence.
    private static boolean isDuplication(GenomicVariant variant) {
        if (variant.variantType().baseType() == VariantType.DUP && variant.isSymbolic()) {
            return true;
        }
        // can only detect simple duplications from small variations
        return variant.ref().length() == 1 && altIsDupOfRef(variant.ref(), variant.alt());
    }

    private static boolean altIsDupOfRef(String ref, String alt) {
        if (ref.length() == 0 || alt.length() == 0) {
            return false;
        }
        char refChar = ref.charAt(0);
        for (int i = 0; i < alt.length(); i++) {
            if (alt.charAt(i) != refChar) {
                return false;
            }
        }
        return true;
    }

    private static String toDuplicationString(GenomicVariant variant) {
        // NOTE: the recommendation is not to describe the variant as NM_004006.2:c.20dupT, i.e. describe the duplicated
        // nucleotide sequence. This description is longer, it contains redundant information and chances to make an
        // error increases (e.g. NM_004006.2:c.20dupG).
        if (variant.isSymbolic()) {
            return getPrefix(variant) + variant.start() + "_" + variant.end() + "dup";
        }

        int length = Math.abs(variant.changeLength());
        if (length == 1) {
            return getPrefix(variant) + variant.start() + "dup";
        }
        int end = variant.start() + length - 1;
        return getPrefix(variant) + variant.start() + '_' + end + "dup";
    }

    // Insertion (ins)
    // a sequence change where, compared to the reference sequence, one or more nucleotides are inserted and where the
    // insertion is not a copy of a sequence immediately 5'
    private static boolean isInsertion(GenomicVariant variant) {
        return variant.variantType().baseType() == VariantType.INS && (variant.isSymbolic() || variant.alt()
                .startsWith(variant.ref()));
    }

    private static String toInsertionString(GenomicVariant variant) {
        if (variant.isSymbolic()) {
            // when the postion and/or the sequence of an inserted sequence has not been defined, a description may have
            // a format like g.(100_150)insN[25]. “N[5]”, describes five unknown nucleotides
            return getPrefix(variant) + variant.start() + '_' + variant.end() + "insN[" + variant.changeLength() + ']';
        }
        return getPrefix(variant) + variant.start() + '_' + (variant.start() + 1) + "ins" + variant.alt()
                .substring(variant.ref().length());
    }

    // Inversion
    // a sequence change where, compared to a reference sequence, more than one nucleotide replacing the original
    // sequence are the reverse complement of the original sequence.
    private static boolean isInversion(GenomicVariant variant) {
        return variant.variantType().baseType() == VariantType.INV;
    }

    private static String toInversionString(GenomicVariant variant) {
        return getPrefix(variant) + variant.start() + '_' + variant.end() + "inv";
    }

    // Deletion-insertion (delins):
    // a sequence change where, compared to a reference sequence, one or more nucleotides are replaced by one or more
    // other nucleotides and which is not a substitution, inversion or conversion.
    private static boolean isDelIns(GenomicVariant variant) {
        String ref = variant.ref();
        String alt = variant.alt();
        return variant.variantType() != VariantType.SNV && ref.length() != alt.length() && !variant.alt()
                .startsWith(variant.ref());
    }

    private static String toDelIns(GenomicVariant variant) {
        int length = Math.abs(variant.changeLength());
        if (length == 1) {
            return getPrefix(variant) + variant.start() + "delins" + variant.alt();
        }
        int end = variant.start() + length;
        return getPrefix(variant) + variant.start() + '_' + end + "delins" + variant.alt();
    }

    private static String getPrefix(GenomicVariant variant) {
        return variant.contig().refSeqAccession() + ":g.";
    }
}
