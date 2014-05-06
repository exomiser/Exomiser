/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceGroup;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class StringResourceGroupParser implements ResourceGroupParser {

    private static final Logger logger = LoggerFactory.getLogger(StringResourceGroupParser.class);
    
    private Resource entrezResource;
    private Resource stringResource;
    
    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {
 
        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);
        
        entrezResource = resourceGroup.getResource(EntrezParser.class);
        stringResource = resourceGroup.getResource(StringParser.class);

        if (requiredResourcesValid()) {
            HashMap<String, List<Integer>> ensembl2EntrezGene = new HashMap<>();
            //first parseResource the entrez gene to symbol and ensembl peptide biomart file
            EntrezParser entrezParser = new EntrezParser(ensembl2EntrezGene);
            entrezParser.parseResource(entrezResource, inDir, outDir);

            //now parseResource the STRING DB file
            StringParser stringParser = new StringParser(ensembl2EntrezGene);
            stringParser.parseResource(stringResource, inDir, outDir);
        }
    }

    private boolean requiredResourcesValid() {
        
        if (entrezResource == null) {
            logger.info("Unable to parse STRING resources as the resource required by the {} is null", EntrezParser.class);
        }
        
        if (stringResource == null) {
            logger.info("Unable to parse STRING resources as the resource required by the {} is null", StringParser.class);
        }
        
        return (entrezResource != null && stringResource != null);
    }

    
}
