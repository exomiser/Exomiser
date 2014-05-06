/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceGroup;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OmimResourceGroupParser implements ResourceGroupParser {

    private static final Logger logger = LoggerFactory.getLogger(OmimResourceGroupParser.class);
    
    private Resource morbidMapResource;
    private Resource mim2geneResource;
    private Resource hpoPhenotypeAnnotations;
    
    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {
        
        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);
        
        morbidMapResource = resourceGroup.getResource(MorbidMapParser.class);
        mim2geneResource = resourceGroup.getResource(MimToGeneParser.class);
        hpoPhenotypeAnnotations = resourceGroup.getResource(DiseaseInheritanceCache.class);

        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        //first parseResource the mim2gene file
        MimToGeneParser mimParser = new MimToGeneParser(mim2geneMap);
        mimParser.parseResource(mim2geneResource, inDir, outDir);

        //Need to make the cache for the morbidmap resourceParser
        DiseaseInheritanceCache diseaseInheritanceCache = new DiseaseInheritanceCache();
        diseaseInheritanceCache.parseResource(hpoPhenotypeAnnotations, inDir, outDir);
        if (!diseaseInheritanceCache.isEmpty()) {
            hpoPhenotypeAnnotations.setParseStatus(ResourceOperationStatus.SUCCESS);
        }
        //make the MimList which morbid map will populate
        MorbidMapParser morbidParser = new MorbidMapParser(diseaseInheritanceCache, mim2geneMap);
        morbidParser.parseResource(morbidMapResource, inDir, outDir);
    }
}
