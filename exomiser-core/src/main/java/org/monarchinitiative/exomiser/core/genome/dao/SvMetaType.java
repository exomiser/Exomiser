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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.svart.VariantType;

enum SvMetaType {
    GAIN, GAIN_ME, LOSS, LOSS_ME, INVERSION, COMPLEX, NEUTRAL;

    static SvMetaType toMetaType(VariantType variantType) {
        switch (variantType) {
            case DEL:
            case CNV_LOSS:
            case CNV_LOH:
                return LOSS;
            case DEL_ME:
            case DEL_ME_ALU:
            case DEL_ME_LINE1:
            case DEL_ME_SVA:
            case DEL_ME_HERV:
                return LOSS_ME;
            case INS:
            case DUP:
            case DUP_TANDEM:
            case DUP_INV_BEFORE:
            case DUP_INV_AFTER:
            case CNV_GAIN:
                return GAIN;
            case INS_ME:
            case INS_ME_ALU:
            case INS_ME_LINE1:
            case INS_ME_SVA:
            case INS_ME_HERV:
                return GAIN_ME;
            case INV:
                return INVERSION;
            case CNV:
            case BND:
            case CNV_COMPLEX:
            case TRA:
                return COMPLEX;
            default:
                return NEUTRAL;
        }
    }

    static boolean equivalentTypes(VariantType a, VariantType b) {
        SvMetaType aBaseType = toMetaType(a);
        SvMetaType bBaseType = toMetaType(b);
        return aBaseType == bBaseType;
    }
}
