/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class StringResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "STRING";

    private static final Logger logger = LoggerFactory.getLogger(StringResourceGroupParser.class);

    private Resource entrezResource;
    private Resource stringResource;

    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }

        Map<String, List<Integer>> ensembl2EntrezGene = new HashMap<>();
        //first parseResource the entrez gene to symbol and ensembl peptide biomart file
        EntrezParser entrezParser = new EntrezParser(ensembl2EntrezGene);
        entrezParser.parseResource(entrezResource, inDir, outDir);

        //now parseResource the STRING DB file
        StringParser stringParser = new StringParser(ensembl2EntrezGene);
        stringParser.parseResource(stringResource, inDir, outDir);

    }

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {

        entrezResource = resourceGroup.getResource(EntrezParser.class);
        if (entrezResource == null) {
            logResourceMissing(resourceGroup.getName(), EntrezParser.class);
            return false;
        }

        stringResource = resourceGroup.getResource(StringParser.class);
        if (stringResource == null) {
            logResourceMissing(resourceGroup.getName(), StringParser.class);
            return false;
        }

        return true;
    }
}
