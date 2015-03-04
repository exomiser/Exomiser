/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.annotation.JsonGetter;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatch {

    private final String id;
    private final String label;
    private final String type;

    private final List<PhenotypeMatch> matches;

    //score
    //taxon
    //metadata
    //query term ids
    PhenoGridMatch(String id, String label, String type, List<PhenotypeMatch> phenotypeMatches) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.matches = phenotypeMatches;
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
    
    
}
