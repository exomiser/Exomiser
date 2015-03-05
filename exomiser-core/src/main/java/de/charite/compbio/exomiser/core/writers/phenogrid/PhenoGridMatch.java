/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.List;
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

    PhenoGridMatch(String id, String label, String type, List<PhenotypeMatch> phenotypeMatches, PhenoGridMatchScore score, PhenoGridMatchTaxon taxon) {
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
        Set<String> queryTermIds = new TreeSet();
        for (PhenotypeMatch phenotypeMatch : matches) {
            PhenotypeTerm queryPhenotype = phenotypeMatch.getQueryPhenotype();
            queryTermIds.add(queryPhenotype.getId());
        }
        return queryTermIds;
    }

}
