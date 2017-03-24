/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers.phenodigm.dao;

import org.monarchinitiative.exomiser.core.prioritisers.phenodigm.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.phenodigm.PhenotypeTerm;

import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OntologyDao {
    
    Set<PhenotypeTerm> getAllTerms();
    
    Set<PhenotypeMatch> getPhenotypeMatchesForHpoTerm(PhenotypeTerm hpoTerm);
}
