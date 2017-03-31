/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.service;

import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.Set;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OntologyService {

    Set<PhenotypeTerm> getHpoTerms();
    
    Set<PhenotypeTerm> getMpoTerms();
    
    Set<PhenotypeTerm> getZpoTerms();

    Set<PhenotypeMatch> getHpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    Set<PhenotypeMatch> getMpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);
    
    Set<PhenotypeMatch> getZpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    PhenotypeTerm getPhenotypeTermForHpoId(String hpoId);
}
