/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.config;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import de.charite.compbio.exomiser.io.PhenodigmDataDumper;
import de.charite.compbio.exomiser.resources.Resource;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
    public Path getDataPath() {
        Path dataPath = Paths.get(env.getProperty("data.path"));
        logger.info("Root data working directory set to: {}", dataPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (dataPath.toFile().mkdir()) {
            logger.info("Made new data directory: {}", dataPath.toAbsolutePath());
        }
        return dataPath;
    }

    @Bean
    public Path getDownloadPath() {
        Path downloadPath = Paths.get(getDataPath().toString(), env.getProperty("download.path"));
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
        Path downloadPath = getDownloadPath();

        String resource = "src/main/resources/data/pheno2gene.txt";
        try {
            Files.copy(Paths.get(resource), Paths.get(downloadPath.toString(), "pheno2gene.txt"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied {} to {}", resource, downloadPath);
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
        Path downloadPath = getDownloadPath();

        String resource = "src/main/resources/data/ucsc_hg19.ser.gz";
        try {
            Files.copy(Paths.get(resource), Paths.get(downloadPath.toString(), "ucsc_hg19.ser.gz"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied {} to {}", resource, downloadPath);
        } catch (IOException ex) {
            logger.error("Unable to copy resource {}", resource, ex);
        }
        return true;
    }

    @Bean(name = "processPath")
    public Path getProcessPath() {
        Path processPath = Paths.get(getDataPath().toString(), env.getProperty("process.path"));
        logger.info("Data process directory set to: {}", processPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (processPath.toFile().mkdir()) {
            logger.info("Made new process directory: {}", processPath.toAbsolutePath());
        }
        return processPath;
    }
}
