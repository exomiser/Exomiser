/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.cli.config;

import org.monarchinitiative.exomiser.autoconfigure.UndefinedDataDirectoryException;
import org.monarchinitiative.exomiser.cli.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Provides configuration details from the application.properties file located in
 * the jarfile directory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class MainConfig {

    private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

    /**
     * Used to find the Path the Main application is running on in order to
     * pick-up the user-configured properties files.
     *
     * @return
     */
    @Bean
    public Path exomiserHome() {
        ApplicationHome home = new ApplicationHome(Main.class);
        logger.info("Exomiser home: {}", home.getDir());
        return home.getDir().toPath();
    }

    @Bean
    public Path resultsDir(Path exomiserHome) {
        //TODO: get this from env i.e. exomiser.properties? Will help with server too
        Path defaultOutputDir = exomiserHome.resolve("results");
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
    public Path exomiserDataDirectory(Path exomiserHome, Environment env) {
        String dataDirValue = env.getProperty("exomiser.data-directory");
        if (dataDirValue == null || dataDirValue.isEmpty()) {
            return findDefaultDataDir(exomiserHome);
        }
        logger.info("Data source directory defined in properties as: {}", dataDirValue);
        Path dataPath = exomiserHome.resolve(dataDirValue).toAbsolutePath();
        logger.info("Root data source directory set to: {}", dataPath);
        return dataPath;
    }

    private Path findDefaultDataDir(Path exomiserHome) {
        logger.info("Exomiser data directory not defined in properties. Checking for default...");
        Path dataPath = exomiserHome.resolve("data").toAbsolutePath();
        if (dataPath.toFile().exists()) {
            logger.info("Found default data directory: {}", dataPath);
            return dataPath;
        }
        throw new UndefinedDataDirectoryException("Please provide a valid path for the exomiser.data-directory");
    }

}
