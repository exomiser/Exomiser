/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.io.FileOperationStatus;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Parser {
    
    public FileOperationStatus parse(String inPath, String outPath);

}
