/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.DiseaseModel;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import java.util.List;

/**
 * Interface for Services retrieving model data.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ModelService {

    public List<Model> getHumanDiseaseModels();

    public List<Model> getMouseGeneModels();

    public List<Model> getFishGeneModels();
    
    
}
