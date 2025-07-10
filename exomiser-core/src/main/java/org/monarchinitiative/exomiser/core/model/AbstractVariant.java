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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractVariant extends BaseGenomicVariant<AbstractVariant> implements Variant {

    final GenomeAssembly genomeAssembly;

    final AlleleProto.AlleleKey alleleKey;

    final String geneSymbol;
    final String geneId;
    final VariantEffect variantEffect;
    final List<TranscriptAnnotation> transcriptAnnotations;

    AbstractVariant(Builder<?> builder) {
        super(builder);
        this.alleleKey = AlleleProtoAdaptor.toAlleleKey(this);
        this.genomeAssembly = builder.genomeAssembly;
        this.geneSymbol = builder.geneSymbol;
        this.geneId = builder.geneId;
        this.variantEffect = builder.variantEffect;
        this.transcriptAnnotations = List.copyOf(builder.transcriptAnnotations);
    }

    AbstractVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, GenomeAssembly genomeAssembly, String geneSymbol, String geneId, VariantEffect variantEffect, List<TranscriptAnnotation> transcriptAnnotations) {
        super(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
        this.alleleKey = AlleleProtoAdaptor.toAlleleKey(this);
        this.genomeAssembly = genomeAssembly;
        this.geneSymbol = geneSymbol;
        this.geneId = geneId;
        this.variantEffect = variantEffect;
        this.transcriptAnnotations = List.copyOf(transcriptAnnotations);
    }

    @Override
    public AlleleProto.AlleleKey alleleKey() {
        return alleleKey;
    }

    public String geneSymbol() {
        return geneSymbol;
    }

    public String geneId() {
        return geneId;
    }

    public VariantEffect variantEffect() {
        return variantEffect;
    }

    public List<TranscriptAnnotation> transcriptAnnotations() {
        return transcriptAnnotations;
    }

    @Override
    public boolean hasTranscriptAnnotations() {
        return !transcriptAnnotations.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractVariant)) return false;
        if (!super.equals(o)) return false;
        AbstractVariant that = (AbstractVariant) o;
        return genomeAssembly == that.genomeAssembly &&
               geneSymbol.equals(that.geneSymbol) &&
               geneId.equals(that.geneId) &&
                variantEffect == that.variantEffect &&
               transcriptAnnotations.equals(that.transcriptAnnotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), genomeAssembly, geneSymbol, geneId, variantEffect, transcriptAnnotations);
    }

    abstract static class Builder<T extends Builder<T>> extends BaseGenomicVariant.Builder<T> {

        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();

        private String geneSymbol = "";
        private String geneId = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> transcriptAnnotations = List.of();

        public T variant(Variant variant) {
            super.variant(variant);
            genomeAssembly = variant.genomeAssembly();
            geneSymbol = variant.geneSymbol();
            geneId = variant.geneId();
            variantEffect = variant.variantEffect();
            transcriptAnnotations = variant.transcriptAnnotations();
            return self();
        }

        public T genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = Objects.requireNonNull(genomeAssembly);
            return self();
        }

        public T geneSymbol(String geneSymbol) {
            this.geneSymbol = Objects.requireNonNull(geneSymbol);
            return self();
        }

        public T geneId(String geneId) {
            this.geneId = Objects.requireNonNull(geneId);
            return self();
        }

        public T variantEffect(VariantEffect variantEffect) {
            this.variantEffect = Objects.requireNonNull(variantEffect);
            return self();
        }

        public T transcriptAnnotations(List<TranscriptAnnotation> transcriptAnnotations) {
            this.transcriptAnnotations = Objects.requireNonNull(transcriptAnnotations);
            return self();
        }

        protected abstract AbstractVariant build();

    }
}