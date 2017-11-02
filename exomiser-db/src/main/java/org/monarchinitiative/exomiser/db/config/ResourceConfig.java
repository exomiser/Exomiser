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

package org.monarchinitiative.exomiser.db.config;

import org.monarchinitiative.exomiser.db.parsers.*;
import org.monarchinitiative.exomiser.db.resources.Resource;
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
        //Regulatory features
        resources.add(fantomEnhancerResource());
        resources.add(ensemblEnhancerResource());
        resources.add(clinVarResource());
        resources.add(metaDataResource());

        //these ones are biggies:
        resources.add(dbNsfpResource());
        //VariantFrequency group
        resources.add(dbSnpResource());
        resources.add(espResource());
        resources.add(jannovarResource());
        resources.add(exacResource());

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
    public Resource dbNsfpResource() {
        logger.info("Making dbNSFP resource");
        Resource resource = new Resource("dbNSFP");
        populateResourceFromProperty("nsfp", resource);
        //parsing
        resource.setParserClass(NSFP2SQLDumpParser.class);
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource dbSnpResource() {
        logger.info("Making dbSNP resource");
        Resource resource = new Resource("dbSNP");
        populateResourceFromProperty("dbSnp", resource);
        //parsing
        resource.setParserClass(DbSnpFrequencyParser.class);
        //resource groups
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource exacResource() {
        logger.info("Making ExAC resource");
        Resource resource = new Resource("ExAC");
        populateResourceFromProperty("exac", resource);
        //parsing
        resource.setParserClass(ExACFrequencyParser.class);
        //resource groups
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource espResource() {
        logger.info("Making ESP resource");
        Resource resource = new Resource("ESP");
        populateResourceFromProperty("esp", resource);
        //parsing
        resource.setParserClass(EspFrequencyParser.class);
        //resource groups
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource jannovarResource() {
        logger.info("Making UCSC_HG19 resource");
        Resource resource = new Resource("UCSC_HG19");
        populateResourceFromProperty("ucsc", resource);
        //
        resource.setParserClass(null);
        //
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);

        return resource;
    }

    @Bean
    public Resource fantomEnhancerResource() {
        logger.info("Making FANTOM enhancer resource");
        Resource resource = new Resource("FANTOM_enhancers");
        populateResourceFromProperty("fantom", resource);

        //
        resource.setParserClass(FantomEnhancerParser.class);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource ensemblEnhancerResource() {
        logger.info("Making Ensembl enhancer resource");
        Resource resource = new Resource("Ensembl_enhancers");
        populateResourceFromProperty("ensembl_enhancers", resource);

        //
        resource.setParserClass(EnsemblEnhancerParser.class);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);

        return resource;
    }

    @Bean
    public Resource clinVarResource() {
        logger.info("Making ClinVar resource");
        Resource resource = new Resource("ClinVar");
        populateResourceFromProperty("clinvar", resource);
        //
        resource.setParserClass(ClinVarParser.class);
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
