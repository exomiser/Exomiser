/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli.config;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.postgresql.ds.PGPoolingDataSource;
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
@PropertySource({"jdbc.properties", "file:${jarFilePath}/jdbc.properties"})
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment env;
    
    @Autowired
    private Path dataPath;
                          
    @Bean
    public DataSource dataSource() {
        String url = env.getProperty("exomiser.url");
        if (env.containsProperty("h2Path") &! env.getProperty("h2Path").isEmpty()) {
            env.resolvePlaceholders("h2Path"); //this comes from the application.properties
//            url = env.getProperty("exomiser.url");
        }
        else {
            //so it hasn't been manually set we'll use the default location
            //the placeholders are not visible in the url string hence we replace the 'file:'
            String h2Filepath = String.format("file:%s", dataPath);
            url = env.getProperty("exomiser.url").replace("file:", h2Filepath);
        }
        logger.info("DataSource url set to: {}", url);
        int maxConnections = 10; //maybe get this to be user accessible 
        logger.info("DataSource using maximum of {} database connections", maxConnections);        
        String user = env.getProperty("exomiser.username");
        String password = env.getProperty("exomiser.password");
        if (env.getProperty("exomiser.driverClassName").equals("org.postgresql.Driver")){
            PGPoolingDataSource dataSource = new PGPoolingDataSource();
            dataSource.setMaxConnections(maxConnections);
            String server = env.getProperty("exomiser.server");
            String db = env.getProperty("exomiser.database");
            int port = Integer.parseInt(env.getProperty("exomiser.port"));
            dataSource.setServerName(server);
            dataSource.setDatabaseName(db);
            dataSource.setPortNumber(port);
            dataSource.setUser(user);
            dataSource.setPassword(password);
            logger.info("Returning a new DataSource to URL {} user: {}", url, user);
            return dataSource;
        }
        else{
            JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, user, password);
            dataSource.setMaxConnections(maxConnections);
            logger.info("Returning a new DataSource to URL {} user: {}", url, user);
            return dataSource;
        }
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

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
//        pspc.setLocation(new PathResource("jdbc.properties"));
//        return pspc;
//    }
}
