/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.Main;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
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
    Environment env;
    
    @Autowired
    Path dataPath;
    
    @Bean
    String h2Path(){
        return dataPath.resolve("exomiser").toString();
    }
        
    /**
     * Used to find the Path the Main application is running on in order to
     * pick-up the user-configured properties files.
     *
     * @return
     */
    @Bean
    public static Path mainJarPath() {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();

        Path jarFilePath = null;
        try {
            jarFilePath = Paths.get(codeSource.getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            logger.error("Unable to find jar file", ex);
        }
        logger.info("Jar file is running from location: {}", jarFilePath);
        return jarFilePath;
    }
    
    @Bean
    public DataSource dataSource() {
        logger.info("h2Path: {}", h2Path());
        //this is a shitty hack to get the database url to work without configuration
        //but my Spring-Fu is weak to I can't get the bugger to do a dynamic placeholder resolve
        //of ${h2Path} 
        String url = env.getProperty("exomiser.url").replace("$h2Path", h2Path());
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
//        Path jdbcPropertiesPath = jarPath.resolve("jdbc.properties");
//        Resource[] resources = new PathResource[]{
//                new PathResource(jdbcPropertiesPath)};//,
////                new PathResource(applicationPropertiesPath)};
//        pspc.setLocations(resources);
//        pspc.setIgnoreUnresolvablePlaceholders(true);
//        return pspc;
//    }
}
