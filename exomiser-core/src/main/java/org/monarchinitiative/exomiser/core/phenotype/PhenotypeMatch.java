/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
public final class PhenotypeMatch {
    
    private final PhenotypeTerm matchPhenotype;
    private final PhenotypeTerm queryPhenotype;
    //lowest common subsumer
    private final PhenotypeTerm lcs;
    //Jaccard similarity score
    private final double ic;
    private final double simJ;
    private final double score;

    public PhenotypeMatch(Builder builder) {
        this.matchPhenotype = builder.matchPhenotype;
        this.queryPhenotype = builder.queryPhenotype;
        this.ic = builder.ic;
        this.lcs = builder.lcs;
        this.simJ = builder.simJ;
        this.score = builder.score;
    }

    @JsonIgnore
    public String getQueryPhenotypeId() {
        return (queryPhenotype == null) ? "null" : queryPhenotype.getId();
    }
    
    @JsonProperty("query")
    public PhenotypeTerm getQueryPhenotype() {
        return queryPhenotype;
    }

    @JsonIgnore
    public String getMatchPhenotypeId() {
        return (matchPhenotype == null) ? "null" : matchPhenotype.getId();
    }
    
    @JsonProperty("match")
    public PhenotypeTerm getMatchPhenotype() {
        return matchPhenotype;
    }

    @JsonProperty("lcs")
    public PhenotypeTerm getLcs() {
        return lcs;
    }

    @JsonProperty("ic")
    public double getIc() {
        return ic;
    }

    @JsonProperty("simj")
    public double getSimJ() {
        return simJ;
    }

    @JsonProperty("score")
    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhenotypeMatch)) return false;
        PhenotypeMatch that = (PhenotypeMatch) o;
        return Double.compare(that.ic, ic) == 0 &&
                Double.compare(that.simJ, simJ) == 0 &&
                Double.compare(that.score, score) == 0 &&
                Objects.equals(matchPhenotype, that.matchPhenotype) &&
                Objects.equals(queryPhenotype, that.queryPhenotype) &&
                Objects.equals(lcs, that.lcs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchPhenotype, queryPhenotype, lcs, ic, simJ, score);
    }

    @Override
    public String toString() {
        return "PhenotypeMatch{" + "matchPhenotype=" + matchPhenotype + ", queryPhenotype=" + queryPhenotype + ", lcs=" + lcs + ", simJ=" + simJ + ", score=" + score + '}';
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
            return new PhenotypeMatch(this);
        }
    }
}
