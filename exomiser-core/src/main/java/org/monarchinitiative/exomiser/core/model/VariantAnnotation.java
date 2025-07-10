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

import java.util.List;
import java.util.Objects;

/**
 * Simple immutable data class to represent annotations for a variant.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record VariantAnnotation(String geneSymbol, String geneId, VariantEffect variantEffect, List<TranscriptAnnotation> transcriptAnnotations) implements VariantAnnotations {

    private static final VariantAnnotation EMPTY = new VariantAnnotation("", "", VariantEffect.SEQUENCE_VARIANT, List.of());

    public VariantAnnotation {
        Objects.requireNonNull(geneSymbol);
        Objects.requireNonNull(geneId);
        Objects.requireNonNull(variantEffect);
        Objects.requireNonNull(transcriptAnnotations);
    }

    public static VariantAnnotation of(String geneSymbol, String geneId, VariantEffect variantEffect, List<TranscriptAnnotation> transcriptAnnotations) {
        if (geneSymbol.isEmpty() && geneId.isEmpty() && variantEffect == VariantEffect.SEQUENCE_VARIANT && transcriptAnnotations.isEmpty()) {
            return VariantAnnotation.empty();
        }
        return new VariantAnnotation(geneSymbol, geneId, variantEffect, transcriptAnnotations);
    }

    public static VariantAnnotation empty() {
        return EMPTY;
    }

    @Override
    public boolean hasTranscriptAnnotations() {
        return !transcriptAnnotations.isEmpty();
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "geneSymbol='" + geneSymbol + '\'' +
                ", geneId='" + geneId + '\'' +
                ", variantEffect=" + variantEffect +
                ", transcriptAnnotations=" + transcriptAnnotations +
                '}';
    }
}
