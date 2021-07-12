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

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SvFrequency implements OutputLine, Comparable<SvFrequency> {

    private final int chr;
    private final int start;
    private final int end;
    private final int svLen;
    private final VariantType svType;
    private final String dbVarId;
    private final String source;
    private final String sourceId;
    private final int alleleCount;
    private final int alleleNum;

    public SvFrequency(int chr, int start, int end, int svLen, VariantType svType, String dbVarId, String source, String sourceId, int alleleCount, int alleleNum) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.svType = svType;
        this.svLen = svLen == 0 ? SvLengthCalculator.calculateLength(start, end, svType) : svLen;
        this.dbVarId = dbVarId;
        this.source = source;
        this.sourceId = sourceId;
        this.alleleCount = alleleCount;
        this.alleleNum = alleleNum;
    }

    public int getChr() {
        return chr;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getSvLen() {
        return svLen;
    }

    public VariantType getSvType() {
        return svType;
    }

    public String getDbVarId() {
        return dbVarId;
    }

    @Override
    public String toOutputLine() {
        //    CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
        return chr + SEP +
                start + SEP +
                end + SEP +
                svLen + SEP +
                svType + SEP +
                dbVarId + SEP +
                source + SEP +
                sourceId + SEP +
                alleleCount + SEP +
                alleleNum;
    }

    @Override
    public int compareTo(SvFrequency o) {
        int result = Integer.compare(chr, o.chr);
        if (result == 0) {
            result = Integer.compare(start, o.start);
        }
        if (result == 0) {
            result = Integer.compare(end, o.end);
        }
        if (result == 0) {
            result = Integer.compare(svLen, o.svLen);
        }
        if (result == 0) {
            result = svType.compareTo(o.svType);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SvFrequency)) return false;
        SvFrequency that = (SvFrequency) o;
        return chr == that.chr &&
                start == that.start &&
                end == that.end &&
                svLen == that.svLen &&
                alleleCount == that.alleleCount &&
                alleleNum == that.alleleNum &&
                source.equals(that.source) &&
                svType == that.svType &&
                sourceId.equals(that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chr, start, end, svLen, source, svType, sourceId, alleleCount, alleleNum);
    }

    @Override
    public String toString() {
        return "SvFrequency{" +
                "chr=" + chr +
                ", start=" + start +
                ", end=" + end +
                ", svLen=" + svLen +
                ", svType=" + svType +
                ", dbVarId=" + dbVarId +
                ", source='" + source + '\'' +
                ", id='" + sourceId + '\'' +
                ", alleleCount=" + alleleCount +
                ", alleleNum=" + alleleNum +
                '}';
    }
}
