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

package de.charite.compbio.exomiser.autoconfigure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.analysis.AnalysisFactory;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@ConditionalOnClass({Exomiser.class, AnalysisFactory.class})
@EnableConfigurationProperties(ExomiserProperties.class)
public class ExomiserAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserAutoConfiguration.class);

    @Autowired
    private ExomiserProperties exomiserProperties;

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     *
     * @return
     */
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(exomiserProperties.getDataDir());
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }

    private Path resolveRelativeToDataDir(String fileName) {
        return dataPath().resolve(fileName);
    }

    //Variant analysis configuration
    @Bean
    public Path ucscFilePath() {
        String ucscFileNameValue = exomiserProperties.getUcscFileName();
        Path ucscFilePath = resolveRelativeToDataDir(ucscFileNameValue);
        logger.debug("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }

    /**
     * This takes a few seconds to de-serialise. Can be overridden by defining your own bean.
     */
    @Lazy
    @Bean
    @ConditionalOnMissingBean
    public JannovarData jannovarData() {
        try {
            return new JannovarDataSerializer(ucscFilePath().toString()).load();
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
    }

    /**
     * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixReader inDelTabixReader() {
        return getTabixReaderOrDefaultForProperty(exomiserProperties.getCaddInDelPath());
    }

    /**
     * Optional full system path to CADD whole_genome_SNVs.tsv.gz and whole_genome_SNVs.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixReader snvTabixReader() {
        return getTabixReaderOrDefaultForProperty(exomiserProperties.getCaddSnvPath());
    }

    /**
     * Optional full system path to REMM remmData.tsv.gz and remmData.tsv.gz.tbi file pair.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixReader remmTabixReader() {
        return getTabixReaderOrDefaultForProperty(exomiserProperties.getRemmPath());
    }

    private TabixReader getTabixReaderOrDefaultForProperty(String pathToTabixGzFile) {
        String tabixGzPathValue = pathToTabixGzFile;
        if (tabixGzPathValue.isEmpty()) {
            tabixGzPathValue = resolveRelativeToDataDir("placeholder.tsv.gz").toString();
        }
        try {
            return new TabixReader(tabixGzPathValue);
        } catch (IOException e) {
            throw new RuntimeException(tabixGzPathValue + " file not found. Please check exomiser properties file points to a valid tabix .gz file.", e);
        }
    }

    //Prioritiser configuration

    @Bean
    public Path phenixDataDirectory() {
        String phenixDataDirValue = exomiserProperties.getPhenixDataDir();
        Path phenixDataDirectory = resolveRelativeToDataDir(phenixDataDirValue);
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
        Path randomWalkFilePath = resolveRelativeToDataDir(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = exomiserProperties.getRandomWalkIndexFileName();
        Path randomWalkIndexFilePath = resolveRelativeToDataDir(randomWalkIndexFileNameValue);

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        HikariDataSource dataSource;

        dataSource = new HikariDataSource(h2Config());

        logger.info("DataSource using maximum of {} database connections", dataSource.getMaximumPoolSize());
        logger.info("Returning a new {} DataSource pool to URL {} user: {}", dataSource.getPoolName(), dataSource.getJdbcUrl(), dataSource.getUsername());
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public HikariConfig h2Config() {

        ExomiserProperties.H2 h2 = exomiserProperties.getH2();

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(determineH2Url(h2));
        config.setUsername(h2.getUser());
        config.setPassword(h2.getPassword());
        config.setMaximumPoolSize(h2.getMaxConnections());
        config.setPoolName("exomiser-H2");

        return config;
    }

    private String determineH2Url(ExomiserProperties.H2 h2) {
        //the data path is the default place for the exomiser H2 database to be found.
        if (h2.getDirectory().isEmpty()) {
            logger.info("H2 path not set. Using default data path: {}", dataPath());
            return resolveH2UrlPathPlaceholder(h2.getUrl(), dataPath().toAbsolutePath().toString());
        } else {
            logger.info("Using user defined H2 path: {}", h2.getDirectory());
            return resolveH2UrlPathPlaceholder(h2.getUrl(), h2.getDirectory());
        }
    }

    private String resolveH2UrlPathPlaceholder(String h2Url, String h2AbsolutePath) {
        return h2Url.replace("${h2Path}", h2AbsolutePath);
    }

}
