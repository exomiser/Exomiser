/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractVariant extends AbstractVariantCoordinates implements Variant {

    final String geneSymbol;
    final String geneId;
    final VariantEffect variantEffect;
    final List<TranscriptAnnotation> annotations;

    AbstractVariant(Builder<?> builder) {
        super(builder);
        this.geneSymbol = builder.geneSymbol;
        this.geneId = builder.geneId;
        this.variantEffect = builder.variantEffect;
        this.annotations = ImmutableList.copyOf(builder.annotations);
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
        return geneSymbol.equals(that.geneSymbol) &&
                geneId.equals(that.geneId) &&
                variantEffect == that.variantEffect &&
                annotations.equals(that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), geneSymbol, geneId, variantEffect, annotations);
    }

    abstract static class Builder<T extends Builder<T>> extends AbstractVariantCoordinates.Builder<T> {

        private String geneSymbol = "";
        private String geneId = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = ImmutableList.of();

        public T geneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
            return self();
        }

        public T geneId(String geneId) {
            this.geneId = geneId;
            return self();
        }

        public T variantEffect(VariantEffect variantEffect) {
            this.variantEffect = variantEffect;
            return self();
        }

        public T annotations(List<TranscriptAnnotation> annotations) {
            this.annotations = annotations;
            return self();
        }

        abstract Variant build();

    }
}