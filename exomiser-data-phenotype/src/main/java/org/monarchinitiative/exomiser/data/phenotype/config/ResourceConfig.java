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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.monarchinitiative.exomiser.data.phenotype.parsers.*;
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Java configuration for providing {@code Resource} definitions for the
 * application to work on.
 * <p>
 * If you want to add another data source, this is where you define the resource
 * and the application will pick it up and incorporate the data into the database.
 * Obviously you'll have to write a parser and a flyway migration to get the data
 * into the database.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:resource.properties"})
public class ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(ResourceConfig.class);

    @Autowired
    Environment env;

    @Bean
    public Set<Resource> resources() {
        logger.info("Making new set of Resources");
        Set<Resource> resources = new LinkedHashSet<>();
//            resources.add(exampleResource());
        resources.add(orphanetResource());
        resources.add(hpoResource());
        //OMIM group
        resources.add(omimMimToGeneResource());
        resources.add(omimMorbidMapResource());
        resources.add(hpoPhenotypeAnnotationsResource());

        resources.add(entrezToSymResource());
        //Exome Walker
        resources.add(exomeWalkerPhenotypicSeriesResource());
        resources.add(exomeWalkerOmimToGeneResource());

        return resources;

    }

    @Bean
    public Resource exampleResource() {
        logger.info("Making example resource");
        Resource resource = new Resource("Example");
        resource.setUrl("");
        resource.setRemoteFileName("");
        resource.setVersion("");
        resource.setExtractedFileName("");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    //reads properties from resource.properties for a given resource and populates the resource variables
    private void populateResourceFromProperty(String resourcePropertyId, Resource resource) {
        resource.setUrl(env.getProperty(resourcePropertyId + ".url"));
        resource.setRemoteFileName(env.getProperty(resourcePropertyId + ".remoteFile"));
        resource.setVersion(env.getProperty(resourcePropertyId + ".version"));
        resource.setExtractedFileName(env.getProperty(resourcePropertyId + ".extractedName"));
        resource.setExtractionScheme(env.getProperty(resourcePropertyId + ".extractScheme"));
        resource.setParsedFileName(env.getProperty(resourcePropertyId + ".parsedName"));
    }

    @Bean
    public Resource hpoResource() {
        logger.info("Making HPO resource");
        Resource resource = new Resource("HPO");
        populateResourceFromProperty("hpo", resource);
        //parsing
        resource.setParserClass(HPOOntologyFileParser.class);
        //resource groups
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource orphanetResource() {
        logger.info("Making Orphanet resource");
        Resource resource = new Resource("Orphanet");
        populateResourceFromProperty("orphanet", resource);
        //parsing
        resource.setParserClass(Orphanet2GeneParser.class);
        //resource groups
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }



    @Bean
    public Resource omimMimToGeneResource() {
        logger.info("Making OMIM_mim2gene resource");
        Resource resource = new Resource("OMIM_mim2gene");
        populateResourceFromProperty("omim2gene", resource);
        //
        resource.setParserClass(MimToGeneParser.class);
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource omimMorbidMapResource() {
        logger.info("Making OMIM_morbidmap resource");
        Resource resource = new Resource("OMIM_morbidmap");
        populateResourceFromProperty("morbidmap", resource);
        //
        resource.setParserClass(MorbidMapParser.class);
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource hpoPhenotypeAnnotationsResource() {
        logger.info("Making HPO_phenotype_annotations resource");
        Resource resource = new Resource("HPO_phenotype_annotations");
        populateResourceFromProperty("omimpheno", resource);
        //
        resource.setParserClass(DiseaseInheritanceCache.class);
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource entrezToSymResource() {
        logger.info("Making entrez2sym resource");
        Resource resource = new Resource("entrez2sym");
        populateResourceFromProperty("string2entrez", resource);
        //
        resource.setParserClass(EntrezParser.class);
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource exomeWalkerPhenotypicSeriesResource() {
        logger.info("Making ExomeWalker_phenotypic_series resource");
        Resource resource = new Resource("ExomeWalker_phenotypic_series");
        populateResourceFromProperty("walkerpheno", resource);

        //
        resource.setParserClass(PhenoSeriesParser.class);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);

        return resource;
    }



    @Bean
    public Resource exomeWalkerOmimToGeneResource() {
        logger.info("Making ExomeWalker_omim2gene resource");
        Resource resource = new Resource("ExomeWalker_omim2gene");
        populateResourceFromProperty("walkergene", resource);
        //
        resource.setParserClass(Omim2GeneParser.class);
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource metaDataResource() {
        logger.info("Making MetaData resource");
        Resource resource = new Resource("MetaData");
        resource.setUrl("");
        resource.setRemoteFileName("");
        resource.setVersion("");
        resource.setExtractedFileName("");
        resource.setExtractionScheme("");
        resource.setParsedFileName("metadata.pg");
        //
        resource.setParserClass(MetaDataParser.class);
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

}
