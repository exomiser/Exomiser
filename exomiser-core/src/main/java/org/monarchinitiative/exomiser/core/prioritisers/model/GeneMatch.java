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

package org.monarchinitiative.exomiser.core.prioritisers.model;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record GeneMatch(int queryGeneId, int matchGeneId, double score, List<GeneModelPhenotypeMatch> bestMatchModels) {

    public static final GeneMatch NO_HIT = new GeneMatch(0, 0, 0, List.of());

    public GeneMatch {
        Objects.requireNonNull(bestMatchModels);
        bestMatchModels = List.copyOf(bestMatchModels);
    }

    @Override
    public String toString() {
        return "GeneMatch{" +
                "queryGeneId=" + queryGeneId +
                ", matchGeneId=" + matchGeneId +
                ", score=" + score +
                ", bestMatchModels=" + bestMatchModels +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int queryGeneId = 0;
        private int matchGeneId = 0;
        private double score = 0;
        private List<GeneModelPhenotypeMatch> bestMatchModels = List.of();


        public Builder queryGeneId(int queryGeneId) {
            this.queryGeneId = queryGeneId;
            return this;
        }

        public Builder matchGeneId(int matchGeneId) {
            this.matchGeneId = matchGeneId;
            return this;
        }

        public Builder score(double score) {
            this.score = score;
            return this;
        }

        public Builder bestMatchModels(List<GeneModelPhenotypeMatch> bestMatchModels) {
            this.bestMatchModels = bestMatchModels;
            return this;
        }

        public GeneMatch build() {
            return new GeneMatch(queryGeneId, matchGeneId, score, bestMatchModels);
        }

    }
}