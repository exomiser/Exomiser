/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.util.List;
import java.util.Objects;

/**
 * Simple immutable data class to represent annotations for a variant.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotation implements Variant {

    private static final VariantAnnotation EMPTY = new Builder().build();

    private final GenomeAssembly genomeAssembly;
    private final int chromosome;
    private final String chromosomeName;
    private final int position;
    private final String ref;
    private final String alt;

    private final String geneSymbol;
    private final String geneId;
    private final VariantEffect variantEffect;
    private final List<TranscriptAnnotation> annotations;

    private VariantAnnotation(Builder builder) {
        this.genomeAssembly = builder.genomeAssembly;
        this.chromosome = builder.chromosome;
        this.chromosomeName = builder.chromosomeName;
        this.position = builder.position;
        this.ref = builder.ref;
        this.alt = builder.alt;
        this.geneSymbol = builder.geneSymbol;
        this.geneId = builder.geneId;
        this.variantEffect = builder.variantEffect;
        this.annotations = ImmutableList.copyOf(builder.annotations);
    }

    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    public int getChromosome() {
        return chromosome;
    }

    public String getChromosomeName() {
        return chromosomeName;
    }

    public int getPosition() {
        return position;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneId() {
        return geneId;
    }

    public VariantEffect getVariantEffect() {
        return variantEffect;
    }

    public List<TranscriptAnnotation> getTranscriptAnnotations() {
        return annotations;
    }

    @Override
    public boolean hasTranscriptAnnotations() {
        return !annotations.isEmpty();
    }

    public static VariantAnnotation empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantAnnotation that = (VariantAnnotation) o;
        return chromosome == that.chromosome &&
                position == that.position &&
                genomeAssembly == that.genomeAssembly &&
                Objects.equals(chromosomeName, that.chromosomeName) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(alt, that.alt) &&
                Objects.equals(geneSymbol, that.geneSymbol) &&
                Objects.equals(geneId, that.geneId) &&
                variantEffect == that.variantEffect &&
                Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, chromosome, chromosomeName, position, ref, alt, geneSymbol, geneId, variantEffect, annotations);
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "genomeAssembly=" + genomeAssembly +
                ", chromosome=" + chromosome +
                ", chromosomeName='" + chromosomeName + '\'' +
                ", position=" + position +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", geneId='" + geneId + '\'' +
                ", variantEffect=" + variantEffect +
                ", annotations=" + annotations +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        //Should be GenomeAssembly.UNSPECIFIED ?
        private GenomeAssembly genomeAssembly = GenomeAssembly.HG19;
        private int chromosome = 0;
        private String chromosomeName = "";
        private int position = 0;
        private String ref = "";
        private String alt = "";
        private String geneSymbol = "";
        private String geneId = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = ImmutableList.of();

        public Builder genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = genomeAssembly;
            return this;
        }

        public Builder chromosome(int chr) {
            this.chromosome = chr;
            return this;
        }

        public Builder chromosomeName(String chromosomeName) {
            this.chromosomeName = chromosomeName;
            return this;
        }

        public Builder position(int position) {
            this.position = position;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder alt(String alt) {
            this.alt = alt;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
            return this;
        }

        public Builder geneId(String geneId) {
            this.geneId = geneId;
            return this;
        }

        public Builder variantEffect(VariantEffect variantEffect) {
            this.variantEffect = variantEffect;
            return this;
        }

        public Builder annotations(List<TranscriptAnnotation> annotations) {
            this.annotations = annotations;
            return this;
        }

        public VariantAnnotation build() {
            return new VariantAnnotation(this);
        }
    }
}
