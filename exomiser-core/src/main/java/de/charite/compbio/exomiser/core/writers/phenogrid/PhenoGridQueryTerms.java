/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridQueryTerms {
    
    private final String id;
    private final Set<String> phenotypeIds;

    public PhenoGridQueryTerms(String id, Set<String> phenotypeIds) {
        this.id = id;
        this.phenotypeIds = phenotypeIds;
    }

    public String getId() {
        return id;
    }

    @JsonProperty("phenotype_list")
    public Set<String> getPhenotypeIds() {
        return phenotypeIds;
    }  
    
}
