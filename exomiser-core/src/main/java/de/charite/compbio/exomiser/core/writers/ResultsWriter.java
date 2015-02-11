/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.ExomiserSettings;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResultsWriter {
    
    /**
     * Writes the result data out to the file specified in the ExomiserSettings object.
     * 
     * @param sampleData
     * @param settings
     */
    public void writeFile(SampleData sampleData, ExomiserSettings settings);
    
    /**
     * Writes the result data out to a String.
     * 
     * @param sampleData
     * @param settings
     * @return  
     */
    public String writeString(SampleData sampleData, ExomiserSettings settings);
}
