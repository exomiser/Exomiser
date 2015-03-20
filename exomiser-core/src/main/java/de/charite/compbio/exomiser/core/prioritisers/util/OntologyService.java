/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.List;
import java.util.Set;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OntologyService {

    public Set<PhenotypeTerm> getHpoTerms();
    
    public Set<PhenotypeTerm> getMpoTerms();
    
    public Set<PhenotypeTerm> getZpoTerms();
    
    public List<String> getHpoIdsForDiseaseId(String diseaseId);

    public Set<PhenotypeMatch> getHpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    public Set<PhenotypeMatch> getMpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);
    
    public Set<PhenotypeMatch> getZpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    public PhenotypeTerm getPhenotypeTermForHpoId(String hpoId);
}
