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

import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.svart.VariantType;

import java.util.Objects;

public class SvPathogenicity implements OutputLine, Comparable<SvPathogenicity> {

    private final int chr;
    private final int start;
    private final int end;
    private final int svLen;
    private final VariantType svType;
    private final String dbVarId;
    private final String source;
    private final String rcvId;
    private final String variationId;
    private final ClinVarData.ClinSig clinSig;
    private final ClinVarData.ReviewStatus clinRevStat;

    public SvPathogenicity(int chr, int start, int end, int svLen, VariantType svType, String dbVarId, String source, String rcvId, String variationId, ClinVarData.ClinSig clinSig, ClinVarData.ReviewStatus clinRevStat) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.svLen = svLen;
        this.svType = svType;
        this.dbVarId = dbVarId;
        this.source = source;
        this.rcvId = rcvId;
        this.variationId = variationId;
        this.clinSig = clinSig;
        this.clinRevStat = clinRevStat;
    }

    public int chr() {
        return chr;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public int svLen() {
        return svLen;
    }

    public VariantType svType() {
        return svType;
    }

    public String dbVarId() {
        return dbVarId;
    }

    public String source() {
        return source;
    }

    public String rcvId() {
        return rcvId;
    }

    public String variationId() {
        return variationId;
    }

    public ClinVarData.ClinSig clinSig() {
        return clinSig;
    }

    public ClinVarData.ReviewStatus clinRevStat() {
        return clinRevStat;
    }

    @Override
    public String toOutputLine() {
        //    CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | RCV_AC | VARIATION_ID | VARIATION_ID | CLIN_SIG | CLIN_REV_STAT
        return chr + SEP +
                start + SEP +
                end + SEP +
                svLen + SEP +
                svType + SEP +
                dbVarId + SEP +
                source + SEP +
                rcvId + SEP +
                variationId + SEP +
                clinSig + SEP +
                clinRevStat;
    }

    @Override
    public int compareTo(SvPathogenicity o) {
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
        if (o == null || getClass() != o.getClass()) return false;
        SvPathogenicity that = (SvPathogenicity) o;
        return chr == that.chr && start == that.start && end == that.end && svLen == that.svLen && svType == that.svType && dbVarId.equals(that.dbVarId) && source.equals(that.source) && rcvId.equals(that.rcvId) && variationId.equals(that.variationId) && clinSig == that.clinSig && clinRevStat.equals(that.clinRevStat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chr, start, end, svLen, svType, dbVarId, source, rcvId, variationId, clinSig, clinRevStat);
    }

    @Override
    public String toString() {
        return "SvPathogenicity{" +
                "chr=" + chr +
                ", start=" + start +
                ", end=" + end +
                ", svLen=" + svLen +
                ", svType=" + svType +
                ", dbVarId='" + dbVarId + '\'' +
                ", source='" + source + '\'' +
                ", rcvId='" + rcvId + '\'' +
                ", variationId='" + variationId + '\'' +
                ", clinSig='" + clinSig + '\'' +
                ", clinRevStat='" + clinRevStat + '\'' +
                '}';
    }
}
