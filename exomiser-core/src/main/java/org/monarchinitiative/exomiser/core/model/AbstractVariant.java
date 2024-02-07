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
    final List<TranscriptAnnotation> annotations;

    AbstractVariant(Builder<?> builder) {
        super(builder);
        this.alleleKey = AlleleProtoAdaptor.toAlleleKey(this);
        this.genomeAssembly = builder.genomeAssembly;
        this.geneSymbol = builder.geneSymbol;
        this.geneId = builder.geneId;
        this.variantEffect = builder.variantEffect;
        this.annotations = List.copyOf(builder.annotations);
    }

    AbstractVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, GenomeAssembly genomeAssembly, String geneSymbol, String geneId, VariantEffect variantEffect, List<TranscriptAnnotation> annotations) {
        super(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
        this.alleleKey = AlleleProtoAdaptor.toAlleleKey(this);
        this.genomeAssembly = genomeAssembly;
        this.geneSymbol = geneSymbol;
        this.geneId = geneId;
        this.variantEffect = variantEffect;
        this.annotations = List.copyOf(annotations);
    }

    @Override
    public AlleleProto.AlleleKey alleleKey() {
        return alleleKey;
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
                annotations.equals(that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), genomeAssembly, geneSymbol, geneId, variantEffect, annotations);
    }

    abstract static class Builder<T extends Builder<T>> extends BaseGenomicVariant.Builder<T> {

        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();

        private String geneSymbol = "";
        private String geneId = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = List.of();

        public T variant(Variant variant) {
            super.variant(variant);
            genomeAssembly = variant.getGenomeAssembly();
            geneSymbol = variant.getGeneSymbol();
            geneId = variant.getGeneId();
            variantEffect = variant.getVariantEffect();
            annotations = variant.getTranscriptAnnotations();
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

        public T annotations(List<TranscriptAnnotation> annotations) {
            this.annotations = Objects.requireNonNull(annotations);
            return self();
        }

        protected abstract AbstractVariant build();

    }
}