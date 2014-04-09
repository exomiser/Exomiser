package de.charite.compbio.exomiser.db.build;

import com.googlecode.flyway.core.Flyway;
import de.charite.compbio.exomiser.config.AppConfig;
import de.charite.compbio.exomiser.resources.ExternalResource;
import de.charite.compbio.exomiser.io.PhenodigmDataDumper;
import de.charite.compbio.exomiser.resources.ResourceDownloadHandler;
import de.charite.compbio.exomiser.resources.ResourceExtractionHandler;
import de.charite.compbio.exomiser.resources.ResourceParserHandler;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        //Get Spring to sort it's shit out... 
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        //create variables which would otherwise be injected manually from the context
        DataSource h2DataSource = (DataSource) context.getBean("exomiserH2DataSource");
        DataSource postGresDataSource = (DataSource) context.getBean("exomiserPostgresDataSource");
        Set<ExternalResource> externalResources = (Set<ExternalResource>) context.getBean("externalResources");
        Path dataPath = (Path) context.getBean("dataPath");
        String ucscFileName = (String) context.getBean("ucscFileName");
                
        Path downloadPath = Paths.get(dataPath.toString() + "/download");

        if (downloadPath.toFile().mkdir()) {
            logger.info("Made new data download directory: {}", downloadPath.toAbsolutePath());
        }
        //TODO: get from properties
        boolean downloadExternalResources = false;
        if (downloadExternalResources) {
            //download and unzip the necessary input files
            logger.info("Downloading required flatfiles...");
            ResourceDownloadHandler.downloadResources(externalResources, downloadPath);
        } else {
            logger.info("Skipping download of external resource files.");
        }
        //Path for processing the downloaded files to prepare them for parsing (i.e. unzip, untar)
        Path proccessPath = Paths.get(dataPath.toString() + "/extracted");
        if (proccessPath.toFile().mkdir()) {
            logger.info("Made new processed data directory: {}", proccessPath.toAbsolutePath());
        }

        //TODO: get from properties
        boolean extractExternalResources = false;
        if (extractExternalResources) {
            //process the downloaded files to prepare them for parsing (i.e. unzip, untar)
            logger.info("Extracting required flatfiles...");
            ResourceExtractionHandler.extractResources(externalResources, downloadPath, proccessPath);
        } else {
            logger.info("Skipping extraction of external resource files.");
        }

        //TODO: get from properties
        boolean parseExternalResources = true;
        if (parseExternalResources) {
            //first we need to prepare the serialized ucsc18 data file using Jannovar
            //this is required for parsing the dbSNP data where it is used as a filter to 
            // remove variants outside of exonic regions.
            File ucscSerializedData = new File(proccessPath.toFile(), ucscFileName);
            if (!ucscSerializedData.exists()) {
                logger.warn("UCSC serialized data file is not present in the process path. Please add it here: {}", ucscSerializedData.getPath());
                //no useable API for Jannovar so we have to add it manually 
            }
            
            //parse the file and output to the project output dir.
            logger.info("Parsing files for db dump...");            
            ResourceParserHandler.parseResources(externalResources, ucscSerializedData, proccessPath, dataPath);
            
        } else {
            logger.info("Skipping parsing of external resource files.");
        }

        logger.info("Statuses for external resources:");
        for (ExternalResource resource : externalResources) {
            logger.info(resource.getStatus());
        }
        //dump Phenodigm data to flatfiles for import
        //TODO: get from properties
        boolean dumpPhenoDigmData = false;
        if (dumpPhenoDigmData) {
            PhenodigmDataDumper.dumpPhenodigmData(dataPath);
        } else {
            logger.info("Skipping making Phenodigm data dump files.");
        }

        //now load the database using Flyway we're going to create an enbedded H2
        //and a PostgreSQL database. TODO: This should be parallelizable.
        boolean migrateDatabases = false;
        if (migrateDatabases) {
            logger.info("Migrating exomiser databases...");
            // Create the Flyway instance
            logger.info("Migrating exomiser PostgreSQL database...");
            Flyway postgresqlFlyway = new Flyway();
            // Point it to the database
            postgresqlFlyway.setDataSource(postGresDataSource);
            postgresqlFlyway.setSchemas("EXOMISER");
            postgresqlFlyway.setLocations("db/migration/common", "db/migration/postgres");
            Map<String, String> propertyPlaceHolders = new HashMap<>();
            propertyPlaceHolders.put("import.path", dataPath.toString());
            postgresqlFlyway.setPlaceholders(propertyPlaceHolders);
            // Start the migration (will import files from the data dir)
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
