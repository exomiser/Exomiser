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

package org.monarchinitiative.exomiser.data.phenotype;

import org.flywaydb.core.Flyway;
import org.monarchinitiative.exomiser.data.phenotype.config.AppConfig;
import org.monarchinitiative.exomiser.data.phenotype.config.DataSourceConfig;
import org.monarchinitiative.exomiser.data.phenotype.config.ResourceConfig;
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceDownloadHandler;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceExtractionHandler;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Main class for building the exomiser database. This will attempt to download
 * and process the resources specified in the app.properties file.
 * {@code  org.monarchinitiative.exomiser.config.ResourceConfig}.
 */
@Component
public class Main implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private final AppConfig appConfig;
    private final ResourceConfig resourceConfig;
    private final DataSourceConfig dataSourceConfig;

    public Main(AppConfig appConfig, ResourceConfig resourceConfig, DataSourceConfig dataSourceConfig) {
        this.appConfig = appConfig;
        this.resourceConfig = resourceConfig;
        this.dataSourceConfig = dataSourceConfig;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        //set the Paths
        Path dataPath = appConfig.dataPath();
        Path downloadPath = appConfig.downloadPath();

        Set<Resource> externalResources = resourceConfig.resources();

        //Download the Resources
        boolean downloadResources = appConfig.downloadResources();
        if (downloadResources) {
            //download and unzip the necessary input files
            logger.info("Downloading required flatfiles...");
            ResourceDownloadHandler.downloadResources(externalResources, downloadPath);
        } else {
            logger.info("Skipping download of external resource files.");
        }
        //Path for processing the downloaded files to prepare them for parsing (i.e. unzip, untar)
        Path proccessPath = appConfig.processPath();

        //Extract the Resources
        boolean extractResources = appConfig.extractResources();
        if (extractResources) {
            //process the downloaded files to prepare them for parsing (i.e. unzip, untar)
            logger.info("Extracting required flatfiles...");
            ResourceExtractionHandler.extractResources(externalResources, downloadPath, proccessPath);
        } else {
            logger.info("Skipping extraction of external resource files.");
        }

        //Parse the Resources
        boolean parseResources = appConfig.parseResources();
        if (parseResources) {
            //parse the file and output to the project output dir.
            logger.info("Parsing resource files...");
            ResourceParserHandler.parseResources(externalResources, proccessPath, dataPath);

        } else {
            logger.info("Skipping parsing of external resource files.");
        }

        logger.info("Statuses for external resources:");
        for (Resource resource : externalResources) {
            logger.info(resource.getStatus());
        }

        logger.info("Migrating exomiser databases...");
        //define where the data import path is otherwise everything will fail
        Map<String, String> propertyPlaceHolders = new HashMap<>();
        propertyPlaceHolders.put("import.path", dataPath.toString());


        boolean migrateH2 = appConfig.migrateH2();
        if (migrateH2) {
            DataSource h2DataSource = dataSourceConfig.exomiserH2DataSource();
            migrateH2Database(h2DataSource, propertyPlaceHolders);
        } else {
            logger.info("Skipping migration of H2 database.");
        }
    }

    private static void migrateH2Database(DataSource h2DataSource, Map<String, String> propertyPlaceHolders) {
        logger.info("Migrating exomiser H2 database...");
        Flyway h2Flyway = new Flyway();
        h2Flyway.setDataSource(h2DataSource);
        h2Flyway.setSchemas("EXOMISER");
        h2Flyway.setLocations("migration/common", "migration/h2");
        h2Flyway.setPlaceholders(propertyPlaceHolders);
        h2Flyway.clean();
        h2Flyway.migrate();
    }
}
