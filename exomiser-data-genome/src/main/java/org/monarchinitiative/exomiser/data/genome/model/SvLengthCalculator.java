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

package org.monarchinitiative.exomiser.data.genome.model;

import org.monarchinitiative.svart.VariantType;

public class SvLengthCalculator {

    private SvLengthCalculator() {
        // static utility class
    }

    /**
     * Caution! This method assumes 1-based coordinates.
     *
     * @param start
     * @param end
     * @param variantType
     * @return
     */
    public static int calculateLength(int start, int end, VariantType variantType) {
        int zeroStart = start - 1;
        // CNV_GAIN and CNV_LOSS have a CNV case type which is no so informative.
        if (variantType == VariantType.CNV_GAIN) {
            return end - zeroStart;
        } else if (variantType == VariantType.CNV_LOSS) {
            return zeroStart - end;
        }
        switch (variantType.baseType()) {
            case DEL:
                return zeroStart - end;
            case CNV:
            case DUP:
            case INS:
            case INV:
                return end - zeroStart;
            default:
                return 0;
        }
    }
}
