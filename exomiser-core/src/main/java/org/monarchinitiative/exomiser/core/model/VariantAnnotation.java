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
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Position;
import org.monarchinitiative.svart.Strand;

import java.util.List;

/**
 * Simple immutable data class to represent annotations for a variant.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotation extends AbstractVariant {

    private static final VariantAnnotation EMPTY = new Builder()
            .with(Contig.unknown(), "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(0), "", "", 0)
            .build();

    private VariantAnnotation(Builder builder) {
        super(builder);
    }

    private VariantAnnotation(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end, String ref, String alt, int changeLength, GenomeAssembly genomeAssembly, String geneSymbol, String geneId, VariantEffect variantEffect, List<TranscriptAnnotation> annotations) {
        super(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength, genomeAssembly, geneSymbol, geneId, variantEffect, annotations);
    }

    public static VariantAnnotation empty() {
        return EMPTY;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected VariantAnnotation newVariantInstance(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end, String ref, String alt, int changeLength) {
        return new VariantAnnotation(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength, genomeAssembly, geneSymbol, geneId, variantEffect, annotations);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "genomeAssembly=" + genomeAssembly +
                ", chromosome=" + contig().id() +
                ", contig='" + contig().name() + '\'' +
                ", strand=" + strand() +
                ", start=" + start() +
                ", end=" + end() +
                ", length=" + length() +
                ", ref='" + ref() + '\'' +
                ", alt='" + alt() + '\'' +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", geneId='" + geneId + '\'' +
                ", variantEffect=" + variantEffect +
                ", annotations=" + annotations +
                '}';
    }

    public static Builder builder(GenomeAssembly assembly, int chr, int start, String ref, String alt) {
        return new Builder().with(assembly.getContigById(chr), "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(start), ref, alt)
                .genomeAssembly(assembly);
    }

    public static Builder builder(GenomeAssembly assembly, int chr, int start, int end, String ref, String alt, int changeLength) {
        return new Builder().with(assembly.getContigById(chr), "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(start), Position.of(end), ref, alt, changeLength)
                .genomeAssembly(assembly);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractVariant.Builder<Builder> {

        @Override
        public VariantAnnotation build() {
            return new VariantAnnotation(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
