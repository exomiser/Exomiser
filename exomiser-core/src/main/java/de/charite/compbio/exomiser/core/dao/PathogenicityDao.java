/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import jannovar.exome.Variant;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PathogenicityDao {

    PathogenicityData getPathogenicityData(Variant variant);
    
}
