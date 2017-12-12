/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class MouseResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "OMIM";

    private static final Logger logger = LoggerFactory.getLogger(MouseResourceGroupParser.class);

    private Resource mgiPhenotypeResource;
    private Resource impcPhenotypeResource;
    private Resource mouseHomoloGeneOrthologResource;
    private Resource mouseEnsemblOthologResource;

    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup
                    .getName());
            return;
        }

        Map<String, String> mouseId2Symbol = new HashMap<>();
        Map<String, String> humanId2Symbol = new HashMap<>();
        Map<String, Set<String>> mouse2human = new HashMap<>();

        MouseHomoloGeneOrthologParser mouseHomoloGeneOrthologParser = new MouseHomoloGeneOrthologParser(mouseId2Symbol, humanId2Symbol, mouse2human);
        mouseHomoloGeneOrthologParser.parseResource(mouseHomoloGeneOrthologResource, inDir, outDir);
        MouseEnsemblOrthologParser mouseEnsemblGeneOrthologParser = new MouseEnsemblOrthologParser(mouseId2Symbol, humanId2Symbol, mouse2human);
        mouseEnsemblGeneOrthologParser.parseResource(mouseEnsemblOthologResource, inDir, outDir);

        Map<String, String> mouse2PhenotypesMap = new HashMap<>();
        Map<String, String> mouse2geneMap = new HashMap<>();
        MGIPhenotypeParser mgiPhenotypeParser = new MGIPhenotypeParser(mouse2PhenotypesMap, mouse2geneMap);
        mgiPhenotypeParser.parseResource(mgiPhenotypeResource, inDir, outDir);
        IMPCPhenotypeParser impcPhenotypeParser = new IMPCPhenotypeParser(mouse2PhenotypesMap, mouse2geneMap, mouseId2Symbol);
        impcPhenotypeParser.parseResource(impcPhenotypeResource, inDir, outDir);

    }

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        mgiPhenotypeResource = resourceGroup.getResource(MGIPhenotypeParser.class);
        if (mgiPhenotypeResource == null) {
            logResourceMissing(resourceGroup.getName(), MGIPhenotypeParser.class);
            return false;
        }
        impcPhenotypeResource = resourceGroup.getResource(IMPCPhenotypeParser.class);
        if (impcPhenotypeResource == null) {
            logResourceMissing(resourceGroup.getName(), IMPCPhenotypeParser.class);
            return false;
        }
        mouseHomoloGeneOrthologResource = resourceGroup.getResource(MouseHomoloGeneOrthologParser.class);
        if (mouseHomoloGeneOrthologResource == null) {
            logResourceMissing(resourceGroup.getName(), MouseHomoloGeneOrthologParser.class);
            return false;
        }
        mouseEnsemblOthologResource = resourceGroup.getResource(MouseEnsemblOrthologParser.class);
        if (mouseEnsemblOthologResource == null) {
            logResourceMissing(resourceGroup.getName(), MouseEnsemblOrthologParser.class);
            return false;
        }

        return true;
    }
}
