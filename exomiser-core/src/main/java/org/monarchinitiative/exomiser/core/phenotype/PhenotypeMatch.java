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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

/**
 * Contains information about how well a pair of <code>PhenotypeTerm</code>
 * match each other.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonPropertyOrder({"query", "match", "lcs", "ic", "simj", "score"})
public record PhenotypeMatch(
        @JsonProperty("query")
        PhenotypeTerm queryPhenotype,
        @JsonProperty("match")
        PhenotypeTerm matchPhenotype,
        //lowest common subsumer
        PhenotypeTerm lcs,
        double ic,
        @JsonProperty("simj")
        double simJ,
        double score) {

    public PhenotypeMatch {
//        Objects.requireNonNull(queryPhenotype);
//        Objects.requireNonNull(matchPhenotype);
    }

    @JsonIgnore
    public String queryPhenotypeId() {
        return (queryPhenotype == null) ? "null" : queryPhenotype.id();
    }

    @JsonIgnore
    public String matchPhenotypeId() {
        return (matchPhenotype == null) ? "null" : matchPhenotype.id();
    }

    @Override
    public String toString() {
        return "PhenotypeMatch{" +
               "queryPhenotype=" + queryPhenotype +
               ", matchPhenotype=" + matchPhenotype +
               ", lcs=" + lcs +
               ", ic=" + ic +
               ", simJ=" + simJ +
               ", score=" + score +
               '}';
    }

    @JsonCreator
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PhenotypeTerm queryPhenotype;
        private PhenotypeTerm matchPhenotype;
        //lowest common subsumer
        private PhenotypeTerm lcs;
        //information content (relates to LCS)
        private double ic;
        //Jaccard similarity score
        private double simJ;
        private double score;

        private Builder() {
        }

        public Builder query(PhenotypeTerm queryPhenotype) {
            this.queryPhenotype = queryPhenotype;
            return this;
        }

        public Builder match(PhenotypeTerm matchPhenotype) {
            this.matchPhenotype = matchPhenotype;
            return this;
        }

        public Builder lcs(PhenotypeTerm lcs) {
            this.lcs = lcs;
            return this;
        }

        public Builder ic(double ic) {
            this.ic = ic;
            return this;
        }

        public Builder simj(double simJ) {
            this.simJ = simJ;
            return this;
        }

        public Builder score(double score) {
            this.score = score;
            return this;
        }

        public PhenotypeMatch build() {
            return new PhenotypeMatch(
                    queryPhenotype,
                    matchPhenotype,
                    lcs,
                    ic,
                    simJ,
                    score
            );
        }
    }
}
