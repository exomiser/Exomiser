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

package org.monarchinitiative.exomiser.web.config;

import org.monarchinitiative.exomiser.autoconfigure.phenotype.PhenotypeProperties;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrixIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@EnableConfigurationProperties(PhenotypeProperties.class)
public class ExomiserWebConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserWebConfig.class);

    /**
     * Override bean to load the DataMatrix fully into memory rather than accessing off disk.
     */
    @Bean
    public DataMatrix randomWalkMatrix(PhenotypeProperties phenotypeProperties, Path phenotypeDataDirectory) {
        String randomWalkFileNameValue = phenotypeProperties.getRandomWalkFileName();
        Path randomWalkFilePath = phenotypeDataDirectory.resolve(randomWalkFileNameValue);
        logger.info("Loading in-memory random-walk matrix from {}", randomWalkFilePath);
        return DataMatrixIO.loadInMemoryDataMatrix(randomWalkFilePath);
    }
}
