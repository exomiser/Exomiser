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

/**
 * Simple immutable data class to represent annotations for a variant.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotation extends AbstractVariant {

    private static final VariantAnnotation EMPTY = new Builder().build();

    private VariantAnnotation(Builder builder) {
        super(builder);
    }

    public static VariantAnnotation empty() {
        return EMPTY;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "genomeAssembly=" + genomeAssembly +
                ", chromosome=" + chromosome +
                ", chromosomeName='" + chromosomeName + '\'' +
                ", start=" + startPos +
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

    public static class Builder extends AbstractVariant.Builder<Builder> {

        @Override
        public VariantAnnotation build() {
            return new VariantAnnotation(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
