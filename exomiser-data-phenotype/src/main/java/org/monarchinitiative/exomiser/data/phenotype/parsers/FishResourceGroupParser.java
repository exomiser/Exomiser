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

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class FishResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "OMIM";

    private static final Logger logger = LoggerFactory.getLogger(FishResourceGroupParser.class);

    private Resource fishPhenotypeResource;
    private Resource fishOrthologResource;
    private Resource fishGeneLabelResource;

    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }

        Map<String, String> fishId2Symbol = new HashMap<>();
        FishGeneLabelParser fishGeneLabelParser = new FishGeneLabelParser(fishId2Symbol);
        fishGeneLabelParser.parseResource(fishGeneLabelResource,inDir,outDir);

        FishPhenotypeParser fishPhenotypeParser = new FishPhenotypeParser(fishId2Symbol);
        fishPhenotypeParser.parseResource(fishPhenotypeResource,inDir,outDir);

        FishOrthologParser fishOrthologParser = new FishOrthologParser();
        fishOrthologParser.parseResource(fishOrthologResource,inDir,outDir);

    }

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        fishPhenotypeResource = resourceGroup.getResource(FishPhenotypeParser.class);
        if (fishPhenotypeResource == null) {
            logResourceMissing(resourceGroup.getName(), FishPhenotypeParser.class);
            return false;
        }
        fishOrthologResource = resourceGroup.getResource(FishOrthologParser.class);
        if (fishOrthologResource == null) {
            logResourceMissing(resourceGroup.getName(), FishOrthologParser.class);
            return false;
        }
        fishGeneLabelResource = resourceGroup.getResource(FishGeneLabelParser.class);
        if (fishGeneLabelResource == null) {
            logResourceMissing(resourceGroup.getName(), FishGeneLabelParser.class);
            return false;
        }

        return true;
    }
}
