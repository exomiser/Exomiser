/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Data transport object for representing all gene/disease phenotype matches for
 * an organism to a set of query phenotype ids.
 *
 * For example: 
 * http://www.monarchinitiative.org/simsearch/phenotype?input_items=HP:0000218,HP:0000238,HP:0000244,HP:0000303,HP:0000316,HP:0000327,HP:0000486,HP:0000494,HP:0000586,HP:0000678,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003070,HP:0005347,HP:0006101,HP:0010055,HP:0011304,HP:0005280,HP:0003795,HP:0004440,HP:0000452,HP:0000453,HP:0001159,HP:0003041,HP:0003196,HP:0006110&target_species=9606&limit=10
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchGroup {

    private final List<PhenoGridMatch> matches;
    private final Set<String> queryPhenotypeTermIds;
    //what's this cutoff for? the IC? 
    private static final int CUTOFF = 10;

    public PhenoGridMatchGroup(List<PhenoGridMatch> matches, Collection<String> queryPhenotypeTermIds) {
        this.matches = matches;
        this.queryPhenotypeTermIds = new TreeSet<>(queryPhenotypeTermIds);
    }

    @JsonProperty("b")
    public List<PhenoGridMatch> getMatches() {
        return matches;
    }

    @JsonProperty("a")
    public Set<String> getQueryPhenotypeTermIds() {
        return queryPhenotypeTermIds;
    }

    @JsonProperty("cutoff")
    public int getCutOff() {
        return CUTOFF;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.matches);
        hash = 17 * hash + Objects.hashCode(this.queryPhenotypeTermIds);
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
        final PhenoGridMatchGroup other = (PhenoGridMatchGroup) obj;
        if (!Objects.equals(this.matches, other.matches)) {
            return false;
        }
        return Objects.equals(this.queryPhenotypeTermIds, other.queryPhenotypeTermIds);
    }

    @Override
    public String toString() {
        return "PhenoGridMatchGroup{" + "matches=" + matches + ", queryPhenotypeTermIds=" + queryPhenotypeTermIds + '}';
    }

}
