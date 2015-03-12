/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Contains information about how well a pair of <code>PhenotypeTerm</code> 
 * match each other.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeMatch {
    
    private final PhenotypeTerm matchPhenotype;
    private final PhenotypeTerm queryPhenotype;
    //lowest common subsumer
    private final PhenotypeTerm lcs;
    //Jaccard similarity score
    private final double simJ;
    private final double score;
    
    public PhenotypeMatch(PhenotypeTerm queryPhenotype, PhenotypeTerm matchPhenotype, double simJ, double score, PhenotypeTerm lcs) {
        this.queryPhenotype = queryPhenotype;
        this.matchPhenotype = matchPhenotype;
        this.simJ = simJ;
        this.lcs = lcs;
        this.score = score;
    }

    @JsonIgnore
    public String getQueryPhenotypeId() {
        return (queryPhenotype == null) ? "null" : queryPhenotype.getId();
    }
    
    @JsonProperty("a")
    public PhenotypeTerm getQueryPhenotype() {
        return queryPhenotype;
    }

    @JsonIgnore
    public String getMatchPhenotypeId() {
        return (matchPhenotype == null) ? "null" : matchPhenotype.getId();
    }
    
    @JsonProperty("b")
    public PhenotypeTerm getMatchPhenotype() {
        return matchPhenotype;
    }

    @JsonProperty("lcs")
    public PhenotypeTerm getLcs() {
        return lcs;
    }

    @JsonIgnore
    public double getSimJ() {
        return simJ;
    }

    @JsonIgnore
    public double getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.matchPhenotype);
        hash = 17 * hash + Objects.hashCode(this.queryPhenotype);
        hash = 17 * hash + Objects.hashCode(this.lcs);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.simJ) ^ (Double.doubleToLongBits(this.simJ) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhenotypeMatch other = (PhenotypeMatch) obj;
        if (!Objects.equals(this.matchPhenotype, other.matchPhenotype)) {
            return false;
        }
        if (!Objects.equals(this.queryPhenotype, other.queryPhenotype)) {
            return false;
        }
        if (!Objects.equals(this.lcs, other.lcs)) {
            return false;
        }
        if (Double.doubleToLongBits(this.simJ) != Double.doubleToLongBits(other.simJ)) {
            return false;
        }
        if (Double.doubleToLongBits(this.score) != Double.doubleToLongBits(other.score)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhenotypeMatch{" + "matchPhenotype=" + matchPhenotype + ", queryPhenotype=" + queryPhenotype + ", lcs=" + lcs + ", simJ=" + simJ + ", score=" + score + '}';
    }
    
}
