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

package org.monarchinitiative.exomiser.core.model;

import org.monarchinitiative.svart.VariantType;

public enum SvMetaType {
    GAIN, GAIN_ME, LOSS, LOSS_ME, INVERSION, COMPLEX, CNV, NEUTRAL;

    private static SvMetaType toMetaType(VariantType variantType) {
        return switch (variantType) {
            case DEL, CNV_LOSS, CNV_LOH -> LOSS;
            case DEL_ME, DEL_ME_ALU, DEL_ME_LINE1, DEL_ME_SVA, DEL_ME_HERV -> LOSS_ME;
            case INS, DUP, DUP_TANDEM, DUP_INV_BEFORE, DUP_INV_AFTER, CNV_GAIN, STR -> GAIN;
            case INS_ME, INS_ME_ALU, INS_ME_LINE1, INS_ME_SVA, INS_ME_HERV -> GAIN_ME;
            case INV -> INVERSION;
            case CNV -> CNV;
            case BND, CNV_COMPLEX, TRA -> COMPLEX;
            default -> NEUTRAL;
        };
    }

    public static boolean isEquivalent(VariantType a, VariantType b) {
        SvMetaType aBaseType = toMetaType(a);
        SvMetaType bBaseType = toMetaType(b);
        if (aBaseType == bBaseType) {
            return true;
        }
        if (aBaseType == CNV && isGainOrLoss(bBaseType)) {
            return true;
        }
        return bBaseType == CNV && isGainOrLoss(aBaseType);
    }

    private static boolean isGainOrLoss(SvMetaType svMetaType) {
        return isGain(svMetaType) || isLoss(svMetaType);
    }

    private static boolean isGain(SvMetaType svMetaType) {
        return svMetaType == GAIN || svMetaType == GAIN_ME;
    }

    private static boolean isLoss(SvMetaType svMetaType) {
        return svMetaType == LOSS || svMetaType == LOSS_ME;
    }

    public static boolean isGain(VariantType variantType) {
        SvMetaType svMetaType = toMetaType(variantType);
        return isGain(svMetaType);
    }

    public static boolean isLoss(VariantType variantType) {
        SvMetaType svMetaType = toMetaType(variantType);
        return isLoss(svMetaType);
    }

    public static boolean isNeutral(VariantType variantType) {
        SvMetaType svMetaType = toMetaType(variantType);
        return svMetaType == NEUTRAL;
    }
}
