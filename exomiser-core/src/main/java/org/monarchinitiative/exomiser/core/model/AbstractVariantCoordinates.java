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

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractVariantCoordinates implements VariantCoordinates {

    final GenomeAssembly genomeAssembly;

    final String chromosomeName;
    final int chromosome;
    final int start;
    final String ref;
    final String alt;
    final int length;

    // additional fields for describing structural variants. These need overriding in
    // the case of SNVs to default values based on chromosomeName, startChr, startPos, ref and alt
    final int startMin;
    final int startMax;

    final String endChromosomeName;
    final int endChromosome;
    final int end;

    final int endMin;
    final int endMax;

    final VariantType variantType;

    protected AbstractVariantCoordinates(Builder<?> builder) {
        this.genomeAssembly = builder.genomeAssembly;
        this.chromosomeName = builder.chromosomeName;
        this.chromosome = builder.startChr;
        this.start = builder.start;
        this.ref = builder.ref;
        this.alt = builder.alt;
        this.length = builder.length;

        // additional fields for structural variants
        this.startMin = builder.startMin;
        this.startMax = builder.startMax;

        this.endChromosomeName = builder.endChromosomeName.isEmpty() ? builder.chromosomeName : builder.endChromosomeName;
        this.endChromosome = builder.endChromosome == 0 ? builder.startChr : builder.endChromosome;
        this.end = builder.end == 0 ? builder.start : builder.end;
        this.endMin = builder.endMin;
        this.endMax = builder.endMax;

        this.variantType = builder.variantType;
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public String getChromosomeName() {
        return chromosomeName;
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
    public int getChromosome() {
        return chromosome;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getStartMin() {
        return startMin;
    }

    @Override
    public int getStartMax() {
        return startMax;
    }

    @Override
    public String getEndChromosomeName() {
        return endChromosomeName;
    }

    @Override
    public int getEndChromosome() {
        return endChromosome;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public int getEndMin() {
        return endMin;
    }

    @Override
    public int getEndMax() {
        return endMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractVariantCoordinates)) return false;
        AbstractVariantCoordinates that = (AbstractVariantCoordinates) o;
        return chromosome == that.chromosome &&
                start == that.start &&
                startMin == that.startMin &&
                startMax == that.startMax &&
                endChromosome == that.endChromosome &&
                end == that.end &&
                endMin == that.endMin &&
                endMax == that.endMax &&
                genomeAssembly == that.genomeAssembly &&
                chromosomeName.equals(that.chromosomeName) &&
                endChromosomeName.equals(that.endChromosomeName) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt) &&
                variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, chromosomeName, chromosome, start, ref, alt, startMin, startMax, endChromosomeName, endChromosome, end, endMin, endMax, length, variantType);
    }

    @Override
    public String toString() {
        return "AbstractVariantCoordinates{" +
                "genomeAssembly=" + genomeAssembly +
                ", chromosomeName='" + chromosomeName + '\'' +
                ", chromosome=" + chromosome +
                ", startPos=" + start +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", startMin=" + startMin +
                ", startMax=" + startMax +
                ", endChromosomeName='" + endChromosomeName + '\'' +
                ", endChromosome=" + endChromosome +
                ", endPos=" + end +
                ", endMin=" + endMin +
                ", endMax=" + endMax +
                ", length=" + length +
                ", variantType=" + variantType +
                '}';
    }

    protected abstract static class Builder<T extends Builder<T>> {

        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();

        private String ref = "";
        private String alt = "";

        private int length = 0;
        private VariantType variantType = VariantType.UNKNOWN;

        private String chromosomeName = "";
        private int startChr;
        private int start;
        private int startMin;
        private int startMax;

        private String endChromosomeName = "";
        private int endChromosome;
        private int end;
        private int endMin;
        private int endMax;

        public T genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = Objects.requireNonNull(genomeAssembly);
            return self();
        }

        public T chromosomeName(String chromosomeName) {
            this.chromosomeName = Objects.requireNonNull(chromosomeName);
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

        public T startMin(int startMin) {
            this.startMin = startMin;
            return self();
        }

        public T startMax(int startMax) {
            this.startMax = startMax;
            return self();
        }

        public T endChromosomeName(String endChromosomeName) {
            this.endChromosomeName = Objects.requireNonNull(endChromosomeName);
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

        public T endMin(int endMin) {
            this.endMin = endMin;
            return self();
        }

        public T endMax(int endMax) {
            this.endMax = endMax;
            return self();
        }

        protected abstract VariantCoordinates build();

        protected abstract T self();
    }
}

