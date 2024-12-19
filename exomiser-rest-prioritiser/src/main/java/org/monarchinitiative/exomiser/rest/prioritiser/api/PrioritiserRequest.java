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

package org.monarchinitiative.exomiser.rest.prioritiser.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 12.1.0
 */
@JsonDeserialize(builder = PrioritiserRequest.Builder.class)
@Schema(description = "Request parameters for gene prioritisation")
public record PrioritiserRequest(
        @Schema(
                description = "Set of HPO phenotype identifiers",
                example = "[\"HP:0001156\", \"HP:0001363\", \"HP:0011304\", \"HP:0010055\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<String> phenotypes,

        @Schema(
                description = "Set of NCBI gene IDs to consider in prioritisation",
                example = "[2263, 2264]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        List<Integer> genes,

        @Schema(
                description = "Name of the prioritiser algorithm to use. One of ['hiphive', 'phenix', 'phive']. " +
                              "Defaults to 'hiphive' which allows for cross-species and PPI hits. 'phenix' is a" +
                              " legacy prioritiser which will only prioritise human disease-gene associations. It is" +
                              " the equivalent of 'hiphive' with prioritiser-params='human'. 'phive' is just the" +
                              " mouse subset of hiphive, equivalent to 'hiphive' with prioritiser-params='mouse'.",
                example = "hiphive",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String prioritiser,

        @Schema(
                description = "Additional parameters for the prioritiser. This is optional for the 'hiphive' prioritiser." +
                              " values can be at least one of 'human,mouse,fish,ppi'. Will default to all, however" +
                              " just 'human' will restrict matches to known human disease-gene associations.",
                example = "human",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String prioritiserParams,

        @Schema(
                description = "Maximum number of results to return (0 for unlimited)",
                example = "20",
                defaultValue = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        int limit
) {

    static PrioritiserRequest from(Builder builder) {
        Objects.requireNonNull(builder);
        return new PrioritiserRequest(
                builder.phenotypes.stream().distinct().toList(),
                builder.genes.stream().distinct().toList(),
                builder.prioritiser,
                builder.prioritiserParams,
                builder.limit);
    }

    @Override
    public String toString() {
        return "PrioritiserRequest{" +
                "phenotypes=" + phenotypes +
                ", genes=" + genes +
                ", prioritiser='" + prioritiser + '\'' +
                ", prioritiserParams='" + prioritiserParams + '\'' +
                ", limit=" + limit +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Collection<String> phenotypes = new ArrayList<>();
        private Collection<Integer> genes = new ArrayList<>();
        private String prioritiser = "hiphive";
        private String prioritiserParams = "";
        private int limit;

        public Builder phenotypes(Collection<String> phenotypes) {
            this.phenotypes = Objects.requireNonNull(phenotypes, "phenotypes");
            return this;
        }

        public Builder genes(Collection<Integer> genes) {
            this.genes = Objects.requireNonNull(genes, "genes");
            return this;
        }

        public Builder prioritiser(String prioritiser) {
            this.prioritiser = Objects.requireNonNull(prioritiser, "prioritiser");
            return this;
        }

        public Builder prioritiserParams(String prioritiserParams) {
            this.prioritiserParams = Objects.requireNonNull(prioritiserParams, "prioritiserParams");
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public PrioritiserRequest build() {
            return PrioritiserRequest.from(this);
        }
    }
}
