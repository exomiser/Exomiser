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

import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record RegulatoryFeature(int contigId, int start, int end, FeatureType featureType) implements ChromosomalRegion {

    public enum FeatureType {ENHANCER, TF_BINDING_SITE, PROMOTER, PROMOTER_FLANKING_REGION, CTCF_BINDING_SITE, OPEN_CHROMATIN, FANTOM_PERMISSIVE, UNKNOWN}

    public RegulatoryFeature {
        Objects.requireNonNull(featureType, "featureType cannot be null");
        if (start > end) {
            throw new IllegalArgumentException(String.format("Start %d position defined as occurring after end position %d. Please check your positions", start, end));
        }
    }

    public VariantEffect variantEffect() {
        return VariantEffect.REGULATORY_REGION_VARIANT;
    }

    @Override
    public String toString() {
        return "RegulatoryFeature{" +
                "contigId=" + contigId +
                ", start=" + start +
                ", end=" + end +
                ", featureType=" + featureType +
                '}';
    }
}
