/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import java.util.List;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OntologyService {

    public List<String> getHpoIdsForDiseaseId(String diseaseId);

}
