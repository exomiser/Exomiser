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

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum StructuralType {
    // encompasses all non-structural variants, i.e. SNV, MNV, INDEL < 999 bases
    NON_STRUCTURAL,
    // Structural, but not of a recognised type
    UNKNOWN,
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
    // Non-canonical types used by other progs

    //STR - Short Tandem Repeat from ExpansionHunter
    STR,
    //TRA - Translocation from Sniffles
    TRA;

    private StructuralType baseType;
    private StructuralType subType;

    StructuralType() {
        this.baseType = this;
        this.subType = this;
    }

    StructuralType(StructuralType parent) {
        this.baseType = parent;
        this.subType = this;
    }

    StructuralType(StructuralType baseType, StructuralType subType) {
        this.baseType = baseType;
        this.subType = subType;
    }

    public static StructuralType parseValue(String value) {
        String stripped = trimInput(value);
        // ExpansionHunter formats ShortTandemRepeats with the number of repeats like this: <STR56>
        if (stripped.startsWith("STR")) {
            return STR;
        }
        switch (stripped) {
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
        if (stripped.startsWith("BND")) {
            return BND;
        }
        return UNKNOWN;
    }

    private static String trimInput(String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public StructuralType getBaseType() {
        return baseType;
    }

    public StructuralType getSubType() {
        return subType;
    }

    public boolean isStructural() {
        return this != NON_STRUCTURAL;
    }
}
