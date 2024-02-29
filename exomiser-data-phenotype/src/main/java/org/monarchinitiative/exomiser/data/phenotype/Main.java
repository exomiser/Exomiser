/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.data.phenotype.config.ApplicationConfigurationProperties;
import org.monarchinitiative.exomiser.data.phenotype.config.ReleaseFileSystem;
import org.monarchinitiative.exomiser.data.phenotype.processors.ResourceChecker;
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.DiseaseProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.GeneProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.OntologyProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.ProcessingGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Main class for building the exomiser database. This will attempt to download
 * and process the resources specified in the app.properties file.
 * {@code  org.monarchinitiative.exomiser.config.ResourceConfig}.
 */
@Component
public class Main implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private final ApplicationConfigurationProperties applicationConfigurationProperties;
    private final ReleaseFileSystem releaseFileSystem;

    private final DiseaseProcessingGroup diseaseProcessingGroup;
    private final GeneProcessingGroup geneProcessingGroup;
    private final OntologyProcessingGroup ontologyProcessingGroup;

    private final Flyway flyway;

    public Main(ApplicationConfigurationProperties applicationConfigurationProperties,
                ReleaseFileSystem releaseFileSystem,
                DiseaseProcessingGroup diseaseProcessingGroup,
                GeneProcessingGroup geneProcessingGroup,
                OntologyProcessingGroup ontologyProcessingGroup,
                Flyway flyway
    ) {
        this.applicationConfigurationProperties = applicationConfigurationProperties;
        this.releaseFileSystem = releaseFileSystem;
        this.diseaseProcessingGroup = diseaseProcessingGroup;
        this.geneProcessingGroup = geneProcessingGroup;
        this.ontologyProcessingGroup = ontologyProcessingGroup;
        this.flyway = flyway;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        logger.info("Building Exomiser {}_phenotype database...", applicationConfigurationProperties.getBuildVersion());

        List<ProcessingGroup> processingGroups = List.of(diseaseProcessingGroup, geneProcessingGroup, ontologyProcessingGroup);
        for (ProcessingGroup processingGroup : processingGroups) {
            String processingGroupName = processingGroup.getName();
            logger.info("==== Starting processing group: {} ====", processingGroupName);
            if (applicationConfigurationProperties.isDownloadResources()) {
                logger.info("Downloading {} required flatfiles...", processingGroupName);
                processingGroup.downloadResources();
            } else {
                logger.info("Skipping download of {} external resource files.", processingGroupName);
            }
            if (applicationConfigurationProperties.isProcessResources()) {
                ResourceChecker resourceChecker = ResourceChecker.check(processingGroup.getResources());
                if (resourceChecker.resourcesPresent()) {
                    logger.info("Processing resource {} files...", processingGroupName);
                    processingGroup.processResources();
                } else {
                    logger.error("{} unable to run due to missing resource(s):", processingGroupName);
                    resourceChecker.getMissingResources()
                            .forEach(externalResource -> logger.error("{} MISSING RESOURCE: {}", processingGroupName, externalResource.getResourcePath()));
                    throw new IllegalStateException("Missing resource(s) in group " + processingGroupName);
                }
            } else {
                logger.info("Skipping processing of {} external resource files.", processingGroupName);
            }
        }

        boolean migrateH2 = applicationConfigurationProperties.isMigrateDatabase();
        if (migrateH2) {
            logger.info("Migrating exomiser databases...");
            flyway.clean();
            flyway.migrate();
            // shutdown and compact the database. This will reduce the file size by about 3GB-4GB (50%)
            try {
                DataSource datasource = flyway.getConfiguration().getDataSource();
                Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                statement.execute("SHUTDOWN COMPACT");
            } catch (Exception e) {
                logger.error("", e);
            }
        } else {
            logger.info("Skipping migration of H2 database.");
        }
    }
}
