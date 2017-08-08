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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Data transport object for outputting phenotype match data to a disease or
 * gene phenotype.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatch {

    private final String id;
    private final String label;
    private final String type;

    private final List<PhenotypeMatch> matches;
    private final PhenoGridMatchScore score;
    private final PhenoGridMatchTaxon taxon;

    public PhenoGridMatch(String id, String label, String type, List<PhenotypeMatch> phenotypeMatches, PhenoGridMatchScore score, PhenoGridMatchTaxon taxon) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.matches = phenotypeMatches;
        this.score = score;
        this.taxon = taxon;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public List<PhenotypeMatch> getMatches() {
        return matches;
    }

    public PhenoGridMatchScore getScore() {
        return score;
    }

    public PhenoGridMatchTaxon getTaxon() {
        return taxon;
    }

    @JsonIgnore
    public Set<String> getQueryTermIds() {
        Set<String> queryTermIds = new TreeSet<>();
        for (PhenotypeMatch phenotypeMatch : matches) {
            PhenotypeTerm queryPhenotype = phenotypeMatch.getQueryPhenotype();
            queryTermIds.add(queryPhenotype.getId());
        }
        return queryTermIds;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.label);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.matches);
        hash = 53 * hash + Objects.hashCode(this.score);
        hash = 53 * hash + Objects.hashCode(this.taxon);
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
        final PhenoGridMatch other = (PhenoGridMatch) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.matches, other.matches)) {
            return false;
        }
        if (!Objects.equals(this.score, other.score)) {
            return false;
        }
        return Objects.equals(this.taxon, other.taxon);
    }

    @Override
    public String toString() {
        return "PhenoGridMatch{" + "id=" + id + ", label=" + label + ", type=" + type + ", #matches=" + matches.size() + ", score=" + score + ", taxon=" + taxon + '}';
    }

}
