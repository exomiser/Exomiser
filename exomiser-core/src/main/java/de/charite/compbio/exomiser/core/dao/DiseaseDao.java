/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface DiseaseDao {
        
    public Set<String> getHpoIdsForDiseaseId(String diseaseId);
    
    public Map<String, String> getDiseaseIdToTerms();

}
