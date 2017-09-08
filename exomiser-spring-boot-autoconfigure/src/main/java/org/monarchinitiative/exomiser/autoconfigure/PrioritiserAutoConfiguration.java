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

import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrixIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@ConditionalOnClass(PriorityFactory.class)
@Import({DataDirectoryAutoConfiguration.class, PhenotypeMatchServiceAutoConfiguration.class})
@EnableConfigurationProperties(ExomiserProperties.class)
@ComponentScan("org.monarchinitiative.exomiser.core.prioritisers")
public class PrioritiserAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserAutoConfiguration.class);

    private final ExomiserProperties exomiserProperties;
    private final Path exomiserDataDirectory;

    public PrioritiserAutoConfiguration(ExomiserProperties exomiserProperties, Path exomiserDataDirectory) {
        this.exomiserProperties = exomiserProperties;
        this.exomiserDataDirectory = exomiserDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "phenixDataDir")
    public Path phenixDataDirectory() {
        String phenixDataDirValue = exomiserProperties.getPhenixDataDir();
        Path phenixDataDirectory = exomiserDataDirectory.resolve(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoOboFilePath")
    public Path hpoOboFilePath() {
        String hpoFileName = exomiserProperties.getHpoFileName();
        Path hpoFilePath = phenixDataDirectory().resolve(hpoFileName);
        logger.debug("hpoOboFilePath: {}", hpoFilePath.toAbsolutePath());
        return hpoFilePath;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoAnnotationFilePath")
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = exomiserProperties.getHpoAnnotationFile();
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(hpoAnnotationFileValue);
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
     * @return
     */
    @Lazy
    @Bean
    @ConditionalOnMissingBean(name = "randomWalkMatrix")
    public DataMatrix randomWalkMatrix() {
        String randomWalkFileNameValue = exomiserProperties.getRandomWalkFileName();
        Path randomWalkFilePath = exomiserDataDirectory.resolve(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = exomiserProperties.getRandomWalkIndexFileName();
        Path randomWalkIndexFilePath = exomiserDataDirectory.resolve(randomWalkIndexFileNameValue);

        return DataMatrixIO.loadDataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }
}
