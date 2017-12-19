/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base configuration class for exomiser. Default locations for the standard resources are contained in the
 * exomiserDataDirectory.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@EnableConfigurationProperties(ExomiserProperties.class)
public class DataDirectoryAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DataDirectoryAutoConfiguration.class);

    private final ExomiserProperties properties;

    public DataDirectoryAutoConfiguration(ExomiserProperties properties) {
        this.properties = properties;
    }

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     */
    @Bean
    @ConditionalOnMissingBean(name = "exomiserDataDirectory")
    public Path exomiserDataDirectory() {
        String dataDirectory = properties.getDataDirectory();
        if (dataDirectory == null || dataDirectory.isEmpty()) {
            throw new UndefinedDataDirectoryException("Exomiser data directory not defined. Please provide a valid path.");
        }
        Path dataPath = Paths.get(dataDirectory).toAbsolutePath();
        logger.info("Exomiser data directory set to: {}", dataPath);
        return dataPath;
    }

    @Bean
    public Path exomiserWorkingDirectory() {
        Path workingDir = Paths.get(getWorkingDir());
        logger.info("Exomiser working directory set to: {}", workingDir.toAbsolutePath());
        return workingDir;
    }

    // This is a test comment
//    @Bean
//    Path analysisPath() {
//        Path analysisPath = Paths.get(environment.getProperty("exomiser.working-directory"));
//        try {
//            if (!analysisPath.toFile().exists()) {
//                logger.info("Setting up analysis path at {}", analysisPath);
//                Files.createDirectory(analysisPath);
//            }
//        } catch (IOException ex) {
//            logger.error("Unable to create directory for analyses {}", analysisPath, ex);
//        }
//        return analysisPath;
//    }

    private String getWorkingDir() {
        if (properties.getWorkingDirectory() != null) {
            return properties.getWorkingDirectory();
        }
        String tempDirectory = System.getProperty("java.io.tmpdir");
        return new File(tempDirectory, "exomiser").getAbsolutePath();
    }
}
