/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Used for storing results of a phenotypic analysis of a sample against a set
 * of input phenotypes. This is used to output json for use by the Monarch
 * Phenogrid JavaScript widget.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGrid {

    private final PhenoGridQueryTerms phenoGridQueryTerms;

    private final List<PhenoGridMatchGroup> phenoGridMatchGroups;

    public PhenoGrid(PhenoGridQueryTerms phenoGridQueryTerms, List<PhenoGridMatchGroup> phenoGridMatchGroups) {
        this.phenoGridQueryTerms = phenoGridQueryTerms;
        this.phenoGridMatchGroups = phenoGridMatchGroups;
    }

    @JsonProperty("input_terms")
    public PhenoGridQueryTerms getPhenoGridQueryTerms() {
        return phenoGridQueryTerms;
    }

    @JsonProperty("matches")
    public List<PhenoGridMatchGroup> getPhenoGridMatchGroups() {
        return phenoGridMatchGroups;
    }

}
