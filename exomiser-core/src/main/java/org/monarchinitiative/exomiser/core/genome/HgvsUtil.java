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

import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.core.model.Variant;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgvsUtil {

    private HgvsUtil() {
    }

    public static String toHgvsGenomic(Variant variant) {
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
    private static boolean isSubstitution(Variant variant) {
        return AllelePosition.isSnv(variant.getRef(), variant.getAlt());
    }

    private static String toSubstitutionString(Variant variant) {
        return getPrefix(variant) + variant.getStart() + variant.getRef() + ">" + variant.getAlt();
    }

    // Deletion (del):
    // a sequence change where, compared to a reference sequence, one or more nucleotides are not present (deleted).
    private static boolean isDeletion(Variant variant) {
        return variant.getVariantType().isDeletion() && (variant.isSymbolic() || variant.getRef()
                .startsWith(variant.getAlt()));
    }

    private static String toDeletionString(Variant variant) {
//        Format: “prefix”“position(s)_deleted”“del”, e.g. g.123_127del
//        NOTE: it is allowed to describe the variant as NG_012232.1:g.19_21delTCA
//        “prefix” = reference sequence used = g.
//        “position(s)_deleted” = position nucleotide or range of nucleotides deleted = 123_127
//        “del” = type of change is a deletion = del 1
        if (variant.isSymbolic()) {
            return getPrefix(variant) + variant.getStart() + "_" + variant.getEnd() + "del";
        }

        int length = Math.abs(variant.getLength());
        if (length == 1) {
            return getPrefix(variant) + (variant.getStart() + 1) + "del" + variant.getRef()
                    .substring(variant.getAlt().length());
        }
        int start = variant.getStart() - 1 + (variant.getRef().length() - variant.getAlt().length());
        int end = start + length - 1;
        // Need to adjust the start and end to report the DELETED section
        return getPrefix(variant) + start + "_" + end + "del" + variant.getRef()
                .substring(variant.getAlt().length());
    }

    // Duplication (dup):
    // a sequence change where, compared to a reference sequence, a copy of one or more nucleotides are inserted
    // directly 3' of the original copy of that sequence.
    private static boolean isDuplication(Variant variant) {
        if (variant.getVariantType().isDuplication() && variant.isSymbolic()) {
            return true;
        }
        // can only detect simple duplications from small variations
        return variant.getRef().length() == 1 && altIsDupOfRef(variant.getRef(), variant.getAlt());
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

    private static String toDuplicationString(Variant variant) {
        if (variant.isSymbolic()) {
            return getPrefix(variant) + variant.getStart() + "_" + variant.getEnd() + "dup";
        }

        int length = Math.abs(variant.getLength());
        if (length == 1) {
            return getPrefix(variant) + variant.getStart() + "dup" + variant.getAlt()
                    .substring(variant.getRef().length());
        }
        int end = variant.getStart() + length - 1;
        return getPrefix(variant) + variant.getStart() + '_' + end + "dup" + variant.getAlt()
                .substring(variant.getRef().length());
    }

    // Insertion (ins)
    // a sequence change where, compared to the reference sequence, one or more nucleotides are inserted and where the
    // insertion is not a copy of a sequence immediately 5'
    private static boolean isInsertion(Variant variant) {
        return variant.getVariantType().isInsertion() && (variant.isSymbolic() || variant.getAlt()
                .startsWith(variant.getRef()));
    }

    private static String toInsertionString(Variant variant) {
        if (variant.isSymbolic()) {
            return getPrefix(variant) + variant.getStart() + '_' + variant.getEnd() + "ins";
        }
        return getPrefix(variant) + variant.getStart() + '_' + (variant.getStart() + 1) + "ins" + variant.getAlt()
                .substring(variant.getRef().length());
    }

    // Inversion
    // a sequence change where, compared to a reference sequence, more than one nucleotide replacing the original
    // sequence are the reverse complement of the original sequence.
    private static boolean isInversion(Variant variant) {
        return variant.getVariantType().isInversion();
    }

    private static String toInversionString(Variant variant) {
        return getPrefix(variant) + variant.getStart() + '_' + variant.getEnd() + "inv";
    }

    // Deletion-insertion (delins):
    // a sequence change where, compared to a reference sequence, one or more nucleotides are replaced by one or more
    // other nucleotides and which is not a substitution, inversion or conversion.
    private static boolean isDelIns(Variant variant) {
        String ref = variant.getRef();
        String alt = variant.getAlt();
        return !variant.getVariantType().isSnv() && ref.length() != alt.length() && !variant.getAlt()
                .startsWith(variant.getRef());
    }

    private static String toDelIns(Variant variant) {
        int length = Math.abs(variant.getLength());
        if (length == 1) {
            return getPrefix(variant) + variant.getStart() + "delins" + variant.getAlt();
        }
        int end = variant.getStart() + length;
        return getPrefix(variant) + variant.getStart() + '_' + end + "delins" + variant.getAlt();
    }

    private static String getPrefix(Variant variant) {
        // this would be neater with an actual Chromosome class
        return variant.getGenomeAssembly().getRefSeqAccession(variant.getStartContigId()) + ":g.";
    }
}
