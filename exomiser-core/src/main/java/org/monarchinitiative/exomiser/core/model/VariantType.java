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

package org.monarchinitiative.exomiser.core.model;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum VariantType {

    // default unrecognised type
    UNKNOWN,

    // encompasses all 'non-structural' variants, i.e. SNV, MNV, INDEL < 50 bases
    // n.b. there are differences in definitions of how big a structural variant is ranging from
    // >= 50 (An integrated map of structural variation in 2,504 human genomes https://www.nature.com/articles/nature15394,
    //   A structural variation reference for medical and population genetics https://www.nature.com/articles/s41586-020-2287-8)
    // >150 (HTSJDK VariantContext.MAX_ALLELE_SIZE_FOR_NON_SV)
    // >=1000 (Jannovar VariantAnnotator)
    SNV,
    // a multi-nucleotide variation. Not very informative.
    MNV,

    SYMBOLIC,
    // VCF standard reserved values for structural variants
    DEL,
    //DEL:ME
    DEL_ME(DEL),
    //DEL:ME:ALU
    DEL_ME_ALU(DEL, DEL_ME),
    DEL_ME_LINE1(DEL, DEL_ME),
    DEL_ME_SVA(DEL, DEL_ME),
    DEL_ME_HERV(DEL, DEL_ME),

    INS,
    INS_ME(INS),
    INS_ME_ALU(INS, INS_ME),
    INS_ME_LINE1(INS, INS_ME),
    INS_ME_SVA(INS, INS_ME),
    INS_ME_HERV(INS, INS_ME),

    DUP,
    //DUP:TANDEM
    DUP_TANDEM(DUP),
    //DUP:INV-BEFORE
    DUP_INV_BEFORE(DUP),
    //DUP:INV-AFTER
    DUP_INV_AFTER(DUP),

    INV,
    CNV,
    BND,

    // Non-canonical types used by other progs,

    // Canvas CNV types see: https://github.com/Illumina/canvas/wiki#output
    // These are found in the ID field: Canvas:GAIN,  Canvas:LOSS,  Canvas:LOH,  Canvas:COMPLEX
    // the SVTYPE=CNV
    CNV_GAIN(CNV),
    CNV_LOSS(CNV),
    CNV_LOH(CNV),
    CNV_COMPLEX(CNV),

    //STR - Short Tandem Repeat from ExpansionHunter
    STR,
    //TRA - Translocation from Sniffles
    TRA;

    private final VariantType baseType;
    private final VariantType subType;

    VariantType() {
        this.baseType = this;
        this.subType = this;
    }

    VariantType(VariantType parent) {
        this.baseType = parent;
        this.subType = this;
    }

    VariantType(VariantType baseType, VariantType subType) {
        this.baseType = baseType;
        this.subType = subType;
    }

    public static VariantType parseValue(@Nonnull String value) {
        if (value.isEmpty()) {
            return UNKNOWN;
        }
        String stripped = trimAngleBrackets(Objects.requireNonNull(value));
        switch (stripped) {
            case "SNP":
            case "SNV":
                return SNV;
            case "MNP":
            case "MNV":
                return MNV;
            case "DEL":
                return DEL;
            case "INS":
                return INS;
            case "DUP":
                return DUP;
            case "INV":
                return INV;
            case "CNV":
                return CNV;
            case "BND":
                return BND;
            // STR is not part of the formal spec, but is output by ExpansionHunter
            case "STR":
                return STR;
            case "TRA":
                return TRA;

            //extended DEL types
            case "DEL:ME":
                return DEL_ME;
            case "DEL:ME:ALU":
                return DEL_ME_ALU;
            case "DEL:ME:LINE1":
                return DEL_ME_LINE1;
            case "DEL:ME:SVA":
                return DEL_ME_SVA;
            case "DEL:ME:HERV":
                return DEL_ME_HERV;

            //extended INS types
            case "INS:ME":
                return INS_ME;
            case "INS:ME:ALU":
                return INS_ME_ALU;
            case "INS:ME:LINE1":
                return INS_ME_LINE1;
            case "INS:ME:SVA":
                return INS_ME_SVA;
            case "INS:ME:HERV":
                return INS_ME_HERV;

            //extended DUP types
            case "DUP:TANDEM":
                return DUP_TANDEM;
            case "DUP:INV-BEFORE":
                return DUP_INV_BEFORE;
            case "DUP:INV-AFTER":
                return DUP_INV_AFTER;
            default:
                // fall through to
        }
        if (stripped.startsWith("BND")) {
            return BND;
        }
        if (AllelePosition.isBreakend(value)) {
            return BND;
        }
        // in other cases where we don't recognise the exact type, use the closest type or sub-type
        // given VCF doesn't precisely define these, these are a safer bet that just UNKNOWN
        if (stripped.startsWith("DEL:ME")) {
            return DEL_ME;
        }
        if (stripped.startsWith("DEL")) {
            return DEL;
        }
        if (stripped.startsWith("INS:ME")) {
            return INS_ME;
        }
        if (stripped.startsWith("DUP:TANDEM")) {
            return DUP_TANDEM;
        }
        if (stripped.startsWith("DUP")) {
            return DUP;
        }
        if (stripped.startsWith("CNV")) {
            return CNV;
        }
        // ExpansionHunter formats ShortTandemRepeats with the number of repeats like this: <STR56>
        if (stripped.startsWith("STR")) {
            return STR;
        }
        if (isSymbolic(value)) {
            return SYMBOLIC;
        }
        return UNKNOWN;
    }

    public static VariantType parseAllele(String ref, String alt) {
        if (AllelePosition.isSymbolic(ref, alt)) {
            return parseValue(alt);
        }
        if (ref.length() == alt.length()) {
            if (alt.length() == 1) {
                return VariantType.SNV;
            }
            return VariantType.MNV;
        }
        return AllelePosition.isInsertion(ref, alt) ? VariantType.INS : VariantType.DEL;
    }

    private static boolean isSymbolic(String value) {
        return AllelePosition.isSymbolic(value);
    }

    private static String trimAngleBrackets(String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public VariantType getBaseType() {
        return baseType;
    }

    public VariantType getSubType() {
        return subType;
    }

    public boolean isSnv() {
        return this == SNV;
    }

    public boolean isMnv() {
        return this == MNV;
    }

    public boolean isDeletion() {
        return this.baseType == DEL;
    }

    public boolean isInsertion() {
        return this.baseType == INS;
    }

    public boolean isDuplication() {
        return this.baseType == DUP;
    }

    public boolean isInversion() {
        return this.baseType == INV;
    }

    public boolean isCnv() {
        return this.baseType == CNV;
    }

    public boolean isBreakend() {
        return this.baseType == BND;
    }

}
