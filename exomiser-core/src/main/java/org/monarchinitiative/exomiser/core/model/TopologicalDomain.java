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

import org.monarchinitiative.svart.GenomicInterval;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a topological domain of a piece of chromatin
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record TopologicalDomain(int contigId, int start, int end, Map<String, Integer> genes) implements ChromosomalRegion {

    public TopologicalDomain {
        Objects.requireNonNull(genes, "genes cannot be null");
        genes = Map.copyOf(genes);
        if (start > end) {
            throw new IllegalArgumentException(String.format("Start %d position defined as occurring after end position %d. Please check your positions", start, end));
        }
    }

    public boolean containsPosition(GenomicInterval variant) {
        if (variant.contigId() == contigId) {
            int variantPosition = variant.start();
            return start <= variantPosition && end >= variantPosition;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TopologicalDomain{" +
                "contigId=" + contigId +
                ", start=" + start +
                ", end=" + end +
                ", genes=" + genes +
                '}';
    }

}
