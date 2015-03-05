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
    
    public PhenotypeMatch(PhenotypeTerm queryPhenotype, PhenotypeTerm matchPhenotype, double simJ, PhenotypeTerm lcs) {
        this.queryPhenotype = queryPhenotype;
        this.matchPhenotype = matchPhenotype;
        this.simJ = simJ;
        this.lcs = lcs;
    }

    @JsonProperty("a")
    public PhenotypeTerm getQueryPhenotype() {
        return queryPhenotype;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.simJ) ^ (Double.doubleToLongBits(this.simJ) >>> 32));
        hash = 53 * hash + Objects.hashCode(this.lcs);
        hash = 53 * hash + Objects.hashCode(this.matchPhenotype);
        hash = 53 * hash + Objects.hashCode(this.queryPhenotype);
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
        if (Double.doubleToLongBits(this.simJ) != Double.doubleToLongBits(other.simJ)) {
            return false;
        }
        if (!Objects.equals(this.lcs, other.lcs)) {
            return false;
        }
        if (!Objects.equals(this.matchPhenotype, other.matchPhenotype)) {
            return false;
        }
        if (!Objects.equals(this.queryPhenotype, other.queryPhenotype)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhenotypeMatch{" + "simJ=" + simJ + ", lcs=" + lcs + ", matchPhenotype=" + matchPhenotype + ", queryPhenotype=" + queryPhenotype + '}';
    }
    
}
