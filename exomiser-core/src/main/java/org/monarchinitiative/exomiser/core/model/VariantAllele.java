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

import org.monarchinitiative.exomiser.core.genome.Contigs;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAllele implements VariantCoordinates {

    private final String startContig;
    private final int start;
    private final ConfidenceInterval startCi;

    private final String endContig;
    private final int end;
    private final ConfidenceInterval endCi;

    private final int length;

    private final String ref;
    private final String alt;
    private final VariantType variantType;

    private VariantAllele(String startContig, int start, int end, String ref, String alt, int length, VariantType variantType, String endContig, ConfidenceInterval startCi, ConfidenceInterval endCi) {
        this.startContig = Objects.requireNonNull(startContig);
        this.start = start;
        this.end = end;
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
        this.length = length;
        this.variantType = Objects.requireNonNull(variantType);
        this.endContig = Objects.requireNonNull(endContig);
        this.startCi = Objects.requireNonNull(startCi);
        this.endCi = Objects.requireNonNull(endCi);
    }

    public static VariantAllele of(String contig, int start, String ref, String alt) {
        if (AllelePosition.isSymbolic(ref, alt)) {
            throw new IllegalArgumentException("Incompatible allele - symbolic allele " + contig + " " + start + " " + ref + " " + alt + " should have type, end and length specified");
        }
        return trimAndParseType(contig, start, ref, alt);
    }

    public static VariantAllele of(String contig, int start, int end, String ref, String alt, int length, VariantType variantType, String endContig, ConfidenceInterval startCi, ConfidenceInterval endCi) {
        if (!AllelePosition.isSymbolic(ref, alt)) {
            return trimAndParseType(contig, start, ref, alt);
        }
        return new VariantAllele(contig, start, end, ref, alt, length, variantType, endContig, startCi, endCi);
    }

    private static VariantAllele trimAndParseType(String contig, int start, String ref, String alt) {
        AllelePosition allelePosition = AllelePosition.trim(start, ref, alt);
        VariantType variantType = VariantType.parseAllele(allelePosition.getRef(), allelePosition.getAlt());
        return new VariantAllele(contig, allelePosition.getStart(), allelePosition.getEnd(), allelePosition.getRef(), allelePosition
                .getAlt(), allelePosition.getLength(), variantType, contig, ConfidenceInterval.precise(), ConfidenceInterval
                .precise());
    }

    @Override
    public String getStartContigName() {
        return startContig;
    }

    @Override
    public String getEndContigName() {
        return endContig;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getAlt() {
        return alt;
    }

    @Override
    public VariantType getVariantType() {
        return variantType;
    }

    @Override
    public int getStartContigId() {
        return Contigs.parseId(startContig);
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public ConfidenceInterval getStartCi() {
        return startCi;
    }

    @Override
    public int getEndContigId() {
        return Contigs.parseId(endContig);
    }

    @Override
    public ConfidenceInterval getEndCi() {
        return endCi;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantAllele)) return false;
        VariantAllele that = (VariantAllele) o;
        return start == that.start &&
                end == that.end &&
                length == that.length &&
                startContig.equals(that.startContig) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt) &&
                variantType == that.variantType &&
                endContig.equals(that.endContig) &&
                startCi.equals(that.startCi) &&
                endCi.equals(that.endCi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startContig, start, end, ref, alt, length, variantType, endContig, startCi, endCi);
    }

    @Override
    public String toString() {
        return "VariantAllele{" +
                "contig='" + startContig + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", length=" + length +
                ", variantType=" + variantType +
                ", endContig='" + endContig + '\'' +
                ", ciStart=" + startCi +
                ", ciEnd=" + endCi +
                '}';
    }
}
