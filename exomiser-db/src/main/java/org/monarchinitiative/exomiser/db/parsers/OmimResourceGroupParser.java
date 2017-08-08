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

package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceGroup;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class OmimResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "OMIM";

    private static final Logger logger = LoggerFactory.getLogger(OmimResourceGroupParser.class);

    private Resource morbidMapResource;
    private Resource mim2geneResource;
    private Resource hpoPhenotypeAnnotations;

    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }

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

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        morbidMapResource = resourceGroup.getResource(MorbidMapParser.class);
        if (morbidMapResource == null) {
            logResourceMissing(resourceGroup.getName(), MorbidMapParser.class);
            return false;
        }

        mim2geneResource = resourceGroup.getResource(MimToGeneParser.class);
        if (mim2geneResource == null) {
            logResourceMissing(resourceGroup.getName(), MimToGeneParser.class);
            return false;
        }

        hpoPhenotypeAnnotations = resourceGroup.getResource(DiseaseInheritanceCache.class);
        if (hpoPhenotypeAnnotations == null) {
            logResourceMissing(resourceGroup.getName(), DiseaseInheritanceCache.class);
            return false;
        }

        return true;
    }
}
