/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

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
    public DataSource dataSource() {
//        System.out.println("Making a new DataSource");
        String url = env.getProperty("exomiser.url");
        String user = env.getProperty("exomiser.username");
        String password = env.getProperty("exomiser.password");
        
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, user, password);

        logger.info("Returning a new DataSource to URL {} user: {}", url, user);
        return dataSource;
    }

    @Bean
    public Connection connection() {
        Connection connection = null;
        try {
            connection = dataSource().getConnection();
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        return connection;
    }

}
