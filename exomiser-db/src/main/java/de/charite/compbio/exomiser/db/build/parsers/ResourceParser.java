/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.parsers;

import de.charite.compbio.exomiser.db.build.resources.Resource;
import java.nio.file.Path;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResourceParser {
    
    public void parseResource(Resource resource, Path inDir, Path outDir);

}
