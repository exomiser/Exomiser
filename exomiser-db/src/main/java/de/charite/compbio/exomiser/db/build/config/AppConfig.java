/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.db.build.config;

import de.charite.compbio.exomiser.db.build.io.PhenodigmDataDumper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Provides configuration details from the app.properties file located in the
 * classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:app.properties"})
public class AppConfig {

    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    Environment env;

    @Bean
    public PhenodigmDataDumper phenodigmDataDumper() {
        return new PhenodigmDataDumper();
    }

    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("data.path"));
        logger.info("Root data working directory set to: {}", dataPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (dataPath.toFile().mkdir()) {
            logger.info("Made new data directory: {}", dataPath.toAbsolutePath());
        }
        return dataPath;
    }

    @Bean
    public Path downloadPath() {
        Path downloadPath = Paths.get(dataPath().toString(), env.getProperty("download.path"));
        logger.info("Data download directory set to: {}", downloadPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (downloadPath.toFile().mkdir()) {
            logger.info("Made new download directory: {}", downloadPath.toAbsolutePath());
        }
        return downloadPath;
    }

    /**
     * Dirty hack to copy the static data into the data directory.
     * @return 
     */
    @Bean
    public boolean copyPheno2GeneResource() {

        String resource = "src/main/resources/data/pheno2gene.txt";
        try {
            Files.copy(Paths.get(resource), downloadPath().resolve("pheno2gene.txt"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied {} to {}", resource, downloadPath());
        } catch (IOException ex) {
            logger.error("Unable to copy resource {}", resource, ex);
        }
        return true;
    }
    
    /**
     * Dirty hack to copy the static data into the data directory.
     * @return 
     */
    @Bean
    public boolean copyUcscHg19Resource() {
        
        String resource = "src/main/resources/data/ucsc_hg19.ser.gz";
        try {
            Files.copy(Paths.get(resource), downloadPath().resolve("ucsc_hg19.ser.gz"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied {} to {}", resource, downloadPath());
        } catch (IOException ex) {
            logger.error("Unable to copy resource {}", resource, ex);
        }
        return true;
    }

    @Bean(name = "processPath")
    public Path getProcessPath() {
        Path processPath = dataPath().resolve(env.getProperty("process.path"));
        logger.info("Data process directory set to: {}", processPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (processPath.toFile().mkdir()) {
            logger.info("Made new process directory: {}", processPath.toAbsolutePath());
        }
        return processPath;
    }

    @Bean
    public boolean downloadResources() {
        boolean download = Boolean.parseBoolean(env.getProperty("downloadResources")); 
        logger.info("Setting application to download resources: {}", download);
        return download;
    }

    @Bean
    public boolean extractResources() {
        boolean extract = Boolean.parseBoolean(env.getProperty("extractResources")); 
        logger.info("Setting application to extract resources: {}", extract);
        return extract;    
    }

    @Bean
    public boolean parseResources() {
        boolean parse = Boolean.parseBoolean(env.getProperty("parseResources")); 
        logger.info("Setting application to parse resources: {}", parse);
        return parse;    
    }

    @Bean
    public boolean dumpPhenoDigmData() {
        boolean dumpPhenoDigmData = Boolean.parseBoolean(env.getProperty("dumpPhenoDigmData")); 
        logger.info("Setting application to dump PhenoDigm data: {}", dumpPhenoDigmData);
        return dumpPhenoDigmData;    
    }

    @Bean
    public boolean migratePostgres() {
        boolean migratePostgres = Boolean.parseBoolean(env.getProperty("migratePostgres")); 
        logger.info("Setting application to migrate PostgreSQL database: {}", migratePostgres);
        return migratePostgres;    
    }

    @Bean
    public boolean migrateH2() {
        boolean migrateH2 = Boolean.parseBoolean(env.getProperty("migrateH2")); 
        logger.info("Setting application to migrate H2 database: {}", migrateH2);
        return migrateH2;    
    }
}
