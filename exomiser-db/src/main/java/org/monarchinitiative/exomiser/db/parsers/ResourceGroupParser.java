/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.resources.ResourceGroup;

import java.nio.file.Path;

/**
 * Interface defining the functionality for how a group of parsers should work in concert
 * in order to produce the output file(s).
 * 
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResourceGroupParser {
   
    /**
     * Parses the {@code org.monarchinitiative.exomiser.resources.Resource} contained in the
     * {@code org.monarchinitiative.exomiser.resources.ResourceGroup} according to
     * the internal rules of the {@cadede.charite.compbio.exomiser.parsers.ResourceGroupParser}
     * implementation.
     * 
     * @param resourceGroup
     * @param inDir
     * @param outDir 
     */
    void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir);
    
    /**
     * Checks that all the required resources for the ResourceGroupParser are present. 
     * @param resourceGroup
     * @return false if any resource is missing.
     */
    boolean requiredResourcesPresent(ResourceGroup resourceGroup);
}
