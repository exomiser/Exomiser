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

package org.monarchinitiative.exomiser.cli.config;

import org.monarchinitiative.exomiser.autoconfigure.EnableExomiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Provides configuration details from the application.properties file located in
 * the jarfile directory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@ComponentScan(basePackages = {"org.monarchinitiative.exomiser.cli"})
@PropertySource("file:${jarFilePath}/application.properties")
@EnableExomiser
public class MainConfig {

    private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

    @Autowired
    private Environment env;

    /**
     * Used to find the Path the Main application is running on in order to
     * pick-up the user-configured properties files.
     *
     * @return
     */
    @Bean
    public Path jarFilePath() {
        Path jarFilePath = Paths.get(env.getProperty("jarFilePath"));
        logger.info("Jar file is running from location: {}", jarFilePath);
        return jarFilePath;
    }

    @Bean
    public Path resultsDir(Path jarFilePath) {
        //TODO: get this from env i.e. exomiser.properties? Will help with server too
        Path defaultOutputDir = jarFilePath.resolve("results");
        try {
            if (!defaultOutputDir.toFile().exists()) {
                Files.createDirectory(defaultOutputDir);
            }
        } catch (IOException ex) {
            logger.error("Unable to create default output directory for results {}", defaultOutputDir, ex);
        }
        logger.info("Default results directory set to: {}", defaultOutputDir.toAbsolutePath());
        return defaultOutputDir;
    }

    @Bean
    public Path exomiserDataDirectory(Path jarFilePath) {
        String dataDirValue = env.getProperty("exomiser.data-directory");
        logger.info("Data source directory defined in properties as: {}", dataDirValue);
        Path dataPath = jarFilePath.resolve(dataDirValue);
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }

    @Bean
    public Resource ehCacheConfig(Path jarFilePath) {
      return new PathResource(jarFilePath.resolve("ehcache.xml"));
    }

    @Bean
    public String banner() {
        Resource banner = new ClassPathResource("banner.txt");
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = banner.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException ex) {
            logger.error("Error reading banner.txt {}", ex);
        }
        return stringBuilder.toString();
    }

}
