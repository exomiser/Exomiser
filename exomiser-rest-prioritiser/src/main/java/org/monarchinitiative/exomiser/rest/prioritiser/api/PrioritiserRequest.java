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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 12.1.0
 */
@JsonDeserialize(builder = PrioritiserRequest.Builder.class)
public class PrioritiserRequest {

    private final List<String> phenotypes;
    private final List<Integer> genes;
    private final String prioritiser;
    private final String prioritiserParams;
    private final int limit;

    private PrioritiserRequest(Builder builder) {
        this.phenotypes = builder.phenotypes.stream().distinct().toList();
        this.genes = builder.genes.stream().distinct().toList();
        this.prioritiser = builder.prioritiser;
        this.prioritiserParams = builder.prioritiserParams;
        this.limit = builder.limit;
    }

    public List<String> getPhenotypes() {
        return phenotypes;
    }

    public List<Integer> getGenes() {
        return genes;
    }

    public String getPrioritiser() {
        return prioritiser;
    }

    public String getPrioritiserParams() {
        return prioritiserParams;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrioritiserRequest)) return false;
        PrioritiserRequest that = (PrioritiserRequest) o;
        return limit == that.limit &&
                phenotypes.equals(that.phenotypes) &&
                genes.equals(that.genes) &&
                prioritiser.equals(that.prioritiser) &&
                prioritiserParams.equals(that.prioritiserParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phenotypes, genes, prioritiser, prioritiserParams, limit);
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
        private String prioritiser = "";
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
            return new PrioritiserRequest(this);
        }
    }
}
