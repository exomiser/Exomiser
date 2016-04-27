/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides the JDBC datasource from the jdbc.properties file located in the
 * classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:jdbc.properties"})
public class DataSourceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
    
    @Autowired
    private Environment env;
    
    @Autowired
    private Path dataPath;
    
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        HikariDataSource dataSource;
        if (env.getProperty("usePostgreSQL").equals("true")) {
            dataSource = new HikariDataSource(postgreSQLConfig());
        }
        else {
            dataSource = new HikariDataSource(h2Config());
        }
        dataSource.setMaximumPoolSize(maxConnections());

        logger.info("DataSource using maximum of {} database connections", dataSource.getMaximumPoolSize());
        logger.info("Returning a new {} DataSource pool to URL {} user: {}", dataSource.getPoolName(), dataSource.getJdbcUrl(), dataSource.getUsername());
        return dataSource;
    }
    
    private int maxConnections() {
        int maxConnections = 10;
        String userSpecifiedMaxConn = env.getProperty("maxConnections");
        try {
            maxConnections = Integer.parseInt(userSpecifiedMaxConn);
        } catch (NumberFormatException ex) {
            logger.error("{} is not a valid integer value. Returning default value of {}", userSpecifiedMaxConn, maxConnections, ex);
        }
        return maxConnections;
    }
    
    private HikariConfig postgreSQLConfig() {

        //resolve the placeholders in the jdbc.properties using the user-supplied data from application.properties
        env.resolvePlaceholders("dbuser");
        env.resolvePlaceholders("password");
        env.resolvePlaceholders("server");
        env.resolvePlaceholders("port");
        env.resolvePlaceholders("database");
        
        //read in the properties from jdbc.properties
        String user = env.getProperty("pg.username");
        String password = env.getProperty("pg.password");
        String server = env.getProperty("pg.server");
        String port = env.getProperty("pg.port");
        String db = env.getProperty("pg.database");
            
        String url = String.format("jdbc:pgsql://%s:%s/%s", server, port, db);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("exomiser-PostgreSQL");

        return config;
    }
    
    private HikariConfig h2Config() {
        
        String user = env.getProperty("h2.username");
        String password = env.getProperty("h2.password");
        String url = env.getProperty("h2.url");
        
        if (env.containsProperty("h2Path") & !env.getProperty("h2Path").isEmpty()) {
            logger.info("Using user defined H2 path: {}", env.getProperty("h2Path"));
            env.resolvePlaceholders("h2Path"); //this comes from the application.properties
        } else {
            logger.info("H2 path not set. Using default data path: {}", dataPath);
            //in this case it hasn't been manually set, so we'll use the default location
            //the placeholders are not visible in the url string hence we replace the 'file:'
            String h2Filepath = String.format("file:%s", dataPath);
            url = env.getProperty("h2.url").replace("file:", h2Filepath);
        }
        
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("exomiser-H2");

        return config;
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
