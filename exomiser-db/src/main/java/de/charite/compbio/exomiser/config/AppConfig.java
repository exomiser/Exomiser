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
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Provides the JDBC datasource from the jdbc.properties file located in the
 * classpath.
 * 
 * @author Jules Jacobsen (jules.jacobsen@sanger.ac.uk)
 */
@Configuration
@PropertySource({"classpath:jdbc.properties","classpath:external-resources.yml","classpath:app.properties"})
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
        logger.info("Data working directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }
    
    @Bean(name = "ucscFileName")
    public String ucscFileName() {
        String ucscFileName = env.getProperty("ucscFileName");
        logger.info("ucscFileName set to: {}", ucscFileName);
        return ucscFileName;
    }    
}
