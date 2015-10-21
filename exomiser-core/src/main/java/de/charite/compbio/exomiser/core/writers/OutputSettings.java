/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OutputSettings {
    
    public boolean outputPassVariantsOnly();

    public int getNumberOfGenesToShow();

    public Set<OutputFormat> getOutputFormats();

    public String getOutputPrefix();

}
