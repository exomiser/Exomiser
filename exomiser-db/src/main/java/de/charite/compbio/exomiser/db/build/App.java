package de.charite.compbio.exomiser.db.build;

import com.googlecode.flyway.core.Flyway;
import de.charite.compbio.exomiser.config.AppConfig;
import de.charite.compbio.exomiser.config.DataSourceConfig;
import de.charite.compbio.exomiser.config.ResourceConfig;
import de.charite.compbio.exomiser.io.PhenodigmDataDumper;
import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceDownloadHandler;
import de.charite.compbio.exomiser.resources.ResourceExtractionHandler;
import de.charite.compbio.exomiser.resources.ResourceParserHandler;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main class for building the exomiser database. This will attempt to download 
 * and process the resources specified in {@code  de.charite.compbio.exomiser.config.ResourceConfig}.
 *
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        //Get Spring to sort it's shit out... 
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class, ResourceConfig.class, DataSourceConfig.class);
        //create variables which would otherwise be injected manually from the context
        DataSourceConfig dataSourceConfig = context.getBean(DataSourceConfig.class);
        
        DataSource h2DataSource = dataSourceConfig.exomiserH2DataSource();
        DataSource postGresDataSource = dataSourceConfig.exomiserPostgresDataSource();
        
        AppConfig appConfig = context.getBean(AppConfig.class);
        //set the Paths
        Path dataPath = appConfig.dataPath();
        Path downloadPath = appConfig.downloadPath();

        //Get the Resources from the ResourceConfiguration 
        ResourceConfig resourceConfig = context.getBean(ResourceConfig.class); 
        
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
        Path proccessPath = appConfig.getProcessPath();

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
        
        //dump Phenodigm data to flatfiles for import
        boolean dumpPhenoDigmData = appConfig.dumpPhenoDigmData();
        if (dumpPhenoDigmData) {
            logger.info("Making Phenodigm data dump files...");
            PhenodigmDataDumper phenoDumper = context.getBean(PhenodigmDataDumper.class);
            phenoDumper.dumpPhenodigmData(dataPath);
        } else {
            logger.info("Skipping making Phenodigm data dump files.");
        }

        //now load the database using Flyway we're going to create an enbedded H2
        //and a PostgreSQL database. TODO: This should be parallelizable.
        boolean migrateDatabases = appConfig.migrateDatabases();
        if (migrateDatabases) {
            logger.info("Migrating exomiser databases...");
            //define where the data import path is otherwise everyrhing will fail
            Map<String, String> propertyPlaceHolders = new HashMap<>();
            propertyPlaceHolders.put("import.path", dataPath.toString());
            
            // Do the PostgreSQL migration            
            logger.info("Migrating exomiser PostgreSQL database...");
            Flyway postgresqlFlyway = new Flyway();
            postgresqlFlyway.setDataSource(postGresDataSource);
            postgresqlFlyway.setSchemas("exomiser");
            postgresqlFlyway.setLocations("db/migration/common", "db/migration/postgres");
            postgresqlFlyway.setPlaceholders(propertyPlaceHolders);
            postgresqlFlyway.clean();
            postgresqlFlyway.migrate();
            
            //Do the H2 migration 
            logger.info("Migrating exomiser H2 database...");
            Flyway h2Flyway = new Flyway();
            h2Flyway.setDataSource(h2DataSource);
            h2Flyway.setSchemas("EXOMISER");
            h2Flyway.setLocations("db/migration/common", "db/migration/h2");
            h2Flyway.setPlaceholders(propertyPlaceHolders);
            h2Flyway.clean();
            h2Flyway.migrate();

        } else {
            logger.info("Skipping migration of database.");
        }
    }
}
