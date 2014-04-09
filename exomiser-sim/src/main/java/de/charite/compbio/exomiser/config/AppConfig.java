/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.config;

import de.charite.compbio.exomiser.sim.App;
import de.charite.compbio.exomiser.sim.ExomeSimulator;
import javax.sql.DataSource;
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
@PropertySource("classpath:jdbc.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource() {
        System.out.println("Making a new DataSource");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("exomiser.driverClassName"));
        dataSource.setUrl(env.getProperty("exomiser.url"));
        dataSource.setUsername(env.getProperty("exomiser.username"));
        dataSource.setPassword(env.getProperty("exomiser.password"));
        System.out.println("Returning a new DataSource to URL " + dataSource.getUrl());
        return dataSource;
    }

    @Bean
    ExomeSimulator exomeSimulator(){
        return new ExomeSimulator();
    }
    
//    @Bean
//    App app(){
//        return new App();
//    }
}
