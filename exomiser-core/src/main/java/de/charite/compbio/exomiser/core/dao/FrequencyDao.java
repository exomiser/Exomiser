/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface FrequencyDao {

    FrequencyData getFrequencyData(Variant variant);
    
}
