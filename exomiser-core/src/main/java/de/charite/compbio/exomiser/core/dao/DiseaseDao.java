/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Disease;
import de.charite.compbio.exomiser.core.model.DiseaseIdentifier;
import de.charite.compbio.exomiser.core.model.GeneIdentifier;
import java.util.Set;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface DiseaseDao {
    
    public Set<Disease> getAllDiseases();
    
    public Disease getDisease(DiseaseIdentifier diseaseId);

    public Set<Disease> getKnownDiseasesForGene(GeneIdentifier geneId);

}
