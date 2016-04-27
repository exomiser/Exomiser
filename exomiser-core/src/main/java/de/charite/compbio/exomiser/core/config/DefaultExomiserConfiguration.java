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

package de.charite.compbio.exomiser.core.config;

import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@ComponentScan("de.charite.compbio.exomiser.core")
@Import({DataSourceConfig.class, CacheConfig.class})
@PropertySource("classpath:exomiser.version")
//adding exomiser.properties here kills external cli config, but would be ideal to have
//We're going to try this with Spring boot so these settings will be part of the application.properties
public class DefaultExomiserConfiguration {

    Logger logger = LoggerFactory.getLogger(DefaultExomiserConfiguration.class);

    @Autowired
    Environment env;

    @Bean
    public String buildVersion() {
        return env.getProperty("buildVersion");
    }

    @Bean
    public String buildTimestamp() {
        return env.getProperty("buildTimestamp");
    }

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     *
     * @return
     */
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("dataDir"));
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }

    private Path resolveRelativeToDataDir(String fileName) {
        return dataPath().resolve(fileName);
    }

    //Variant analysis configuration

    @Bean
    public Path ucscFilePath() {
        String ucscFileNameValue = getValueOfProperty("ucscFileName");
        Path ucscFilePath = resolveRelativeToDataDir(ucscFileNameValue);
        logger.debug("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }

    /**
     * This takes a few seconds to de-serialise.
     */
    @Lazy
    @Bean
    public JannovarData jannovarData() {
        try {
            return new JannovarDataSerializer(ucscFilePath().toString()).load();
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
    }

    @Lazy
    @Bean
    public TabixReader inDelTabixReader() {
        return getTabixReaderOrDefaultForProperty("caddInDelPath", "InDels.tsv.gz");
    }

    @Lazy
    @Bean
    public TabixReader snvTabixReader() {
        return getTabixReaderOrDefaultForProperty("caddSnvPath", "whole_genome_SNVs.tsv.gz");
    }

    @Lazy
    @Bean
    public TabixReader remmTabixReader() {
        String remmPath = getValueOfProperty("remmPath");
        String remmPathValue = resolveRelativeToDataDir(remmPath).toString();
        try {
            return new TabixReader(remmPathValue);
        } catch (IOException e) {
            throw new RuntimeException("REMM file not found ", e);
        }
        // TODO: remove the name in the new config file if we unbundle REMM and enable this:
//        return getTabixReaderOrDefaultForProperty("remmPath", "remmData.tsv.gz");
    }

    private TabixReader getTabixReaderOrDefaultForProperty(String property, String defaultFileName) {
        String tabixGzPathValue = getValueOfProperty(property);
        if (tabixGzPathValue.isEmpty()) {
            tabixGzPathValue = resolveRelativeToDataDir(defaultFileName).toString();
        }
        try {
            return new TabixReader(tabixGzPathValue);
        } catch (IOException e) {
            throw new RuntimeException(property + "=" + tabixGzPathValue + " file not found. Please check exomiser properties file points to a valid tabix .gz file.", e);
        }
    }

    //Prioritiser configuration

    @Bean
    public Path phenixDataDirectory() {
        String phenixDataDirValue = getValueOfProperty("phenomizerDataDir");
        Path phenixDataDirectory = resolveRelativeToDataDir(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    public Path hpoOntologyFilePath() {
        String hpoOntologyFileValue = getValueOfProperty("hpoOntologyFile");
        Path hpoOntologyFilePath = phenixDataDirectory().resolve(hpoOntologyFileValue);
        logger.debug("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }

    @Bean
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = getValueOfProperty("hpoAnnotationFile");
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
    public DataMatrix randomWalkMatrix() {
        String randomWalkFileNameValue = getValueOfProperty("randomWalkFileName");
        Path randomWalkFilePath = resolveRelativeToDataDir(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = getValueOfProperty("randomWalkIndexFileName");
        Path randomWalkIndexFilePath = resolveRelativeToDataDir(randomWalkIndexFileNameValue);

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }

    private String getValueOfProperty(String property) throws PropertyNotFoundException {
        String value = env.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(String.format("Property '%s' not present in exomiser.properties. Check that you have an exomiser.properties file in your classpath.", property));
        }
        return value;
    }

    public class PropertyNotFoundException extends RuntimeException {

        public PropertyNotFoundException(String message) {
            super(message);
        }
    }

}
