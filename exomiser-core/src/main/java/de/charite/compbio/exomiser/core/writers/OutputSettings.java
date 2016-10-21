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

    static OutputSettingsImp.OutputSettingsBuilder builder() {
        return OutputSettingsImp.builder();
    }
    
    boolean outputPassVariantsOnly();

    int getNumberOfGenesToShow();

    Set<OutputFormat> getOutputFormats();

    String getOutputPrefix();

}
