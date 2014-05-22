/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.dao;

import de.charite.compbio.exomiser.filter.Triage;
import jannovar.exome.Variant;

/**
 * Interface for DAO classes returning a {@code de.charite.compbio.exomiser.filter.Triage}
 * object.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface TriageDAO {
    
    public Triage getTriageData(Variant variant);
    
}
