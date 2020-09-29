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

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractVariantCoordinates implements VariantCoordinates {

    final String contig;
    final int chromosome;
    final int start;
    final String ref;
    final String alt;
    final int length;

    // additional fields for describing structural variants. These need overriding in
    // the case of SNVs to default values based on chromosomeName, startChr, startPos, ref and alt
    final ConfidenceInterval startCi;

    final String endContig;
    final int endChromosome;
    final int end;
    final ConfidenceInterval endCi;
    final VariantType variantType;

    protected AbstractVariantCoordinates(Builder<?> builder) {
        this.contig = builder.contig;
        this.chromosome = builder.startChr;
        this.start = builder.start;
        this.ref = builder.ref;
        this.alt = builder.alt;
        this.length = builder.length;

        // additional fields for structural variants
        this.startCi = builder.startCi;

        this.endContig = builder.endContig.isEmpty() ? builder.contig : builder.endContig;
        this.endChromosome = builder.endChromosome == 0 ? builder.startChr : builder.endChromosome;
        this.end = builder.end == 0 ? builder.start : builder.end;
        this.endCi = builder.endCi;

        this.variantType = builder.variantType;
    }

    @Override
    public String getStartContigName() {
        return contig;
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
    public int getLength() {
        return length;
    }

    @Override
    public VariantType getVariantType() {
        return variantType;
    }

    @Override
    public int getStartContigId() {
        return chromosome;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public ConfidenceInterval getStartCi() {
        return startCi;
    }

    @Override
    public ConfidenceInterval getEndCi() {
        return endCi;
    }

    @Override
    public String getEndContigName() {
        return endContig;
    }

    @Override
    public int getEndContigId() {
        return endChromosome;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractVariantCoordinates)) return false;
        AbstractVariantCoordinates that = (AbstractVariantCoordinates) o;
        return chromosome == that.chromosome &&
                start == that.start &&
                startCi == that.startCi &&
                endChromosome == that.endChromosome &&
                end == that.end &&
                endCi == that.endCi &&
//                genomeAssembly == that.genomeAssembly &&
                contig.equals(that.contig) &&
                endContig.equals(that.endContig) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt) &&
                variantType == that.variantType;
    }

    @Override
    public int hashCode() {
//        return Objects.hash(genomeAssembly, contig, chromosome, start, ref, alt, startCi, endContig, endChromosome, end, endCi, length, variantType);
        return Objects.hash(contig, chromosome, start, ref, alt, startCi, endContig, endChromosome, end, endCi, length, variantType);
    }

    @Override
    public String toString() {
        return "AbstractVariantCoordinates{" +
//                "genomeAssembly=" + genomeAssembly +
                "contig='" + contig + '\'' +
                ", chromosome=" + chromosome +
                ", start=" + start +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", startCi=" + startCi +
                ", endContig='" + endContig + '\'' +
                ", endChromosome=" + endChromosome +
                ", end=" + end +
                ", endCi=" + endCi +
                ", length=" + length +
                ", variantType=" + variantType +
                '}';
    }

    protected abstract static class Builder<T extends Builder<T>> {

//        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();

        private String ref = "";
        private String alt = "";

        private int length = 0;
        private VariantType variantType = VariantType.UNKNOWN;

        private String contig = "";
        private int startChr;
        private int start;
        private ConfidenceInterval startCi = ConfidenceInterval.precise();

        private String endContig = "";
        private int endChromosome;
        private int end;
        private ConfidenceInterval endCi = ConfidenceInterval.precise();

//        public T genomeAssembly(GenomeAssembly genomeAssembly) {
//            this.genomeAssembly = Objects.requireNonNull(genomeAssembly);
//            return self();
//        }

        public T contig(String chromosomeName) {
            this.contig = Objects.requireNonNull(chromosomeName);
            return self();
        }

        public T chromosome(int startChr) {
            this.startChr = startChr;
            return self();
        }

        public T start(int start) {
            this.start = start;
            return self();
        }

        public T ref(String ref) {
            this.ref = Objects.requireNonNull(ref);
            return self();
        }

        public T alt(String alt) {
            this.alt = Objects.requireNonNull(alt);
            return self();
        }

        public T length(int length) {
            this.length = length;
            return self();
        }

        public T variantType(VariantType variantType) {
            this.variantType = Objects.requireNonNull(variantType);
            return self();
        }

        public T startCi(ConfidenceInterval startCi) {
            this.startCi = startCi;
            return self();
        }

        public T endContig(String endContig) {
            this.endContig = Objects.requireNonNull(endContig);
            return self();
        }

        public T endChromosome(int endChromosome) {
            this.endChromosome = endChromosome;
            return self();
        }

        public T end(int end) {
            this.end = end;
            return self();
        }

        public T endCi(ConfidenceInterval endCi) {
            this.endCi = endCi;
            return self();
        }

        protected abstract VariantCoordinates build();

        protected abstract T self();
    }
}

