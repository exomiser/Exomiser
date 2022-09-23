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

import java.util.Objects;

/**
 * Value object representing the copy number of an allele.
 */
public class CopyNumber {

    private static final CopyNumber EMPTY = new CopyNumber(-1);
    private static final CopyNumber CN0 = new CopyNumber(0);
    private static final CopyNumber CN1 = new CopyNumber(1);
    private static final CopyNumber WT = new CopyNumber(2);
    private static final CopyNumber CN3 = new CopyNumber(3);
    private static final CopyNumber CN4 = new CopyNumber(4);

    // Canvas DiploidCaller values https://github.com/Illumina/canvas/blob/master/SoftwareDesignDescription.pdf p 28
    private final int copies;

    private CopyNumber(int copyNumber) {
        this.copies = copyNumber;
    }

    public static CopyNumber of(int copyNumber) {
        return switch (copyNumber) {
            case -1 -> EMPTY;
            case 0 -> CN0;
            case 1 -> CN1;
            case 2 -> WT;
            case 3 -> CN3;
            case 4 -> CN4;
            default -> new CopyNumber(copyNumber);
        };
    }

    public static CopyNumber empty() {
        return EMPTY;
    }

    public int copies() {
        return copies;
    }

    public boolean isEmpty() {
        return copies == -1;
    }

    public boolean isCopyGain() {
        return copies > 2;
    }

    public boolean isCopyLoss() {
        return copies != -1 && copies < 2;
    }

    public boolean isCopyNeutral() {
        return copies == 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopyNumber that = (CopyNumber) o;
        return copies == that.copies;
    }

    @Override
    public int hashCode() {
        return Objects.hash(copies);
    }

    @Override
    public String toString() {
        return "CopyNumber{" +
                "copies=" + copies +
                '}';
    }
}
