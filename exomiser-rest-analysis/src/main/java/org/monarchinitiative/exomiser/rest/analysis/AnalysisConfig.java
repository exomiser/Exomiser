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

package org.monarchinitiative.exomiser.rest.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class AnalysisConfig {

    private static Logger logger = LoggerFactory.getLogger(AnalysisConfig.class);

    @Autowired
    Environment environment;

    @Bean
    Path analysisPath() {
        Path analysisPath = Paths.get(environment.getProperty("exomiser.working-directory"));
        try {
            if (!analysisPath.toFile().exists()) {
                logger.info("Setting up analysis path at {}", analysisPath);
                Files.createDirectory(analysisPath);
            }
        } catch (IOException ex) {
            logger.error("Unable to create directory for analyses {}", analysisPath, ex);
        }
        return analysisPath;
    }
}
