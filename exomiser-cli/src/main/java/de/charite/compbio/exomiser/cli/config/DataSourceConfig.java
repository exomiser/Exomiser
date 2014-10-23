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
@PropertySource({"jdbc.properties"})
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment env;

    @Autowired
    private Path dataPath;

    @Bean
    public DataSource dataSource() {
        if (env.getProperty("usePostgreSQL").equals("true")) {
            return postgreSQLDataSource();
        }
        return h2DataSource();
    }

    private int maxConnections() {
        int maxConnections = 10;
        String userSpecifiedMaxConn = env.getProperty("maxConnections");
        try {
            maxConnections = Integer.parseInt(userSpecifiedMaxConn);
        } catch (NumberFormatException ex) {
            logger.error("{} is not a valid integer value. Returning defualt value of {}", userSpecifiedMaxConn, maxConnections, ex);
        }
        return maxConnections;
    }

    private DataSource postgreSQLDataSource() {
        int maxConnections = maxConnections();
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setMaxConnections(maxConnections);
        logger.info("DataSource using maximum of {} database connections", maxConnections);
        dataSource.setInitialConnections(3);

        //resolve the placeholders in the jdbc.properties using the user-supplied data from application.properties
        env.resolvePlaceholders("dbuser");
        env.resolvePlaceholders("password");
        env.resolvePlaceholders("server");
        env.resolvePlaceholders("database");
        env.resolvePlaceholders("port");

        //read in the properties from jdbc.properties
        String user = env.getProperty("pg.username");
        String password = env.getProperty("pg.password");
        String server = env.getProperty("pg.server");
        String db = env.getProperty("pg.database");
        int port = Integer.parseInt(env.getProperty("pg.port"));
        
        dataSource.setServerName(server);
        dataSource.setDatabaseName(db);
        dataSource.setPortNumber(port);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        String url = String.format("jdbc:postgresql://%s:%d/%s", server, port, db);

        logger.info("Returning a new PostgreSQL DataSource to URL {} user: {}", url, user);
        return dataSource;
    }

    private DataSource h2DataSource() {

        String user = env.getProperty("h2.username");
        String password = env.getProperty("h2.password");
        String url = env.getProperty("h2.url");

        if (env.containsProperty("h2Path") & !env.getProperty("h2Path").isEmpty()) {
            env.resolvePlaceholders("h2Path"); //this comes from the application.properties
        } else {
            //in this case it hasn't been manually set, so we'll use the default location
            //the placeholders are not visible in the url string hence we replace the 'file:'
            String h2Filepath = String.format("file:%s", dataPath);
            url = env.getProperty("h2.url").replace("file:", h2Filepath);
        }
        int maxConnections = maxConnections();
        logger.info("DataSource using maximum of {} database connections", maxConnections);
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, user, password);
        dataSource.setMaxConnections(maxConnections);

        logger.info("Returning a new H2 DataSource to URL {} user: {}", url, user);
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
