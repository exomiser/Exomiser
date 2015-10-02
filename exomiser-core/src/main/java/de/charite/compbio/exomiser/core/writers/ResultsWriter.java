/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.analysis.Analysis;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResultsWriter {
    
    /**
     * Writes the result data out to the file specified in the ExomiserSettings object.
     * 
     * @param analysis
     * @param settings
     */
    public void writeFile(Analysis analysis, OutputSettings settings);
       
    /**
     * Writes the result data out to a String.
     * 
     * @param analysis
     * @param settings
     * @return  
     */
    public String writeString(Analysis analysis, OutputSettings settings);
}
