/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.config;

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
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:jdbc.properties"})
public class DataSourceConfig {

    private final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    Environment env;
    
    @Bean
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

    @Bean
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

    @Bean
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
}
