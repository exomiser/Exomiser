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
public class OntologyResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "ONTOLOGYPARSING";

    private static final Logger logger = LoggerFactory.getLogger(OntologyResourceGroupParser.class);

    private Resource hpResource;
    private Resource mpResource;
    private Resource zpResource;
    private Resource hpHpResource;
    private Resource hpMpResource;
    private Resource hpZpResource;

    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }

        Map<String, String> hpId2termMap = new HashMap<>();
        HPOOntologyFileParser hpoOntologyFileParser = new HPOOntologyFileParser(hpId2termMap);
        hpoOntologyFileParser.parseResource(hpResource,inDir,outDir);

        HPHPMapperParser hphpMapperParser = new HPHPMapperParser(hpId2termMap);
        hphpMapperParser.parseResource(hpHpResource,inDir,outDir);

        Map<String, String> mpId2termMap = new HashMap<>();
        MPOntologyFileParser mpOntologyFileParser = new MPOntologyFileParser(mpId2termMap);
        mpOntologyFileParser.parseResource(mpResource,inDir,outDir);

        HPMPMapperParser hpmpMapperParser  = new HPMPMapperParser(hpId2termMap,mpId2termMap);
        hpmpMapperParser.parseResource(hpMpResource, inDir,outDir);

        Map<String, String> zpId2termMap = new HashMap<>();
        ZPOntologyFileParser zpOntologyFileParser = new ZPOntologyFileParser(zpId2termMap);
        zpOntologyFileParser.parseResource(zpResource,inDir,outDir);

        HPZPMapperParser hpzpMapperParser = new HPZPMapperParser(hpId2termMap,zpId2termMap);
        hpzpMapperParser.parseResource(hpZpResource,inDir,outDir);

    }

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        hpResource = resourceGroup.getResource(HPOOntologyFileParser.class);
        if (hpResource == null) {
            logResourceMissing(resourceGroup.getName(), HPOOntologyFileParser.class);
            return false;
        }

        mpResource = resourceGroup.getResource(MPOntologyFileParser.class);
        if (mpResource == null) {
            logResourceMissing(resourceGroup.getName(), MPOntologyFileParser.class);
            return false;
        }

        zpResource = resourceGroup.getResource(ZPOntologyFileParser.class);
        if (zpResource == null) {
            logResourceMissing(resourceGroup.getName(), ZPOntologyFileParser.class);
            return false;
        }

        hpHpResource = resourceGroup.getResource(HPHPMapperParser.class);
        if (hpHpResource == null){
            logResourceMissing(resourceGroup.getName(), HPHPMapperParser.class);
            return false;
        }

        hpMpResource = resourceGroup.getResource(HPMPMapperParser.class);
        if (hpMpResource == null){
            logResourceMissing(resourceGroup.getName(), HPMPMapperParser.class);
            return false;
        }

        hpZpResource = resourceGroup.getResource(HPZPMapperParser.class);
        if (hpHpResource == null){
            logResourceMissing(resourceGroup.getName(), HPZPMapperParser.class);
            return false;
        }

        return true;
    }
}
