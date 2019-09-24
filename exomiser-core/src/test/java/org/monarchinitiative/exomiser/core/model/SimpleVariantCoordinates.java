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

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantCoordinates extends AbstractVariantCoordinates {

    private SimpleVariantCoordinates(Builder builder) {
        super(builder);
    }

    public static SimpleVariantCoordinates of(GenomeAssembly genomeAssembly, int chr, int start, String alt, String ref) {
        return new Builder()
                .genomeAssembly(genomeAssembly)
                .chromosome(chr)
                .start(start)
                .ref(ref)
                .alt(alt)
                .build();
    }

    @Override
    public String toString() {
        return "SimpleVariantCoordinates{" +
                "chr=" + chromosome +
                ", start=" + start +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractVariantCoordinates.Builder<Builder> {

        @Override
        public SimpleVariantCoordinates build() {
            return new SimpleVariantCoordinates(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
