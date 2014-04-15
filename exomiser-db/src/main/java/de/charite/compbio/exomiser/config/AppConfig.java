/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.config;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import de.charite.compbio.exomiser.io.PhenodigmDataDumper;
import de.charite.compbio.exomiser.resources.ExternalResource;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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
 * Provides the JDBC datasource from the jdbc.properties file located in the
 * classpath.
 *
 * @author Jules Jacobsen (jules.jacobsen@sanger.ac.uk)
 */
@Configuration
@PropertySource({"classpath:jdbc.properties", "classpath:external-resources.yml", "classpath:app.properties"})
public class AppConfig {

    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    Environment env;
    
    @Bean(name = "exomiserH2DataSource")
    public DataSource exomiserH2DataSource() {
//        System.out.println("Making a new DataSource");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("exomiser.h2.driverClassName"));
        dataSource.setUrl(env.getProperty("exomiser.h2.url"));
        dataSource.setUsername(env.getProperty("exomiser.h2.username"));
        dataSource.setPassword(env.getProperty("exomiser.h2.password"));
        logger.info("Returning a new DataSource to URL " + dataSource.getUrl() + " username: " + env.getProperty("exomiser.h2.username"));
        return dataSource;
    }

    @Bean(name = "exomiserPostgresDataSource")
    public DataSource exomiserPostgresDataSource() {
//        System.out.println("Making a new DataSource");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("exomiser.postgres.driverClassName"));
        dataSource.setUrl(env.getProperty("exomiser.postgres.url"));
        dataSource.setUsername(env.getProperty("exomiser.postgres.username"));
        dataSource.setPassword(env.getProperty("exomiser.postgres.password"));
        logger.info("Returning a new DataSource to URL " + dataSource.getUrl() + " username: " + env.getProperty("exomiser.postgres.username"));
        return dataSource;
    }

    @Bean(name = "phenodigmDataSource")
    public DataSource phenodigmDataSource() {
//        System.out.println("Making a new DataSource");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("phenodigm.driverClassName"));
        dataSource.setUrl(env.getProperty("phenodigm.url"));
        dataSource.setUsername(env.getProperty("phenodigm.username"));
        dataSource.setPassword(env.getProperty("phenodigm.password"));
        logger.info("Returning a new DataSource to URL " + dataSource.getUrl() + " username: " + env.getProperty("phenodigm.username"));
        return dataSource;
    }

    @Bean(name = "externalResources")
    public Set<ExternalResource> externalResources() {
        Set<ExternalResource> externalResources = new LinkedHashSet<>();
        try {
            //parse yaml file here
            YamlReader reader = new YamlReader(new FileReader("src/main/resources/external-resources.yml"));
            reader.getConfig().setClassTag("resource", ExternalResource.class);
            Map<String, List<ExternalResource>> resources = (Map<String, List<ExternalResource>>) reader.read();

            List<ExternalResource> externalResourse = resources.get("External resources");

            externalResources.addAll(externalResourse);
//            for (ExternalResource externalResource : externalResources) {
//                logger.info("{}", externalResource);
//            }
            reader.close();
        } catch (FileNotFoundException | YamlException ex) {
            logger.error(null, ex);
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        return externalResources;
    }

    @Bean(name = "phenodigmDataDumper")
    public PhenodigmDataDumper phenodigmDataDumper() {
        return new PhenodigmDataDumper(this.phenodigmDataSource());
    }

    @Bean(name = "dataPath")
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("data.path"));
        logger.info("Root data working directory set to: {}", dataPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (dataPath.toFile().mkdir()) {
            logger.info("Made new data directory: {}", dataPath.toAbsolutePath());
        }
        return dataPath;
    }

    @Bean(name = "downloadPath")
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
        Path downloadPath = downloadPath();

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
        Path downloadPath = downloadPath();

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
    public Path processPath() {
        Path processPath = Paths.get(dataPath().toString(), env.getProperty("process.path"));
        logger.info("Data process directory set to: {}", processPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (processPath.toFile().mkdir()) {
            logger.info("Made new process directory: {}", processPath.toAbsolutePath());
        }
        return processPath;
    }

//    @Bean(name = "ucscFileName")
//    public String ucscFileName() {
//        String ucscFileName = env.getProperty("ucscFileName");
//        logger.info("ucscFileName set to: {}", ucscFileName);
//        return ucscFileName;
//    }
}
